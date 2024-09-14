import sbt.*

object Dependencies {

  object Versions {
    val zio       = "2.1.9"
    val zioHttp   = "3.0.0"
    val zioConfig = "4.0.2"
    val jwt       = "10.0.1"

    val zioLogging = "2.3.1"
    val slf4j      = "2.0.13"
    val logback    = "1.5.6"

    val zioSql     = "0.1.2"
    val zioQuil    = "4.8.4"
    val postgresql = "42.7.3"
    val flyway     = "10.15.2"

    val zioTestContainers = "0.10.0"

    val zioProcess = "0.7.2"
  }

  private val zio     = "dev.zio" %% "zio"      % Versions.zio
  private val zioHttp = "dev.zio" %% "zio-http" % Versions.zioHttp

  private val logging = Seq(
    "dev.zio"       %% "zio-logging"        % Versions.zioLogging,
    "dev.zio"       %% "zio-logging-slf4j2" % Versions.zioLogging,
    "org.slf4j"      % "slf4j-api"          % Versions.slf4j,
    "org.slf4j"      % "slf4j-simple"       % Versions.slf4j,
    "ch.qos.logback" % "logback-classic"    % Versions.logback
  )

  private val zioQuil    = "io.getquill"   %% "quill-jdbc-zio" % Versions.zioQuil
  private val postgresql = "org.postgresql" % "postgresql"     % Versions.postgresql
  private val flyway     = "org.flywaydb"   % "flyway-core"    % Versions.flyway

  private val db = Seq(zioQuil, postgresql, flyway)

  val zioConfig = Seq(
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

  val jwt = "com.github.jwt-scala" %% "jwt-core" % Versions.jwt

  val zioProcess = "dev.zio" %% "zio-process" % Versions.zioProcess

  val common = Seq(zio, zioHttp)
  val server = Seq(jwt) ++ zioConfig ++ db ++ logging

  val tests = Seq(
    "dev.zio"               %% "zio-test"                          % Versions.zio               % Test,
    "dev.zio"               %% "zio-test-sbt"                      % Versions.zio               % Test,
    "dev.zio"               %% "zio-test-magnolia"                 % Versions.zio               % Test,
    "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % Versions.zioTestContainers % Test,
    "io.github.scottweaver" %% "zio-2-0-db-migration-aspect"       % Versions.zioTestContainers % Test
  )

}
