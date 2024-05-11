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
dockerUpdateLatest := false
dockerLabels       := Map("org.opencontainers.image.source" -> "https://github.com/walletstate/walletstate-server")

Docker / dockerRepository := Some("ghcr.io/walletstate")
Docker / packageName      := "walletstate-server"
Docker / version          := version.value
dockerExposedPorts ++= Seq(8081)

import scala.sys.process.Process

val createDockerBuildXConfig = taskKey[Unit]("Create docker buildx configuration if not exist")
createDockerBuildXConfig := {
  if (Process("docker buildx inspect multi-platform-builder").! == 1) {
    Process("docker buildx create --use --name multi-platform-builder", baseDirectory.value).!
  }
}

val dockerBuildImageWithBuildX = taskKey[Unit]("Build docker images with buildx")
dockerBuildImageWithBuildX := {
  dockerAliases.value.foreach { alias =>
    Process(
      s"docker buildx build --platform=linux/arm64,linux/amd64 --push -t $alias .",
      baseDirectory.value / "target" / "docker" / "stage"
    ).!
  }
}

Docker / publish := Def.sequential(Docker / publishLocal, createDockerBuildXConfig, dockerBuildImageWithBuildX).value



val generateAngularClient = taskKey[Unit]("Generate Angular Http client")
generateAngularClient := Def.taskDyn {
  Def.task {
    (Compile / runMain).toTask(s" zio.http.gen.GenPlayground ${version.value}").value
  }
}.value



ThisProject / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
Test / fork := true //is needed for testcontainers
