package online.walletstate.models

import zio.http.Status

package object errors {

  /////////// auth errors
  case object InvalidCredentials extends AppError with ToResponse(Status.Unauthorized, "Invalid credentials")

  /////////// user errors
  case object UserNotExist extends AppError with ToResponse(Status.NotFound, "User not found")

  /////////// namespace errors
  case object NamespaceNotExist extends AppError

  case object UserAlreadyHasNamespace // Allow only one namespace for user for now
    extends AppError
      with ToResponse(Status.Forbidden, "User already has the namespace")

  /////////// namespace invite errors
  case object NamespaceInviteNotExist
    extends AppError
      with ToResponse(Status.Forbidden, "Invite code not found. Cannot join the namespace.")

  case object NamespaceInviteExpired
    extends AppError
      with ToResponse(Status.Forbidden, "Invite code expired. Please ask the namespace owner to generate a new one")
  
}
