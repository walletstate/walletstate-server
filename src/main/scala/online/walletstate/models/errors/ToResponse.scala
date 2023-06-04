package online.walletstate.models.errors

import zio.http.*
import zio.json.*

case class ErrorBody(error: String, message: String)
object ErrorBody {
  given codec: JsonCodec[ErrorBody] = DeriveJsonCodec.gen[ErrorBody]
}

// Mix to the errors which can be returned back to the user
trait ToResponse(status: Status, message: String) {
  def toResponse: Response =
    Response(
      status = status,
      body = Body.fromString(ErrorBody(this.getClass.getSimpleName.replace("$", ""), message).toJson)
    )
}
