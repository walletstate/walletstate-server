package online.walletstate.utils

import online.walletstate.models.AppError
import online.walletstate.utils.RequestOps.outputMediaType
import zio.http.*
import zio.http.Header.Accept.MediaTypeWithQFactor
import zio.http.codec.{HttpCodec, HttpCodecError}
import zio.http.endpoint.Endpoint
import zio.{NonEmptyChunk, Trace, ZIO, Zippable}

import scala.reflect.ClassTag

/** Workaround to provide auth context to Endpoint implementation
  */
object EndpointOps {
  
  extension [EPatIn, EIn, EErr: ClassTag, EOut](endpoint: Endpoint[EPatIn, EIn, EErr, EOut, _])
    def implementWithCtx[Ctx, HIn](
        ctxMiddleware: HandlerAspect[Any, Ctx]
    )(
        handler: Handler[Any, Any, HIn, EOut]
    )(
        errorMapper: PartialFunction[Any, EErr] = PartialFunction.empty
    )(using z: Zippable.Out[EIn, Ctx, HIn], trace: Trace): Route[Any, Any] = {
      endpoint.route -> ctxMiddleware -> Handler.fromFunctionZIO[(EPatIn, Ctx, Request)] { (_, ctx, req) =>
        
        val response = for {
          input    <- endpoint.input.decodeRequest(req).absorb
          result   <- handler(z.zip(input, ctx))
          response <- ZIO.attempt(endpoint.output.encodeResponse(result, req.outputMediaType))
        } yield response

        response
          .mapError(errorMapper.orElse(e => e))
          .catchSome {
            case error: EErr =>
              ZIO.succeed(endpoint.error.encodeResponse(error, req.outputMediaType))
            case error: HttpCodecError =>
              ZIO.succeed(AppError.BadRequestCodec.encodeResponse(AppError.BadRequest(error.message), req.outputMediaType))
            case e: AppError.Unauthorized =>
              ZIO.succeed(AppError.UnauthorizedCodec.encodeResponse(e, req.outputMediaType))
          }
          .catchAll { _ => ZIO.succeed(Response.internalServerError) }
      }
    }

}
