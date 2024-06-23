package online.walletstate.common.models

import zio.http.codec.PathCodec
import zio.schema.{derived, Schema}
import zio.{Chunk, Random, Task, UIO, ZIO}

import java.util.UUID

final case class Category(
    id: Category.Id,
    group: Group.Id,
    name: String,
    icon: Option[Icon.Id],
    tags: List[String],
    idx: Int
) extends Groupable
    derives Schema

object Category {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("category-id").transform(Id(_))(_.id)

    given schema: Schema[Id] = Schema[UUID].transform(Id(_), _.id)
  }

  def make(info: Data): UIO[Category] =
    Id.random.map(Category(_, info.group, info.name, info.icon, info.tags, info.idx))

  final case class Data(
      group: Group.Id,
      name: String,
      icon: Option[Icon.Id],
      tags: List[String],
      idx: Int
  ) derives Schema
}
