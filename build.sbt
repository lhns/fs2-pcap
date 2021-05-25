lazy val scalaVersions = Seq("2.13.6", "2.12.13")

ThisBuild / scalaVersion := scalaVersions.head
ThisBuild / versionScheme := Some("early-semver")

lazy val commonSettings: SettingsDefinition = Def.settings(
  organization := "de.lolhens",
  name := "fs2-pcap",
  version := "0.0.1-SNAPSHOT",

  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),

  homepage := Some(url("https://github.com/LolHens/fs2-pcap")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/LolHens/fs2-pcap"),
      "scm:git@github.com:LolHens/fs2-pcap.git"
    )
  ),
  developers := List(
    Developer(id = "LolHens", name = "Pierre Kisters", email = "pierrekisters@gmail.com", url = url("https://github.com/LolHens/"))
  ),

  libraryDependencies ++= Seq(
    "org.scalameta" %% "munit" % "0.7.26" % Test,
    "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
  ),

  testFrameworks += new TestFramework("munit.Framework"),

  addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),

  Compile / doc / sources := Seq.empty,

  version := {
    val tagPrefix = "refs/tags/"
    sys.env.get("CI_VERSION").filter(_.startsWith(tagPrefix)).map(_.drop(tagPrefix.length)).getOrElse(version.value)
  },

  publishMavenStyle := true,

  publishTo := sonatypePublishToBundle.value,

  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    username,
    password
  )).toList
)

name := (root / name).value

lazy val root: Project =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(
      publishArtifact := false,
      publish / skip := true
    )
    .aggregate(core.projectRefs: _*)
    .aggregate(sample.projectRefs: _*)

lazy val core = projectMatrix.in(file("core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "org.pcap4j" % "pcap4j-core" % "1.8.2",
      "org.pcap4j" % "pcap4j-packetfactory-static" % "1.8.2" % Test,
      "co.fs2" %% "fs2-core" % "2.5.5",
    ),
  )
  .jvmPlatform(scalaVersions)

lazy val sample = projectMatrix.in(file("sample"))
  .settings(commonSettings)
  .settings(
    name := "fs2-pcap-sample",

    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.pcap4j" % "pcap4j-packetfactory-static" % "1.8.2",
      "io.monix" %% "monix" % "3.4.0",
    ),

    publish / skip := true,
  )
  .dependsOn(core)
  .jvmPlatform(Seq(scalaVersions.head))
