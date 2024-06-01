package online.walletstate.http.endpoints

import online.walletstate.models.{AppError, Category, Grouped}
import zio.Chunk
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait CategoriesEndpoints extends WalletStateEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "categories")
      .in[Category.Data]
      .out[Category](Status.Created)

  val list =
    Endpoint(Method.GET / "api" / "categories")
      .out[List[Category]]

  val listGrouped =
    Endpoint(Method.GET / "api" / "categories" / "grouped")
      .out[List[Grouped[Category]]]

  val get =
    Endpoint(Method.GET / "api" / "categories" / Category.Id.path)
      .out[Category]
      .outError[AppError.CategoryNotExist](Status.NotFound)

  val update =
    Endpoint(Method.PUT / "api" / "categories" / Category.Id.path)
      .in[Category.Data]
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
    list
    //    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
  )
}

object CategoriesEndpoints extends CategoriesEndpoints
