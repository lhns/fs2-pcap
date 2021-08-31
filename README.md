# fs2-pcap

[![Test Workflow](https://github.com/LolHens/fs2-pcap/workflows/test/badge.svg)](https://github.com/LolHens/fs2-pcap/actions?query=workflow%3Atest)
[![Release Notes](https://img.shields.io/github/release/LolHens/fs2-pcap.svg?maxAge=3600)](https://github.com/LolHens/fs2-pcap/releases/latest)
[![Maven Central](https://img.shields.io/maven-central/v/de.lolhens/fs2-pcap_2.13)](https://search.maven.org/artifact/de.lolhens/fs2-pcap_2.13)
[![Apache License 2.0](https://img.shields.io/github/license/LolHens/fs2-pcap.svg?maxAge=3600)](https://www.apache.org/licenses/LICENSE-2.0)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

This project provides [fs2](https://github.com/typelevel/fs2) helper methods
for [pcap4j](https://github.com/kaitoy/pcap4j). Have a look at the [sample](https://github.com/LolHens/fs2-pcap/blob/main/sample/src/main/scala/de/lolhens/fs2/pcap/sample/Sample.scala) to get started.

## Usage

### build.sbt

```sbt
// use this snippet for cats-effect 2
libraryDependencies += "de.lolhens" %% "fs2-pcap" % "0.0.3"

// use this snippet for cats-effect 3
libraryDependencies += "de.lolhens" %% "fs2-pcap" % "0.1.0"
```

## License

This project uses the Apache 2.0 License. See the file called LICENSE.
