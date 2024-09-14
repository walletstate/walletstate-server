package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError
import zio.Chunk
import zio.http.Header.Authorization
import zio.http.codec.HttpCodecError.CustomError
import zio.http.codec.{BinaryCodecWithSchema, HeaderCodec, HttpCodec, HttpCodecError, HttpContentCodec}
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Header, MediaType, Status}
import zio.schema.Schema

trait WalletStateEndpoints {

  def endpointsMap: Map[String, Endpoint[_, _, _, _, _]] = Map.empty
  def endpoints: Chunk[Endpoint[_, _, _, _, _]]          = Chunk.from(endpointsMap.values)

  // Workaround to not loss empty lists fields: https://github.com/zio/zio-http/issues/2911
  implicit def customCodec[T: Schema]: HttpContentCodec[T] = HttpContentCodec.from(
    MediaType.application.`json` -> BinaryCodecWithSchema.fromBinaryCodec(
      zio.schema.codec.JsonCodec.schemaBasedBinaryCodec[T](
        zio.schema.codec.JsonCodec.Config(ignoreEmptyCollections = false)
      )
    )
  ) // ++ HttpContentCodec.byteChunkCodec ++ HttpContentCodec.byteCodec

  type WalletStateAuthType = AuthType { type ClientRequirement = Either[Authorization.Bearer, Header.Cookie] }
  val walletStateAuth: WalletStateAuthType = AuthType.Bearer | AuthType.Custom(HeaderCodec.cookie)

  extension [PathInput, Input, Err, Output, Auth <: AuthType](
      endpoint: Endpoint[PathInput, Input, Err, Output, Auth]
  ) {
    protected def withAuth: Endpoint[PathInput, Input, Err, Output, WalletStateAuthType] =
      endpoint.auth(walletStateAuth)

    protected def withBadRequestCodec: Endpoint[PathInput, Input, Err, Output, Auth] =
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
