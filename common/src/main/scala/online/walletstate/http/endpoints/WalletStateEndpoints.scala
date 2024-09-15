package online.walletstate.http.endpoints

import online.walletstate.common.models.HttpError
import zio.http.codec.*
import zio.http.codec.HttpCodecError.CustomError
import zio.http.endpoint.{AuthType, Endpoint}
import zio.http.{Header, MediaType, Status}
import zio.schema.Schema
import zio.{Chunk, Config}

trait WalletStateEndpoints {
  import WalletStateEndpoints.Auth.*

  protected def tag: String

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

  extension [PathInput, Input, Err, Output, Auth <: AuthType](
      endpoint: Endpoint[PathInput, Input, Err, Output, Auth]
  ) {
    protected def walletStateEndpoint: Endpoint[PathInput, Input, Err, Output, AppAuthType] =
      endpoint
        .tag(tag)
        .auth(auth)
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

object WalletStateEndpoints {

  object Auth {
    type ClientAuthRequirement = Either[Header.Authorization.Bearer, Header.Cookie]
    type AppAuthType           = AuthType { type ClientRequirement = ClientAuthRequirement }

    val auth: AppAuthType = AuthType.Bearer | AuthType.Custom(HeaderCodec.cookie)

    def bearer(token: String): ClientAuthRequirement        = Left(Header.Authorization.Bearer(token))
    def bearer(token: Config.Secret): ClientAuthRequirement = Left(Header.Authorization.Bearer(token))
  }
}
