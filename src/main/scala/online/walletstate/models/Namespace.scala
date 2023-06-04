package online.walletstate.models

import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.util.UUID

final case class Namespace(id: Namespace.Id, name: String, createdBy: User.Id)

object Namespace {
  final case class Id(id: UUID) extends AnyVal
  object Id {
    def random: UIO[Id] = Random.nextUUID.map(Id(_))

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(name: String, createdBy: User.Id): UIO[Namespace] =
    Id.random.map(Namespace(_, name, createdBy))

  given codec: JsonCodec[Namespace] = DeriveJsonCodec.gen[Namespace]
}
