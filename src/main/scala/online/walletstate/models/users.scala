package online.walletstate.models

import online.walletstate.models.errors.{AppError, ToResponse}
import zio.http.Status
import zio.json.*

import java.util.UUID

object users {
  case class User(id: String, username: String, namespace: Option[UUID] = None)

  object codecs {
    given userCodec: JsonCodec[User] = DeriveJsonCodec.gen[User]
  }

  object errors {
    case object UserNotExist extends AppError with ToResponse(Status.NotFound, "User not found")
  }
}
