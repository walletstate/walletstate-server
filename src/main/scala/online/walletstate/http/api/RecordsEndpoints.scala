package online.walletstate.http.api

import online.walletstate.models.{Account, AppError, Page, Record}
import online.walletstate.models.api.{FullRecord, RecordData}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait RecordsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "records")
      .in[RecordData]
      .out[FullRecord](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "records")
      .query[Account.Id](Account.Id.query)
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[FullRecord]]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val get =
    Endpoint(Method.GET / "api" / "records" / Record.Id.path)
      .out[FullRecord]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.RecordNotExist.type](Status.NotFound)

  val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list
  )

  val endpoints = Chunk(
    create,
//    list, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    get
  )

}

object RecordsEndpoints extends RecordsEndpoints
