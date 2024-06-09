package online.walletstate.http.endpoints

import online.walletstate.models.{HttpError, Category, Grouped}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait CategoriesEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "categories")
      .in[Category.Data]
      .out[Category](Status.Created)
      .withBadRequestCodec

  val list =
    Endpoint(Method.GET / "api" / "categories")
      .out[List[Category]]
      .withBadRequestCodec

  val listGrouped =
    Endpoint(Method.GET / "api" / "categories" / "grouped")
      .out[List[Grouped[Category]]]
      .withBadRequestCodec

  val get =
    Endpoint(Method.GET / "api" / "categories" / Category.Id.path)
      .out[Category]
      .outError[HttpError.NotFound](HttpError.NotFound.status)
      .withBadRequestCodec

  val update =
    Endpoint(Method.PUT / "api" / "categories" / Category.Id.path)
      .in[Category.Data]
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
    list
    //    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
  )
}

object CategoriesEndpoints extends CategoriesEndpoints
