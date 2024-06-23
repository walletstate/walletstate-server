package online.walletstate.services

import online.walletstate.common.models.AuthToken
import online.walletstate.config.AuthConfig
import online.walletstate.config.AuthConfig.config
import online.walletstate.models.AppError.TokenDecodeError
import online.walletstate.models.{AppError, AuthContext}
import pdi.jwt.{Jwt, JwtAlgorithm, JwtClaim}
import zio.*
import zio.json.*

import java.time.ZonedDateTime

trait TokenService {

  def encode[A: JsonEncoder](content: A): UIO[AuthToken]

  def encode[A: JsonEncoder](content: A, expireAt: ZonedDateTime): UIO[AuthToken]

  def decode[A: JsonDecoder: Tag](token: String): IO[TokenDecodeError, A]

  def decodeAuthContext[A <: AuthContext: JsonDecoder: Tag](
      token: String,
      expectedType: AuthContext.Type
  ): IO[TokenDecodeError, A]
}

case class StatelessTokenService(authConfig: AuthConfig) extends TokenService {

  private val algorithm = JwtAlgorithm.HS512
  private val secret    = authConfig.secret

  override def encode[A: JsonEncoder](content: A): UIO[AuthToken] = encode(content, authConfig.tokenTTL)

  override def encode[A: JsonEncoder](content: A, expireAt: ZonedDateTime): UIO[AuthToken] =
    for {
      now <- Clock.instant
      expireIn = Duration.fromInterval(now, expireAt.toInstant)
      token <- encode(content, expireIn)
    } yield token

  private def encode[A: JsonEncoder](content: A, expireIn: Duration): UIO[AuthToken] = for {
    clock <- Clock.javaClock
    claim <- ZIO.succeed(JwtClaim(content.toJson).issuedNow(clock).expiresIn(expireIn.toSeconds)(clock))
    token <- ZIO.succeed(Jwt(clock).encode(claim, secret, algorithm))
  } yield AuthToken(token, authConfig.tokenTTL)

  override def decode[A: JsonDecoder: Tag](token: String): IO[TokenDecodeError, A] =
    for {
      clock  <- Clock.javaClock
      claims <- ZIO.fromTry(decodeJwt(token, clock)).mapError(e => TokenDecodeError(e.getMessage))
      data   <- ZIO.fromEither(claims.content.fromJson[A]).mapError(_ => TokenDecodeError.contentMalformed)
    } yield data

  override def decodeAuthContext[A <: AuthContext: JsonDecoder: Tag](
      token: String,
      expectedType: AuthContext.Type
  ): IO[TokenDecodeError, A] =
    for {
      ctx <- decode[A](token)
      _   <- ZIO.cond(ctx.`type` == expectedType, (), TokenDecodeError.invalidTokenType(expectedType, ctx.`type`))
    } yield ctx

  private def decodeJwt(token: String, clock: java.time.Clock) =
    Jwt(clock).decode(token, secret, Seq(algorithm))
}

object StatelessTokenService {

  val layer =
    ZLayer.fromZIO(ZIO.config[AuthConfig](AuthConfig.config).map(apply))
}
