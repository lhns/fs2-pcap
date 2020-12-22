package de.lolhens.fs2

import cats.effect.{Sync, Timer}
import fs2.{Chunk, Pipe, Stream}
import org.pcap4j.core.PcapHandle
import org.pcap4j.core.PcapHandle.BlockingMode
import org.pcap4j.packet.Packet
import org.pcap4j.packet.factory.PacketFactories
import org.pcap4j.packet.namednumber.DataLinkType

import java.util.concurrent.Executor
import scala.concurrent.duration._

package object pcap {
  private val syncExecutor: Executor = (command: Runnable) => command.run()

  private def getNextRawPackets(handle: PcapHandle, packetCount: Int): Array[Array[Byte]] = {
    val buffer = new Array[Array[Byte]](packetCount)
    var i = 0
    handle.dispatch(packetCount, { packet: Array[Byte] =>
      buffer(i) = packet
      i += 1
    }, syncExecutor)
    buffer.take(i)
  }

  private lazy val packetFactory = PacketFactories.getFactory(classOf[Packet], classOf[DataLinkType])

  def decodePackets[F[_]](handle: PcapHandle): Pipe[F, Array[Byte], Packet] = _.map(packet =>
    packetFactory.newInstance(packet, 0, packet.length, handle.getDlt)
  )

  def encodePackets[F[_]]: Pipe[F, Packet, Array[Byte]] = _.map(_.getRawData)

  def receiveRawPackets[F[_]](handle: PcapHandle,
                              packetCount: Int = 512,
                              yieldTime: FiniteDuration = 10.millis)
                             (implicit F: Sync[F], timer: Timer[F]): Stream[F, Array[Byte]] = Stream.suspend {
    handle.setBlockingMode(BlockingMode.NONBLOCKING)

    Stream.evalUnChunk[F, Array[Byte]](F.delay(Chunk.array(getNextRawPackets(handle, packetCount))))
      .repeat
      .chunks
      .flatMap(rawPackets =>
        if (rawPackets.isEmpty) Stream.sleep_(yieldTime)
        else Stream.chunk(rawPackets)
      )
  }

  def receivePackets[F[_]](handle: PcapHandle,
                           packetCount: Int = 512,
                           yieldTime: FiniteDuration = 10.millis)
                          (implicit F: Sync[F], timer: Timer[F]): Stream[F, Packet] =
    receiveRawPackets[F](handle, packetCount, yieldTime)
      .through(decodePackets(handle))

  def sendRawPackets[F[_]](handle: PcapHandle)
                          (implicit F: Sync[F]): Pipe[F, Array[Byte], Unit] = _.evalMap(rawPacket =>
    F.delay(handle.sendPacket(rawPacket))
  )
}
