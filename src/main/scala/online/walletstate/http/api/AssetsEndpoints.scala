package online.walletstate.http.api

import online.walletstate.models.{AppError, Asset}
import online.walletstate.models.api.CreateAsset
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait AssetsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "assets")
      .in[CreateAsset]
      .out[Asset](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "assets")
      .out[List[Asset]]
      .outError[AppError.Unauthorized ](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val get =
    Endpoint(Method.GET / "api" / "assets" / Asset.Id.path)
      .out[Asset]
      .outError[AppError.Unauthorized ](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val endpointsMap = Map(
    "create" -> create,
    "get"    -> get,
    "list"   -> list
  )

  val endpoints = endpointsMap.values
}

object AssetsEndpoints extends AssetsEndpoints