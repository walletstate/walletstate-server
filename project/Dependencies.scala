import sbt.*

object Dependencies {

  object Versions {
    val zio       = "2.0.21"
    val zioJson   = "0.6.2"
    val zioHttp   = "3.0.0-RC6"
    val zioConfig = "4.0.0-RC16"
    val jwt       = "9.2.0"

    val zioLogging = "2.1.16"
    val slf4j      = "2.0.9"
    val logback    = "1.4.11"

    val zioSql     = "0.1.2"
    val zioQuil    = "4.8.0"
    val postgresql = "42.5.4"
    val flyway     = "9.16.0"

    val zioTestContainers = "0.10.0"
  }

  private val zio     = "dev.zio" %% "zio"      % Versions.zio
  private val zioJson = "dev.zio" %% "zio-json" % Versions.zioJson
  private val zioHttp = "dev.zio" %% "zio-http" % Versions.zioHttp

  private val logging = Seq(
    "dev.zio"       %% "zio-logging"        % Versions.zioLogging,
    "dev.zio"       %% "zio-logging-slf4j2" % Versions.zioLogging,
    "org.slf4j"      % "slf4j-api"          % Versions.slf4j,
    "org.slf4j"      % "slf4j-simple"       % Versions.slf4j,
    "ch.qos.logback" % "logback-classic"    % Versions.logback
  )

//  private val zioSql  = "dev.zio" %% "zio-sql-postgres" % Versions.zioSql //doesn't support Scala 3
  private val zioQuil    = "io.getquill"   %% "quill-jdbc-zio" % Versions.zioQuil
  private val postgresql = "org.postgresql" % "postgresql"     % Versions.postgresql
  private val flyway     = "org.flywaydb"   % "flyway-core"    % Versions.flyway

  private val db = Seq(zioQuil, postgresql, flyway)

  private val zioConfig = Seq(
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

  val jwt = "com.github.jwt-scala" %% "jwt-core" % Versions.jwt

  val all = Seq(zio, zioJson, zioHttp, jwt) ++ zioConfig ++ db ++ logging

  val tests = Seq(
    "dev.zio"               %% "zio-test"                          % Versions.zio               % Test,
    "dev.zio"               %% "zio-test-sbt"                      % Versions.zio               % Test,
    "dev.zio"               %% "zio-test-magnolia"                 % Versions.zio               % Test,
    "io.github.scottweaver" %% "zio-2-0-testcontainers-postgresql" % Versions.zioTestContainers % Test,
    "io.github.scottweaver" %% "zio-2-0-db-migration-aspect"       % Versions.zioTestContainers % Test
  )

}
