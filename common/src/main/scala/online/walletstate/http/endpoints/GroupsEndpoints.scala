package online.walletstate.http.endpoints

import online.walletstate.common.models.Group
import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import zio.http.codec.{Doc, HttpCodec, QueryCodec}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait GroupsEndpoints extends WalletStateEndpoints {

  override protected final val tag: String = "Groups"

  val createEndpoint =
    Endpoint(Method.POST / "api" / "groups").walletStateEndpoint
      .in[Group.CreateData]
      .out[Group](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val listEndpoint =
    Endpoint(Method.GET / "api" / "groups").walletStateEndpoint
      .query(
        QueryCodec
          .query[String]("groupType")
          .transformOrFailLeft[Group.Type](Group.Type.fromString)(Group.Type.asString)
      )
      .out[List[Group]]
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val getEndpoint =
    Endpoint(Method.GET / "api" / "groups" / Group.Id.path).walletStateEndpoint
      .out[Group]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val updateEndpoint =
    Endpoint(Method.PUT / "api" / "groups" / Group.Id.path).walletStateEndpoint
      .in[Group.UpdateData](Doc.h1("Test doc"))
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val deleteEndpoint =
    Endpoint(Method.DELETE / "api" / "groups" / Group.Id.path).walletStateEndpoint
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
    "list"   -> listEndpoint,
    "update" -> updateEndpoint,
    "delete" -> deleteEndpoint
  )
}

object GroupsEndpoints extends GroupsEndpoints
