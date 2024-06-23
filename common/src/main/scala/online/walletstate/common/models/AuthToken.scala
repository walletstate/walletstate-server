package online.walletstate.common.models

import zio.Duration
import zio.schema.{Schema, derived}

import java.time.ZonedDateTime

final case class AuthToken(token: String, expireIn: Duration) derives Schema

object AuthToken {
  final case class Create(expireAt: ZonedDateTime) derives Schema
}
