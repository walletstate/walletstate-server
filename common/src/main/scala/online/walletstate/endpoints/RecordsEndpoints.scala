package online.walletstate.http.endpoints

import online.walletstate.common.models.{Account, HttpError, Page, Record}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait RecordsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "records")
      .in[Record.Data]
      .out[Record.Full](Status.Created)
      .withBadRequestCodec

  val list =
    Endpoint(Method.GET / "api" / "records")
      .query[Account.Id](Account.Id.query)
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.Full]]
      .withBadRequestCodec

  val get =
    Endpoint(Method.GET / "api" / "records" / Record.Id.path)
      .out[Record.Full]
      .outError[HttpError.NotFound](HttpError.NotFound.status)
      .withBadRequestCodec

  val update =
    Endpoint(Method.PUT / "api" / "records" / Record.Id.path)
      .in[Record.Data]
      .out[Record.Full]
      .withBadRequestCodec

  val delete =
    Endpoint(Method.DELETE / "api" / "records" / Record.Id.path)
      .out[Unit](Status.NoContent)
      .withBadRequestCodec

  override val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "update" -> update,
    "delete" -> delete,
    "list"   -> list
  )

  override val endpoints = Chunk(
    create,
//    list, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    get,
    update,
    delete
  )

}

object RecordsEndpoints extends RecordsEndpoints
