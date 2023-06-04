package online.walletstate.services

import online.walletstate.config.AuthConfig
import online.walletstate.config.AuthConfig.config
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.*
import zio.json.*

trait TokenService {

  def encode[A: JsonEncoder](content: A): Task[String]

  def decode[A: JsonDecoder](token: String): IO[String, A]

}

object TokenService {
  def encode[A: JsonEncoder](content: A): ZIO[TokenService, Throwable, String] =
    ZIO.serviceWithZIO[TokenService](_.encode(content))

  def decode[A: JsonDecoder](token: String): ZIO[TokenService, String, A] =
    ZIO.serviceWithZIO[TokenService](_.decode(token))
}

case class StatelessTokenService(authConfig: AuthConfig) extends TokenService {

  private val algorithm = JwtAlgorithm.HS512
  private val secret    = authConfig.secret

  override def encode[A: JsonEncoder](content: A): Task[String] = for {
    clock <- Clock.javaClock
    claim <- ZIO.attempt(JwtClaim(content.toJson).issuedNow(clock).expiresIn(authConfig.tokenTTL.toSeconds)(clock))
  } yield Jwt(clock).encode(claim, secret, algorithm)

  // TODO change error type
  override def decode[A: JsonDecoder](token: String): IO[String, A] = {
    for {
      clock  <- Clock.javaClock
      claims <- ZIO.fromTry(Jwt(clock).decode(token, secret, Seq(algorithm))).mapError(_.getMessage)
      data   <- ZIO.fromEither(claims.content.fromJson[A])
    } yield data
  }
}

object StatelessTokenService {

  val layer =
    ZLayer.fromZIO(ZIO.config[AuthConfig](AuthConfig.config).map(apply))
}
