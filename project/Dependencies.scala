import sbt.*

object Dependencies {

  object Versions {
    val zio       = "2.0.13"
    val zioJson   = "0.5.0"
    val zioHttp   = "3.0.0-RC1"
    val zioConfig = "4.0.0-RC16"
    val jwt       = "9.1.2"
  }

  private val zio     = "dev.zio" %% "zio"      % Versions.zio
  private val zioJson = "dev.zio" %% "zio-json" % Versions.zioJson
  private val zioHttp = "dev.zio" %% "zio-http" % Versions.zioHttp

  private val zioConfig = Seq(
    "dev.zio" %% "zio-config"          % Versions.zioConfig,
    "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
    "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
  )

  val jwt = "com.github.jwt-scala" %% "jwt-core" % Versions.jwt

  val all = Seq(zio, zioJson, zioHttp, jwt) ++ zioConfig
}
