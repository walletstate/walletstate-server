package online.walletstate.models

import zio.Duration

final case class AuthToken(token: String, expireIn: Duration)
