package online.walletstate.services

import online.walletstate.config.AuthConfig
import online.walletstate.config.AuthConfig.config
import online.walletstate.models.AuthToken
import online.walletstate.models.errors.{AppError, InvalidAuthToken, InvalidAuthContext}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.*
import zio.json.*

trait TokenService {

  def encode[A: JsonEncoder](content: A): Task[AuthToken]

  def decode[A: JsonDecoder: Tag](token: String): IO[AppError, A]

}

object TokenService {
  def encode[A: JsonEncoder](content: A): ZIO[TokenService, Throwable, AuthToken] =
    ZIO.serviceWithZIO[TokenService](_.encode(content))

  def decode[A: JsonDecoder: Tag](token: String): ZIO[TokenService, AppError, A] =
    ZIO.serviceWithZIO[TokenService](_.decode(token))
}

case class StatelessTokenService(authConfig: AuthConfig) extends TokenService {

  private val algorithm = JwtAlgorithm.HS512
  private val secret    = authConfig.secret

  override def encode[A: JsonEncoder](content: A): Task[AuthToken] = for {
    clock <- Clock.javaClock
    claim <- ZIO.attempt(JwtClaim(content.toJson).issuedNow(clock).expiresIn(authConfig.tokenTTL.toSeconds)(clock))
    token <- ZIO.attempt(Jwt(clock).encode(claim, secret, algorithm))
  } yield AuthToken(token, authConfig.tokenTTL)

  override def decode[A: JsonDecoder: Tag](token: String): IO[AppError, A] = {
    for {
      clock  <- Clock.javaClock
      claims <- ZIO.fromTry(decodeJwt(token, clock)).mapError(e => InvalidAuthToken(e.getMessage))
      data   <- ZIO.fromEither(claims.content.fromJson[A]).mapError(_ => InvalidAuthContext(Tag[A].tag.shortName))
    } yield data
  }

  private def decodeJwt(token: String, clock: java.time.Clock) =
    Jwt(clock).decode(token, secret, Seq(algorithm))
}

object StatelessTokenService {

  val layer =
    ZLayer.fromZIO(ZIO.config[AuthConfig](AuthConfig.config).map(apply))
}
