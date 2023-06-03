package online.walletstate.models.errors

import zio.json.{DeriveJsonEncoder, JsonEncoder}
import zio.http.Status

final case class ParseError(msg: String) extends AppError with ToResponse(Status.BadRequest, msg)
