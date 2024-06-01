package online.walletstate.http.endpoints

import online.walletstate.models.{AppError, Group}
import zio.http.codec.{Doc, QueryCodec}
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait GroupsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "groups")
      .in[Group.CreateData]
      .out[Group](Status.Created)

  val list =
    Endpoint(Method.GET / "api" / "groups")
      .query(QueryCodec.query("groupType").transformOrFailLeft[Group.Type](Group.Type.fromString)(Group.Type.asString))
      .out[List[Group]]

  val get =
    Endpoint(Method.GET / "api" / "groups" / Group.Id.path)
      .out[Group]
      .outError[AppError.GroupNotExist](Status.NotFound)

  val update =
    Endpoint(Method.PUT / "api" / "groups" / Group.Id.path)
      .in[Group.UpdateData](Doc.h1("Test doc"))
      .out[Unit](Status.NoContent)

  val delete =
    Endpoint(Method.DELETE / "api" / "groups" / Group.Id.path)
      .out[Unit](Status.NoContent)

  override val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list,
    "update" -> update,
    "delete" -> delete
  )
}

object GroupsEndpoints extends GroupsEndpoints
