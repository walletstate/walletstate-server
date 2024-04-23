package online.walletstate.http.api.endpoints

import online.walletstate.models.{Account, Transaction}
import online.walletstate.models.api.CreateTransaction
import online.walletstate.models.errors.{BadRequestError, UnauthorizedError}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait TransactionsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "transactions")
      .in[CreateTransaction]
      .out[Chunk[Transaction]](Status.Created)
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "transactions")
      .query[Account.Id](Account.Id.query)
      .query[Option[Transaction.Page.Token]](Transaction.Page.Token.queryCodec.optional)
      .out[Transaction.Page]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val get =
    Endpoint(Method.GET / "api" / "transactions" / Transaction.Id.path)
      .out[Chunk[Transaction]]
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list
  )

  val endpoints = endpointsMap.values

}

object TransactionsEndpoints extends TransactionsEndpoints
