package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Account, HttpError, Page, Record}
import zio.Chunk
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait RecordsEndpoints extends WalletStateEndpoints {

  override protected final val tag: String = "Records"

  val createEndpoint =
    Endpoint(Method.POST / "api" / "records").walletStateEndpoint
      .in[Record.Data]
      .out[Record.Full](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val listEndpoint =
    Endpoint(Method.GET / "api" / "records").walletStateEndpoint
      .query[Account.Id](Account.Id.query)
      .query[Option[Page.Token]](Page.Token.queryCodec.optional)
      .out[Page[Record.Full]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val getEndpoint =
    Endpoint(Method.GET / "api" / "records" / Record.Id.path).walletStateEndpoint
      .out[Record.Full]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val updateEndpoint =
    Endpoint(Method.PUT / "api" / "records" / Record.Id.path).walletStateEndpoint
      .in[Record.Data]
      .out[Record.Full]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val deleteEndpoint =
    Endpoint(Method.DELETE / "api" / "records" / Record.Id.path).walletStateEndpoint
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

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
