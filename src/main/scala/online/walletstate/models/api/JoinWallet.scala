package online.walletstate.models.api

import zio.schema.{derived, DeriveSchema, Schema}

final case class JoinWallet(inviteCode: String) derives Schema
