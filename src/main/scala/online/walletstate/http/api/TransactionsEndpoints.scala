package online.walletstate.http.api

import online.walletstate.models.{Account, AppError, Transaction}
import online.walletstate.models.api.CreateTransaction
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait TransactionsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "transactions")
      .in[CreateTransaction]
      .out[List[Transaction]](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "transactions")
      .query[Account.Id](Account.Id.query)
      .query[Option[Transaction.Page.Token]](Transaction.Page.Token.queryCodec.optional)
      .out[Transaction.Page]
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val get =
    Endpoint(Method.GET / "api" / "transactions" / Transaction.Id.path)
      .out[List[Transaction]]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list
  )

  val endpoints = endpointsMap.values

}

object TransactionsEndpoints extends TransactionsEndpoints
