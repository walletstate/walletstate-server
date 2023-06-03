package online.walletstate.models

import online.walletstate.models.errors.{AppError, ToResponse}
import zio.http.Status
import zio.json.*

object auth {
  case class LoginInfo(username: String, password: String)

  object codecs {
    given loginInfoCodec: JsonCodec[LoginInfo] = DeriveJsonCodec.gen[LoginInfo]
  }

  object errors {
    case object InvalidCredentials extends AppError with ToResponse(Status.Unauthorized, "Invalid credentials")
  }
}
