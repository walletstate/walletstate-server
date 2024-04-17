package online.walletstate.http.api.endpoints

import online.walletstate.models.Asset
import online.walletstate.models.api.CreateAsset
import online.walletstate.models.errors.{BadRequestError, UnauthorizedError}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait AssetsEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "assets")
      .in[CreateAsset]
      .out[Asset](Status.Created)
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "assets")
      .out[Chunk[Asset]]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val get =
    Endpoint(Method.GET / "api" / "assets" / Asset.Id.path)
      .out[Asset]
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val endpoints = Chunk(create, list, get)
}

object AssetsEndpoints extends AssetsEndpoints
