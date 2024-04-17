package online.walletstate.http.auth

import online.walletstate.models.errors.{BadRequestError, UnauthorizedError}
import zio.http.*
import zio.http.Header.Accept.MediaTypeWithQFactor
import zio.http.codec.{HttpCodec, HttpCodecError}
import zio.http.endpoint.Endpoint
import zio.{NonEmptyChunk, Trace, ZIO, Zippable}

import scala.reflect.ClassTag

/**
 * Workaround to provide auth context to Endpoint implementation
 */
object EndpointOps {

  private val defaultMediaTypes = NonEmptyChunk(MediaTypeWithQFactor(MediaType.application.`json`, Some(1)))

  private[auth] val badRequestCodec   = HttpCodec.error[BadRequestError](Status.BadRequest)
  private[auth] val unauthorizedCodec = HttpCodec.error[UnauthorizedError.type](Status.Unauthorized)

  extension [EPatIn, EIn, EErr: ClassTag, EOut](endpoint: Endpoint[EPatIn, EIn, EErr, EOut, _])
    def implementWithCtx[Ctx, HIn](
        ctxMiddleware: HandlerAspect[Any, Ctx]
    )(
        handler: Handler[Any, Any, HIn, EOut]
    )(
        errorMapper: PartialFunction[Any, EErr] = PartialFunction.empty
    )(using z: Zippable.Out[EIn, Ctx, HIn], trace: Trace): Route[Any, Any] = {
      endpoint.route -> ctxMiddleware -> Handler.fromFunctionZIO[(EPatIn, Ctx, Request)] { (_, ctx, req) =>

        val outMediaTypes =
          NonEmptyChunk.fromChunk(req.headers.getAll(Header.Accept).flatMap(_.mimeTypes)).getOrElse(defaultMediaTypes)

        val response = for {
          input    <- endpoint.input.decodeRequest(req).absorb
          result   <- handler(z.zip(input, ctx))
          response <- ZIO.attempt(endpoint.output.encodeResponse(result, outMediaTypes))
        } yield response

        response
          .mapError(errorMapper.orElse(e => e))
          .catchSome {
            case error: EErr =>
              ZIO.succeed(endpoint.error.encodeResponse(error, outMediaTypes))
            case error: HttpCodecError =>
              ZIO.succeed(badRequestCodec.encodeResponse(BadRequestError(error.message), outMediaTypes))
            case UnauthorizedError =>
              ZIO.succeed(unauthorizedCodec.encodeResponse(UnauthorizedError, outMediaTypes))
          }
          .catchAll { _ => ZIO.succeed(Response.internalServerError) }
      }
    }

}
