package online.walletstate.domain

import zio.json.*

import java.time.Instant
import java.util.UUID

case class Namespace(id: UUID, name: String, createdBy: String, createdAt: Instant)

object Namespace {
  given codec: JsonCodec[Namespace] = DeriveJsonCodec.gen[Namespace]
}

case class CreateNamespace(name: String)

object CreateNamespace {
  given codec: JsonCodec[CreateNamespace] = DeriveJsonCodec.gen[CreateNamespace]
}

case class JoinNamespace(namespace: UUID)
object JoinNamespace {
  given codec: JsonCodec[JoinNamespace] = DeriveJsonCodec.gen[JoinNamespace]
}
