package online.walletstate.models

import zio.{NonEmptyChunk, ZIO}
import zio.http.Header.Accept.MediaTypeWithQFactor
import zio.http.{Response, Status}
import zio.http.codec.HttpCodecType.Content
import zio.http.codec.{HttpCodec, HttpCodecType}
import zio.schema.{DeriveSchema, Schema}

import scala.util.control.NoStackTrace

trait AppError extends Throwable with NoStackTrace {
  val message: String

  def encode(status: Status, mediaType: NonEmptyChunk[MediaTypeWithQFactor]): ZIO[Any, Nothing, Response] =
    ZIO.succeed(AppError.httpCodec(status).encodeResponse(this, mediaType))
}

object AppError {
  final case class ErrorBody(error: String, message: String)
  object ErrorBody {
    given schema: Schema[ErrorBody] = DeriveSchema.gen[ErrorBody]
  }

  given [T <: AppError]: Schema[T] = AppError.ErrorBody.schema.transformOrFail(
    body => Left("Backward error transformation not implemented"),
    appError => Right(AppError.ErrorBody(appError.getClass.getSimpleName.replace("$", ""), appError.message))
  )

  type AppErrorCodec[T <: AppError] = HttpCodec[HttpCodecType.Status with Content, T]

  def httpCodec[T <: AppError](status: Status): AppErrorCodec[T] = HttpCodec.error[T](status)

  val BadRequestCodec: AppErrorCodec[BadRequest]                        = httpCodec(Status.BadRequest)
  val UnauthorizedCodec: AppErrorCodec[Unauthorized]                    = httpCodec(Status.Unauthorized)
  val InternalServerErrorCodec: AppErrorCodec[InternalServerError.type] = httpCodec(Status.InternalServerError)

  trait AppErrorWithMsg(msg: String) extends AppError { val message: String = msg }

  case object InternalServerError           extends AppErrorWithMsg("Oops something went wrong")
  case class BadRequest(msg: String)        extends AppErrorWithMsg(msg)
  case class ParseRequestError(msg: String) extends AppErrorWithMsg(msg)

  case class Unauthorized(msg: String) extends AppError { val message: String = msg }
  object Unauthorized {
    val authTokenNotFound                   = Unauthorized("Auth token not found in request")
    def invalidAuthToken(msg: String)       = Unauthorized(s"Invalid token: $msg")
    def invalidAuthContext(context: String) = Unauthorized(s"Invalid auth context: $context")
  }

  /////////// wallet errors
  case class WalletNotExist() extends AppErrorWithMsg("Wallet not exist")

  /////////// wallet invite errors
  case class WalletInviteNotExist() extends AppErrorWithMsg("Wallet invite not exist")
  case class WalletInviteExpired()  extends AppErrorWithMsg("Wallet invite expired")

  /////////// auth errors
  case class InvalidCredentials() extends AppErrorWithMsg("Invalid credentials. Please try again")

  case class UserIsNotInWallet(user: User.Id, wallet: Wallet.Id)
      extends AppErrorWithMsg(s"User $user doesn't have access to wallet $wallet ")

  /////////// user errors
  case class UserNotExist() extends AppErrorWithMsg("")

  /////////// groups errors
  case class GroupNotExist() extends AppErrorWithMsg("")

  /////////// accounts errors
  case class AccountNotExist() extends AppErrorWithMsg("Account not found")

  /////////// categories errors
  case class CategoryNotExist() extends AppErrorWithMsg("")

  /////////// records errors
  case class RecordNotExist() extends AppErrorWithMsg("Transaction not found")

  case class InvalidTransactionInfo(msg: String) extends AppErrorWithMsg(msg)

  case class InvalidPageToken(error: String) extends AppErrorWithMsg(s"Invalid next page token: $error")

  /////////// assets errors
  case class AssetNotExist() extends AppErrorWithMsg("")

  /////////// exchange rates errors
  case class ExchangeRateNotExist() extends AppErrorWithMsg("")

  /////////// icons
  case class InvalidIconId(msg: String) extends AppErrorWithMsg(msg)
  case class IconNotExist(id: Icon.Id)  extends AppErrorWithMsg(s"Icon with ${id.id} not found")

}
