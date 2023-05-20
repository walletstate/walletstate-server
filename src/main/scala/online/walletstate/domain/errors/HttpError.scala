package online.walletstate.domain.errors

import zio.http.*

//TODO tmp. design error model 
case class AppHttpError(status: Status, msg: String) extends Throwable
