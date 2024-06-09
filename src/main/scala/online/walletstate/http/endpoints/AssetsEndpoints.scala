package online.walletstate.http.endpoints

import online.walletstate.models.{HttpError, Asset, Grouped}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait AssetsEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "assets")
      .in[Asset.Data]
      .out[Asset](Status.Created)
      .withBadRequestCodec

  val listGrouped =
    Endpoint(Method.GET / "api" / "assets" / "grouped")
      .out[List[Grouped[Asset]]]
      .withBadRequestCodec

  val list =
    Endpoint(Method.GET / "api" / "assets")
      .out[List[Asset]]
      .withBadRequestCodec

  val get =
    Endpoint(Method.GET / "api" / "assets" / Asset.Id.path)
      .out[Asset]
      .outError[HttpError.NotFound](HttpError.NotFound.status)
      .withBadRequestCodec

  val update =
    Endpoint(Method.PUT / "api" / "assets" / Asset.Id.path)
      .in[Asset.Data]
      .out[Unit](Status.NoContent)
      .withBadRequestCodec

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
