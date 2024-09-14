package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Account, HttpError, Page, Record}
import zio.Chunk
import zio.http.codec.HttpCodec
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Method, Status}

trait RecordsEndpoints extends WalletStateEndpoints {

  val createEndpoint =
    Endpoint(Method.POST / "api" / "records")
      .auth(AuthType.Bearer)
      .in[Record.Data]
      .out[Record.Full](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val listEndpoint =
    Endpoint(Method.GET / "api" / "records")
      .auth(AuthType.Bearer)
      .query[Account.Id](Account.Id.query)
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.Full]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val getEndpoint =
    Endpoint(Method.GET / "api" / "records" / Record.Id.path)
      .auth(AuthType.Bearer)
      .out[Record.Full]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val updateEndpoint =
    Endpoint(Method.PUT / "api" / "records" / Record.Id.path)
      .auth(AuthType.Bearer)
      .in[Record.Data]
      .out[Record.Full]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  val deleteEndpoint =
    Endpoint(Method.DELETE / "api" / "records" / Record.Id.path)
      .auth(AuthType.Bearer)
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )
      .withBadRequestCodec

  override val endpointsMap = Map(
    "create" -> createEndpoint,
    "get"    -> getEndpoint,
    "update" -> updateEndpoint,
    "delete" -> deleteEndpoint,
    "list"   -> listEndpoint
  )

  override val endpoints = Chunk(
    createEndpoint,
//    list, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    getEndpoint,
    updateEndpoint,
    deleteEndpoint
  )

}

object RecordsEndpoints extends RecordsEndpoints
