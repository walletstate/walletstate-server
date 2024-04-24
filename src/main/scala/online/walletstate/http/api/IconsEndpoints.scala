package online.walletstate.http.api

import online.walletstate.models.api.CreateIcon
import online.walletstate.models.{AppError, Icon}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait IconsEndpoints {

  val list =
    Endpoint(Method.GET / "api" / "icons")
      .out[List[Icon.Id]]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val create =
    Endpoint(Method.POST / "api" / "icons")
      .in[CreateIcon]
      .out[Icon.Id](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val endpointsMap = Map("list" -> list, "create" -> create)
  val endpoints    = endpointsMap.values
}

object IconsEndpoints extends IconsEndpoints
