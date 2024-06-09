package online.walletstate.http.endpoints

import online.walletstate.models.{HttpError, Group}
import zio.http.codec.{Doc, QueryCodec}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait GroupsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "groups")
      .in[Group.CreateData]
      .out[Group](Status.Created)
      .withBadRequestCodec

  val list =
    Endpoint(Method.GET / "api" / "groups")
      .query(QueryCodec.query("groupType").transformOrFailLeft[Group.Type](Group.Type.fromString)(Group.Type.asString))
      .out[List[Group]]
      .withBadRequestCodec

  val get =
    Endpoint(Method.GET / "api" / "groups" / Group.Id.path)
      .out[Group]
      .outError[HttpError.NotFound](HttpError.NotFound.status)
      .withBadRequestCodec

  val update =
    Endpoint(Method.PUT / "api" / "groups" / Group.Id.path)
      .in[Group.UpdateData](Doc.h1("Test doc"))
      .out[Unit](Status.NoContent)
      .withBadRequestCodec

  val delete =
    Endpoint(Method.DELETE / "api" / "groups" / Group.Id.path)
      .out[Unit](Status.NoContent)
      .withBadRequestCodec

  override val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list,
    "update" -> update,
    "delete" -> delete
  )
}

object GroupsEndpoints extends GroupsEndpoints
