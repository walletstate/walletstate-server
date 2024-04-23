package online.walletstate.http.api.endpoints

import online.walletstate.models.Category
import online.walletstate.models.api.{CreateCategory, Grouped}
import online.walletstate.models.errors.{BadRequestError, UnauthorizedError}
import zio.Chunk
import zio.http.{Method, Status}
import zio.http.endpoint.Endpoint

trait CategoriesEndpoints {

  val create =
    Endpoint(Method.POST / "api" / "categories")
      .in[CreateCategory]
      .out[Category](Status.Created)
      .outError[UnauthorizedError.type](Status.Unauthorized)
      .outError[BadRequestError](Status.BadRequest)

  val list =
    Endpoint(Method.GET / "api" / "categories")
      .out[Chunk[Category]]
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val listGrouped =
    Endpoint(Method.GET / "api" / "categories" / "grouped")
      .out[Chunk[Grouped[Category]]]
      .outError[UnauthorizedError.type](Status.Unauthorized)

  val get =
    Endpoint(Method.GET / "api" / "categories" / Category.Id.path)
      .out[Category]
      .outError[UnauthorizedError.type](Status.Unauthorized)
  
  val endpointsMap = Map(
    "create" -> create,
    "get" -> get,
    "list" -> list,
    "listGrouped" -> listGrouped
  )
 
  val endpoints = Chunk(
    create,
    get,
    list
    //    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
  )
}

object CategoriesEndpoints extends CategoriesEndpoints
