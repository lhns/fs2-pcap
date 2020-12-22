package de.lolhens.fs2.pcap

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.pcap4j.core.BpfProgram.BpfCompileMode
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode
import org.pcap4j.core._

import scala.jdk.CollectionConverters._

object Pcap {
  def main(args: Array[String]): Unit = {
    val devs = Pcaps.findAllDevs.asScala.toSeq

    devs.foreach(e => println(e.getDescription))

    val nif = devs(2)

    val phb = new PcapHandle.Builder(nif.getName).promiscuousMode(PromiscuousMode.PROMISCUOUS)

    val handle = phb.build
    handle.setFilter("", BpfCompileMode.OPTIMIZE)

    receiveRawPackets[Task](handle)
      .through(decodePackets(handle))
      .repeat
      .zipWithIndex
      .map { case (packet, index) =>
        println(packet.getRawData.length)
        ()
      }
      .compile
      .drain
      .runSyncUnsafe()

    println("end")
    handle.close()
  }
}
