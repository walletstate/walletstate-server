ThisProject / scalaVersion := "3.3.1"

ThisProject / name         := "walletstate"
ThisProject / organization := "online.walletstate"

ThisProject / libraryDependencies ++= Dependencies.all
ThisProject / libraryDependencies ++= Dependencies.tests

enablePlugins(
  JavaAppPackaging,
  DockerPlugin,
  GitVersioning
)

Compile / mainClass := Some("online.walletstate.Application")

git.useGitDescribe := true
git.baseVersion    := "0.0.0"
val tagRegexp    = "v([0-9]+\\.[0-9]+\\.[0-9]+)".r
val commitRegexp = "v([0-9]+\\.[0-9]+\\.[0-9]+)-([0-9]+)-(.*)".r
git.gitTagToVersionNumber := {
  case tagRegexp(v)                        => Some(v)
  case commitRegexp(v, commitNumber, hash) => Some(s"$v-$commitNumber-$hash")
  case string: String                      => Some(string)
  case _                                   => None
}

dockerBaseImage    := "openjdk:20-jdk-slim" //todo investigate. JRE?
dockerUpdateLatest := true
dockerLabels       := Map("org.opencontainers.image.source" -> "https://github.com/walletstate/walletstate-server")

Docker / dockerRepository := Some("ghcr.io/walletstate")
Docker / packageName      := "walletstate-server"
Docker / version          := version.value
dockerExposedPorts ++= Seq(8081)

ThisProject / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
Test / fork := true //is needed for testcontainers
