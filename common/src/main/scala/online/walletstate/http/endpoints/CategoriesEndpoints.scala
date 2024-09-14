package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError.{BadRequest, InternalServerError, NotFound, Unauthorized}
import online.walletstate.common.models.{Category, Grouped}
import zio.Chunk
import zio.http.codec.HttpCodec
import zio.http.endpoint.Endpoint
import zio.http.{Method, Status}

trait CategoriesEndpoints extends WalletStateEndpoints {

  override protected final val tag: String = "Categories"

  val createEndpoint =
    Endpoint(Method.POST / "api" / "categories").walletStateEndpoint
      .in[Category.Data]
      .out[Category](Status.Created)
      .outErrors[BadRequest | Unauthorized | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val listEndpoint =
    Endpoint(Method.GET / "api" / "categories").walletStateEndpoint
      .out[List[Category]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val listGroupedEndpoint =
    Endpoint(Method.GET / "api" / "categories" / "grouped").walletStateEndpoint
      .out[List[Grouped[Category]]]
      .outErrors[Unauthorized | InternalServerError](
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val getEndpoint =
    Endpoint(Method.GET / "api" / "categories" / Category.Id.path).walletStateEndpoint
      .out[Category]
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  val updateEndpoint =
    Endpoint(Method.PUT / "api" / "categories" / Category.Id.path).walletStateEndpoint
      .in[Category.Data]
      .out[Unit](Status.NoContent)
      .outErrors[BadRequest | Unauthorized | NotFound | InternalServerError](
        HttpCodec.error[BadRequest](Status.BadRequest),
        HttpCodec.error[Unauthorized](Status.Unauthorized),
        HttpCodec.error[NotFound](Status.NotFound),
        HttpCodec.error[InternalServerError](Status.InternalServerError)
      )

  override val endpointsMap = Map(
    "create"      -> createEndpoint,
    "get"         -> getEndpoint,
    "update"      -> updateEndpoint,
    "list"        -> listEndpoint,
    "listGrouped" -> listGroupedEndpoint
  )

  override val endpoints = Chunk(
    createEndpoint,
    getEndpoint,
    updateEndpoint,
    listEndpoint
    //    listGrouped, // java.util.NoSuchElementException: None.get https://github.com/zio/zio-http/issues/2767
  )
}

object CategoriesEndpoints extends CategoriesEndpoints
