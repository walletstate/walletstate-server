package online.walletstate.domain

import java.util.UUID

case class User(id: String, username: String, namespace: Option[UUID] = None)
