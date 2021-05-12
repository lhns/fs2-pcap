package de.lolhens.fs2

import cats.effect.{Async, Resource, Sync}
import fs2.{Chunk, Pipe, Stream}
import org.pcap4j.core.PcapHandle.BlockingMode
import org.pcap4j.core.{PcapHandle, PcapNetworkInterface, Pcaps}
import org.pcap4j.packet.Packet
import org.pcap4j.packet.factory.{PacketFactories, PacketFactory}
import org.pcap4j.packet.namednumber.{DataLinkType, NamedNumber}

import java.util.ConcurrentModificationException
import java.util.concurrent.Executor
import scala.collection.JavaConverters._
import scala.concurrent.duration._

package object pcap {
  def networkInterfaces[F[_]](implicit F: Sync[F]): Stream[F, PcapNetworkInterface] =
    Stream.evalSeq(F.delay(Pcaps.findAllDevs.asScala.toSeq))

  def capture[F[_]](deviceName: String)
                   (builder: PcapHandle.Builder => PcapHandle.Builder)
                   (implicit F: Sync[F]): Resource[F, PcapHandle] = Resource.make(F.delay(
    builder(new PcapHandle.Builder(deviceName)).build()
  ))(handle => F.delay(
    handle.close()
  ))

  def capture[F[_]](networkInterface: PcapNetworkInterface)
                   (builder: PcapHandle.Builder => PcapHandle.Builder)
                   (implicit F: Sync[F]): Resource[F, PcapHandle] =
    capture(networkInterface.getName)(builder)

  private val syncExecutor: Executor = (command: Runnable) => command.run()

  private def getNextRawPackets(handle: PcapHandle, packetCount: Int): Array[Array[Byte]] = {
    val buffer = new Array[Array[Byte]](packetCount)

    var i = 0
    val expected = handle.dispatch(packetCount, { packet: Array[Byte] =>
      val index = i
      buffer(index) = packet
      i = index + 1
    }, syncExecutor)

    if (i != expected)
      throw new ConcurrentModificationException("Callback in PcapHandle.dispatch was called concurrently!")

    buffer.take(i)
  }

  def decodePackets[F[_], T, N <: NamedNumber[_, _]](packetFactory: PacketFactory[T, N],
                                                     number: N): Pipe[F, Array[Byte], T] = _.map(packet =>
    packetFactory.newInstance(packet, 0, packet.length, number)
  )

  private lazy val packetFactory = PacketFactories.getFactory(classOf[Packet], classOf[DataLinkType])

  def decodePackets[F[_]](dataLinkType: DataLinkType): Pipe[F, Array[Byte], Packet] =
    decodePackets(packetFactory, dataLinkType)

  def decodePackets[F[_]](handle: PcapHandle): Pipe[F, Array[Byte], Packet] =
    decodePackets(handle.getDlt)

  def encodePackets[F[_]]: Pipe[F, Packet, Array[Byte]] = _.map(_.getRawData)

  def dispatchRawPackets[F[_]](handle: PcapHandle,
                               chunkSize: Int = 512,
                               yieldTime: FiniteDuration = 10.millis)
                              (implicit F: Async[F]): Stream[F, Array[Byte]] = Stream.suspend {
    handle.setBlockingMode(BlockingMode.NONBLOCKING)

    Stream.eval(F.delay(Chunk.array(getNextRawPackets(handle, chunkSize))))
      .repeat
      .flatMap(rawPackets =>
        if (rawPackets.isEmpty) Stream.sleep_(yieldTime)
        else Stream.chunk(rawPackets)
      )
  }

  def dispatchPackets[F[_]](handle: PcapHandle,
                            chunkSize: Int = 512,
                            yieldTime: FiniteDuration = 10.millis)
                           (implicit F: Async[F]): Stream[F, Packet] =
    dispatchRawPackets[F](handle, chunkSize, yieldTime)
      .through(decodePackets(handle))

  def sendRawPackets[F[_]](handle: PcapHandle)
                          (implicit F: Sync[F]): Pipe[F, Array[Byte], Unit] = _.evalMap(rawPacket =>
    F.delay(handle.sendPacket(rawPacket))
  ).drain ++ Stream.emit(())

  def sendPackets[F[_]](handle: PcapHandle)
                       (implicit F: Sync[F]): Pipe[F, Packet, Unit] =
    encodePackets[F]
      .andThen(sendRawPackets(handle))
}
