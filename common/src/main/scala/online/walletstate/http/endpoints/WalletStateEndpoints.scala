package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError
import zio.Chunk
import zio.http.{Header, MediaType, Status}
import zio.http.codec.{BinaryCodecWithSchema, HttpCodec, HttpCodecError, HttpContentCodec}
import zio.http.codec.HttpCodecError.CustomError
import zio.http.endpoint.EndpointMiddleware.Typed
import zio.http.endpoint.{Endpoint, EndpointMiddleware}
import zio.schema.Schema

trait WalletStateEndpoints {

  def endpointsMap: Map[String, Endpoint[_, _, _, _, _]] = Map.empty
  def endpoints: Chunk[Endpoint[_, _, _, _, _]]          = Chunk.from(endpointsMap.values)

  protected val EndpointAuthorization: Typed[Header.Authorization, Nothing, Unit] = EndpointMiddleware.auth

  // Workaround to not loss empty lists fields: https://github.com/zio/zio-http/issues/2911
  implicit def customCodec[T: Schema]: HttpContentCodec[T] = HttpContentCodec.from(
    MediaType.application.`json` -> BinaryCodecWithSchema.fromBinaryCodec(
      zio.schema.codec.JsonCodec.schemaBasedBinaryCodec[T](
        zio.schema.codec.JsonCodec.Config(ignoreEmptyCollections = false)
      )
    )
  ) // ++ HttpContentCodec.byteChunkCodec ++ HttpContentCodec.byteCodec

  extension [PathInput, Input, Err, Output, Middleware <: EndpointMiddleware](
      endpoint: Endpoint[PathInput, Input, Err, Output, Middleware]
  ) {
    protected def withBadRequestCodec: Endpoint[PathInput, Input, Err, Output, Middleware] =
      endpoint
        .outCodecError(
          HttpCodec
            .error[HttpError.BadRequest](Status.BadRequest)
            .transform(e => CustomError(e.error, e.message)) {
              case e: CustomError    => HttpError.BadRequest(e.name, e.message)
              case e: HttpCodecError => HttpError.BadRequest(e.getClass.getSimpleName.replace("$", ""), e.message)
            }
        )
  }

}
