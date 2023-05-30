import sbt.*

object Dependencies {

  object Versions {
    val zio       = "2.0.13"
    val zioJson   = "0.5.0"
    val zioHttp   = "3.0.0-RC1"
    val zioConfig = "4.0.0-RC16"
    val jwt       = "9.1.2"

    val zioSql     = "0.1.2"
    val zioQuil    = "4.6.0.1"
    val postgresql = "42.5.4"
  }

  private val zio     = "dev.zio" %% "zio"      % Versions.zio
  private val zioJson = "dev.zio" %% "zio-json" % Versions.zioJson
  private val zioHttp = "dev.zio" %% "zio-http" % Versions.zioHttp

//  private val zioSql  = "dev.zio" %% "zio-sql-postgres" % Versions.zioSql //doesn't support Scala 3
  private val zioQuil    = "io.getquill"   %% "quill-jdbc-zio" % Versions.zioQuil
  private val postgresql = "org.postgresql" % "postgresql"     % Versions.postgresql

  private val db = Seq(zioQuil, postgresql)

  private val zioConfig = Seq(
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

  val jwt = "com.github.jwt-scala" %% "jwt-core" % Versions.jwt

  val all = Seq(zio, zioJson, zioHttp, jwt) ++ zioConfig ++ db
}
