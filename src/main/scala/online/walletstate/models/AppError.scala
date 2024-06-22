package online.walletstate.models

import scala.util.control.NoStackTrace

trait AppError(msg: String) extends Throwable with NoStackTrace {
  val message: String   = msg
  def errorName: String = this.getClass.getSimpleName.replace("$", "")
}

object AppError {

  case class ParseRequestError(msg: String) extends AppError(msg)

  case object NoToken                      extends AppError("Auth token not present in request")
  case class TokenDecodeError(msg: String) extends AppError(msg)
  object TokenDecodeError {
    val contentMalformed: TokenDecodeError = TokenDecodeError("Cannot extract content from token")

    def invalidTokenType(expected: AuthContext.Type, actual: AuthContext.Type): TokenDecodeError =
      TokenDecodeError(s"Incorrect token type. Expected $expected token type but provided $actual token type")
  }

  /////////// wallet errors
  case class WalletNotExist() extends AppError("Wallet not exist")

  /////////// wallet invite errors
  case class WalletInviteNotExist() extends AppError("Wallet invite not exist")
  case class WalletInviteExpired()  extends AppError("Wallet invite expired")

  /////////// auth errors
  case class InvalidCredentials() extends AppError("Invalid credentials. Please try again")

  case class UserIsNotInWallet(user: User.Id, wallet: Wallet.Id)
      extends AppError(s"User $user doesn't have access to wallet $wallet ")

  /////////// user errors
  case class UserNotExist() extends AppError("")

  /////////// groups errors
  case class GroupNotExist() extends AppError("")

  /////////// accounts errors
  case class AccountNotExist() extends AppError("Account not found")

  /////////// categories errors
  case class CategoryNotExist() extends AppError("")

  /////////// records errors
  case class RecordNotExist() extends AppError("Transaction not found")

  case class InvalidTransactionInfo(msg: String) extends AppError(msg)

  case class InvalidPageToken(error: String) extends AppError(s"Invalid next page token: $error")

  /////////// assets errors
  case class AssetNotExist() extends AppError("")

  /////////// exchange rates errors
  case class ExchangeRateNotExist() extends AppError("")

  /////////// icons
  case class InvalidIconId(msg: String) extends AppError(msg)
  case class IconNotExist(id: Icon.Id)  extends AppError(s"Icon with ${id.id} not found")

}
