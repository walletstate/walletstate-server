package online.walletstate.http.api

import online.walletstate.models.{AppError, Category}
import online.walletstate.models.api.{CreateCategory, Grouped, UpdateCategory}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait CategoriesEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "categories")
      .in[CreateCategory]
      .out[Category](Status.Created)
      .outError[AppError.Unauthorized](Status.Unauthorized)
      .outError[AppError.BadRequest](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "categories")
      .out[List[Category]]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val listGrouped =
    Endpoint(Method.GET / "api" / "categories" / "grouped")
      .out[List[Grouped[Category]]]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val get =
    Endpoint(Method.GET / "api" / "categories" / Category.Id.path)
      .out[Category]
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val update =
    Endpoint(Method.PUT / "api" / "categories" / Category.Id.path)
      .in[UpdateCategory]
      .out[Category](Status.Ok)
      .outError[AppError.BadRequest](Status.BadRequest)
      .outError[AppError.Unauthorized](Status.Unauthorized)

  val endpointsMap = Map(
    "create"      -> create,
    "get"         -> get,
    "update"      -> update,
    "list"        -> list,
    "listGrouped" -> listGrouped
  )

  val endpoints = Chunk(
    create,
    get,
    update,
    list
    //    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
  )
}

object CategoriesEndpoints extends CategoriesEndpoints
