organization := "de.lolhens"
name := "fs2-pcap"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.3"
crossScalaVersions := Seq("2.12.12", scalaVersion.value)

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/LolHens/fs2-pcap"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/LolHens/fs2-pcap"),
    "scm:git@github.com:LolHens/fs2-pcap.git"
  )
)
developers := List(
  Developer(id = "LolHens", name = "Pierre Kisters", email = "pierrekisters@gmail.com", url = url("https://github.com/LolHens/"))
)

libraryDependencies ++= Seq(
  "org.pcap4j" % "pcap4j-core" % "1.8.2",
  "org.pcap4j" % "pcap4j-packetfactory-static" % "1.8.2",
  "co.fs2" %% "fs2-core" % "2.5.0",
  "io.monix" %% "monix" % "3.3.0"
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

Compile / doc / sources := Seq.empty

version := {
  val tagPrefix = "refs/tags/"
  sys.env.get("CI_VERSION").filter(_.startsWith(tagPrefix)).map(_.drop(tagPrefix.length)).getOrElse(version.value)
}

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

credentials ++= (for {
  username <- sys.env.get("SONATYPE_USERNAME")
  password <- sys.env.get("SONATYPE_PASSWORD")
} yield Credentials(
  "Sonatype Nexus Repository Manager",
  "oss.sonatype.org",
  username,
  password
)).toList
