package online.walletstate.http.endpoints

import online.walletstate.models.api.{CreateAsset, Grouped, UpdateAsset}
import online.walletstate.models.{AppError, Asset}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait AssetsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "assets")
      .in[CreateAsset]
      .out[Asset](Status.Created)

  val listGrouped =
    Endpoint(Method.GET / "api" / "assets" / "grouped")
      .out[List[Grouped[Asset]]]

  val list =
    Endpoint(Method.GET / "api" / "assets")
      .out[List[Asset]]

  val get =
    Endpoint(Method.GET / "api" / "assets" / Asset.Id.path)
      .out[Asset]
      .outError[AppError.AssetNotExist](Status.NotFound)

  val update =
    Endpoint(Method.PUT / "api" / "assets" / Asset.Id.path)
      .in[UpdateAsset]
      .out[Unit](Status.NoContent)

  override val endpointsMap = Map(
    "create"      -> create,
    "get"         -> get,
    "update"      -> update,
    "list"        -> list,
    "listGrouped" -> listGrouped
  )

  override val endpoints = Chunk(
    create,
    get,
    update,
//    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
    list
  )
}

object AssetsEndpoints extends AssetsEndpoints
