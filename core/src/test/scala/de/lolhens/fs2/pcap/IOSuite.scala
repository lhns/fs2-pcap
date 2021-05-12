package de.lolhens.fs2.pcap

import cats.effect.{IO, unsafe}
import munit.TaglessFinalSuite

import scala.concurrent.Future

abstract class IOSuite extends TaglessFinalSuite[IO] {
  override protected def toFuture[A](f: IO[A]): Future[A] = f.unsafeToFuture()(unsafe.IORuntime.global)
}
