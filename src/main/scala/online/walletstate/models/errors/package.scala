package online.walletstate.models

import zio.http.Status
import zio.schema.{DeriveSchema, Schema}

package object errors {

  trait BaseErrorBody(message: String) extends AppError

  final case class BadRequestError(message: String) extends BaseErrorBody(message)
  object BadRequestError {
    given schema: Schema[BadRequestError] = DeriveSchema.gen[BadRequestError]
  }

  case object UnauthorizedError extends BaseErrorBody("User is not authorized") {
    given schema: Schema[UnauthorizedError.type] = Schema.singleton(this)
  }

  abstract class BadRequestError2(msg: String) extends AppError with ToResponse(Status.BadRequest, msg)
  abstract class NotFoundError(msg: String)    extends AppError with ToResponse(Status.NotFound, msg)

  /////////// auth errors
  case object InvalidCredentials extends AppError with ToResponse(Status.Unauthorized, "Invalid credentials")

  case object AuthTokenNotFound extends AppError with ToResponse(Status.Unauthorized, "Request without auth token")
  case class InvalidAuthToken(msg: String) extends AppError with ToResponse(Status.Unauthorized, msg)
  case class InvalidAuthContext(context: String)
      extends AppError
      with ToResponse(Status.Unauthorized, s"Cannot extract $context from auth token.")

  /////////// user errors
  case object UserNotExist extends AppError with ToResponse(Status.NotFound, "User not found")

  /////////// wallet errors
  case object WalletNotExist extends AppError

  case object UserAlreadyHasWallet // Allow only one wallet for user for now
      extends AppError
      with ToResponse(Status.Forbidden, "User already has the wallet")

  /////////// wallet invite errors
  case object WalletInviteNotExist
      extends AppError
      with ToResponse(Status.Forbidden, "Invite code not found. Cannot join the wallet.")

  case object WalletInviteExpired
      extends AppError
      with ToResponse(Status.Forbidden, "Invite code expired. Please ask the wallet owner to generate a new one")

  /////////// accounts groups errors
  case object AccountsGroupNotExist extends AppError with ToResponse(Status.NotFound, "Accounts group not found")
  case object CanNotDeleteAccountsGroup
      extends AppError
      with ToResponse(
        Status.BadRequest,
        "Cannot delete group with accounts. Remove accounts or move to another group first"
      )

  /////////// accounts errors
  case class AccountNotExist() extends BaseErrorBody("Account not found")
  object AccountNotExist {
    given schema: Schema[AccountNotExist] = DeriveSchema.gen[AccountNotExist]
  }

  /////////// categories errors
  case object CategoryNotExist extends AppError with ToResponse(Status.NotFound, "Category not found")

  /////////// transactions errors
  case object TransactionNotExist                extends NotFoundError("Transaction not found")
  case class InvalidTransactionInfo(msg: String) extends BadRequestError2(msg)
  case class InvalidPageToken(error: String)     extends BadRequestError2(s"Invalid next page token: $error")

  /////////// assets errors
  case object AssetNotExist extends AppError with ToResponse(Status.NotFound, "Asset not found")

  /////////// exchange rates errors
  case object ExchangeRateNotExist extends AppError with ToResponse(Status.NotFound, "Exchange rate not found")

  /////////// icons
  case class InvalidIconId(msg: String) extends BadRequestError2(msg)
}
