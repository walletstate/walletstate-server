package online.walletstate.http.endpoints

import online.walletstate.models.HttpError
import zio.Chunk
import zio.http.codec.{HttpCodec, HttpCodecError}
import zio.http.codec.HttpCodecError.CustomError
import zio.http.endpoint.{Endpoint, EndpointMiddleware}

trait WalletStateEndpoints {

  def endpointsMap: Map[String, Endpoint[_, _, _, _, _]] = Map.empty
  def endpoints: Chunk[Endpoint[_, _, _, _, _]]          = Chunk.from(endpointsMap.values)

  extension [PathInput, Input, Err, Output, Middleware <: EndpointMiddleware](
      endpoint: Endpoint[PathInput, Input, Err, Output, Middleware]
  ) {
    def withBadRequestCodec: Endpoint[PathInput, Input, Err, Output, Middleware] =
      endpoint
        .outCodecError(
          HttpCodec
            .error[HttpError.BadRequest](HttpError.BadRequest.status)
            .transform(e => CustomError(e.error, e.message)) {
              case e: CustomError    => HttpError.BadRequest(e.name, e.message)
              case e: HttpCodecError => HttpError.BadRequest(e.getClass.getSimpleName.replace("$", ""), e.message)
            }
        )
  }

}
