package online.walletstate.models

import zio.http.codec.PathCodec
import zio.json.{DeriveJsonCodec, JsonCodec}
import zio.{Random, Task, UIO, ZIO}

import java.time.Instant
import java.util.UUID

final case class Record(
    id: Record.Id,
    account: Account.Id,
    amount: BigDecimal,
    `type`: RecordType,
    category: Category.Id,
    description: Option[String],
    time: Instant,
    createdBy: User.Id
)

object Record {
  final case class Id(id: UUID) extends AnyVal

  object Id {
    def random: UIO[Id]            = Random.nextUUID.map(Id(_))
    def from(id: String): Task[Id] = ZIO.attempt(UUID.fromString(id)).map(Id(_))

    val path: PathCodec[Id] = zio.http.uuid("record-id").transform(Id(_))(_.id)

    given codec: JsonCodec[Id] = JsonCodec[UUID].transform(Id(_), _.id)
  }

  def make(
      account: Account.Id,
      amount: BigDecimal,
      `type`: RecordType,
      category: Category.Id,
      description: Option[String],
      time: Instant,
      createdBy: User.Id
  ): UIO[Record] =
    Id.random.map(Record(_, account, amount, `type`, category, description, time, createdBy))

  given codec: JsonCodec[Record] = DeriveJsonCodec.gen[Record]
}

//TODO Move to record object
enum RecordType {
  case Income, Spending, Transfer
}

object RecordType {
  given codec: JsonCodec[RecordType] = JsonCodec[String].transform(RecordType.valueOf(_), _.toString)
}
