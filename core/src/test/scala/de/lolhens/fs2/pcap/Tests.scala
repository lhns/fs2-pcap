package de.lolhens.fs2.pcap

import cats.effect.IO
import de.lolhens.fs2.pcap
import fs2.Stream
import org.pcap4j.packet.namednumber.{DataLinkType, EtherType}
import org.pcap4j.packet.{EthernetPacket, Packet}
import org.pcap4j.util.MacAddress

import scala.concurrent.ExecutionContext.Implicits.global

class Tests extends munit.FunSuite {
  test("encode and decode Packets") {
    val ethernetPacket: Packet = new EthernetPacket.Builder()
      .srcAddr(MacAddress.getByName("00:00:00:00:00:00"))
      .dstAddr(MacAddress.getByName("00:00:00:00:00:00"))
      .`type`(EtherType.IPV4)
      .pad(Array[Byte]())
      .build()

    Stream[IO, Packet](ethernetPacket)
      .through(pcap.encodePackets)
      .through(pcap.decodePackets(DataLinkType.EN10MB))
      .compile
      .toList
      .unsafeToFuture()
      .map { decoded =>
        assertEquals(decoded, List(ethernetPacket))
      }
  }
}
