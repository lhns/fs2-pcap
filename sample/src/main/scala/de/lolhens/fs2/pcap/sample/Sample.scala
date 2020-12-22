package de.lolhens.fs2.pcap.sample

import cats.effect.ExitCode
import de.lolhens.fs2.pcap
import fs2.Stream
import monix.eval.{Task, TaskApp}
import org.pcap4j.core.BpfProgram.BpfCompileMode
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode

import scala.jdk.CollectionConverters._

object Sample extends TaskApp {
  private def padLeft(string: String, length: Int): String = (" " * length + string).takeRight(length)

  private def padRight(string: String, length: Int): String = (string + " " * length).take(length)

  override def run(args: List[String]): Task[ExitCode] =
    (for {
      networkInterface <- pcap.networkInterfaces[Task]
      _ = println(networkInterface.getDescription)
      handle <- Stream.resource(pcap.capture[Task](networkInterface)(_
        .promiscuousMode(PromiscuousMode.PROMISCUOUS)
      ))
    } yield {
      handle.setFilter("", BpfCompileMode.OPTIMIZE)

      pcap.dispatchPackets[Task](handle)
        .zipWithIndex
        .map { case (packet, index) =>
          println(
            padRight(networkInterface.getDescription, 20) + " " +
              padLeft(index.toString, 6) + " " +
              padLeft(packet.getRawData.length.toString, 6) + " " +
              packet.asScala.map(_.getClass.toString.reverse.takeWhile(_ != '.').reverse).mkString(", ")
          )
        }
    })
      .parJoinUnbounded
      .compile
      .drain
      .map(_ => ExitCode.Success)
}
