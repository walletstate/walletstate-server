package online.walletstate.http.api.endpoints

import online.walletstate.models.Group
import online.walletstate.models.api.{CreateGroup, UpdateGroup}
import online.walletstate.models.errors.{BadRequestError, UnauthorizedError}
import zio.Chunk
import zio.http.codec.Doc
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait GroupsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "groups" / Group.Type.path)
      .in[CreateGroup]
      .out[Group](Status.Created)
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "groups" / Group.Type.path)
      .out[Chunk[Group]]
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val get =
    Endpoint(Method.GET / "api" / "groups" / Group.Type.path / Group.Id.path)
      .out[Group]
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val update =
    Endpoint(Method.PUT / "api" / "groups" / Group.Type.path / Group.Id.path)
      .in[UpdateGroup](Doc.h1("Test doc"))
      .out[Unit](Status.NoContent)
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val delete =
    Endpoint(Method.DELETE / "api" / "groups" / Group.Type.path / Group.Id.path)
      .out[Unit](Status.NoContent)
      .outError[UnauthorizedError.type](Status.Unauthorized)
  
  val endpointsMap = Map(
    "create" -> create,
    "get" -> get,
    "list" -> list,
    "update" -> update,
    "delete" -> delete
  )

  val endpoints = endpointsMap.values
}

object GroupsEndpoints extends GroupsEndpoints