package online.walletstate.models

import zio.http.Status

package object errors {

  /////////// auth errors
  case object InvalidCredentials extends AppError with ToResponse(Status.Unauthorized, "Invalid credentials")

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

  /////////// accounts errors
  case object AccountNotExist extends AppError with ToResponse(Status.NotFound, "Account not found")

  /////////// categories errors
  case object CategoryNotExist extends AppError with ToResponse(Status.NotFound, "Category not found")

  /////////// records errors
  case object RecordNotExist extends AppError with ToResponse(Status.NotFound, "Record not found")

}
