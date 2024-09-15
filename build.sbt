import scala.sys.process.Process

ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "online.walletstate"

lazy val walletstate = project
  .in(file("."))
  .settings(
    name := "walletstate"
  )
  .aggregate(common, server, client, angularClientGen)

lazy val common = project
  .in(file("common"))
  .settings(
    name := "common",
    libraryDependencies ++= Dependencies.common,
    release := Def.sequential(publish).value
  )
  .settings(libraryPublishSettings)
  .settings(gitVersionSettings)
  .enablePlugins(GitVersioning)

lazy val server = project
  .in(file("server"))
  .dependsOn(common)
  .settings(
    name := "walletstate-server",
    libraryDependencies ++= Dependencies.server,
    libraryDependencies ++= Dependencies.tests,
    Compile / mainClass := Some("online.walletstate.Application"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    Test / fork := true, // is needed for testcontainers
    release     := Def.sequential(Docker / publish).value
  )
  .settings(dockerSettings)
  .settings(gitVersionSettings)
  .enablePlugins(JavaAppPackaging, DockerPlugin, GitVersioning)

lazy val client = project
  .in(file("client"))
  .dependsOn(common)
  .settings(
    name := "client",
    libraryDependencies ++= Dependencies.zioConfig,
    release := Def.sequential(publish).value
  )
  .settings(gitVersionSettings)
  .settings(libraryPublishSettings)
  .enablePlugins(GitVersioning)

lazy val angularClientGen = project
  .in(file("angular-client-gen"))
  .dependsOn(common)
  .settings(
    name := "angular-client-generator",
    libraryDependencies += Dependencies.zioProcess,
    release := Def.sequential(generateAngularClient).value
  )
  .settings(angularClientGenSettings)
  .settings(gitVersionSettings)
  .enablePlugins(GitVersioning)

///////////////////////////////////////////////////////////////////////////////////////
// Git version settings
///////////////////////////////////////////////////////////////////////////////////////
val tagRegexp    = "v([0-9]+\\.[0-9]+\\.[0-9]+)".r
val commitRegexp = "v([0-9]+\\.[0-9]+\\.[0-9]+)-([0-9]+)-(.*)".r

lazy val gitVersionSettings = Seq(
  git.useGitDescribe := true,
  git.baseVersion    := "0.0.0",
  git.gitTagToVersionNumber := {
    case tagRegexp(v)                        => Some(v)
    case commitRegexp(v, commitNumber, hash) => Some(s"$v-$commitNumber-$hash")
    case string: String                      => Some(string)
    case _                                   => None
  }
)

///////////////////////////////////////////////////////////////////////////////////////
// Maven publish
///////////////////////////////////////////////////////////////////////////////////////
lazy val libraryPublishSettings = Seq(
  publishTo := Some(
    "GitHub WalletState Maven Packages" at "https://maven.pkg.github.com/walletstate/walletstate-server"
  ),
  publishMavenStyle := true,
  credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
)

///////////////////////////////////////////////////////////////////////////////////////
// Release settings
///////////////////////////////////////////////////////////////////////////////////////
val release = taskKey[Unit]("Release new version of the component")

///////////////////////////////////////////////////////////////////////////////////////
// Docker settings
///////////////////////////////////////////////////////////////////////////////////////
lazy val createBuildXConfig   = taskKey[Unit]("Create docker buildx configuration if not exist")
lazy val buildImageWithBuildX = taskKey[Unit]("Build docker images with buildx")
lazy val dockerSettings = Seq(
  dockerBaseImage    := "openjdk:20-jdk-slim", // todo investigate. JRE?
  dockerUpdateLatest := false,
  dockerExposedPorts ++= Seq(8081),
  dockerLabels := Map("org.opencontainers.image.source" -> "https://github.com/walletstate/walletstate-server"),
  Docker / dockerRepository := Some("ghcr.io/walletstate"),
  Docker / packageName      := "walletstate-server",
  Docker / version          := version.value,
  Docker / publish          := Def.sequential(Docker / publishLocal, createBuildXConfig, buildImageWithBuildX).value,
  createBuildXConfig := {
    if (Process("docker buildx inspect multi-platform-builder").! == 1) {
      Process("docker buildx create --use --name multi-platform-builder", baseDirectory.value).!
    }
  },
  buildImageWithBuildX := {
    dockerAliases.value.foreach { alias =>
      Process(
        s"docker buildx build --platform=linux/arm64,linux/amd64 --push -t $alias .",
        baseDirectory.value / "target" / "docker" / "stage"
      ).!
    }
  }
)

///////////////////////////////////////////////////////////////////////////////////////
// Angular client generation settings
///////////////////////////////////////////////////////////////////////////////////////
val generateAngularClient = taskKey[Unit]("Generate Angular Http client")
val angularClientGenSettings = Seq(
  generateAngularClient := Def.taskDyn {
    val clientVersion = version.value
    Def.task {
      (Compile / runMain).toTask(s" zio.http.gen.GenPlayground $clientVersion").value
    }
  }.value
)
