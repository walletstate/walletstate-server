package online.walletstate.http.api

import online.walletstate.models.{AppError, Group}
import online.walletstate.models.api.{CreateGroup, UpdateGroup}
import zio.Chunk
import zio.http.codec.{Doc, QueryCodec}
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait GroupsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "groups")
      .in[CreateGroup]
      .out[Group](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "groups")
      .query(QueryCodec.query("groupType").transformOrFailLeft[Group.Type](Group.Type.fromString)(Group.Type.asString))
      .out[List[Group]]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val get =
    Endpoint(Method.GET / "api" / "groups" / Group.Id.path)
      .out[Group]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val update =
    Endpoint(Method.PUT / "api" / "groups" / Group.Id.path)
      .in[UpdateGroup](Doc.h1("Test doc"))
      .out[Unit](Status.NoContent)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val delete =
    Endpoint(Method.DELETE / "api" / "groups" / Group.Id.path)
      .out[Unit](Status.NoContent)
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list,
    "update" -> update,
    "delete" -> delete
  )

  val endpoints = endpointsMap.values
}

object GroupsEndpoints extends GroupsEndpoints
