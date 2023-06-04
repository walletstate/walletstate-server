package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, UserNamespaceContext}
import online.walletstate.models.api.CreateRecord
import online.walletstate.models.{Account, Record}
import online.walletstate.services.RecordsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class RecordsRoutes(auth: AuthMiddleware, recordsService: RecordsService) {

  private val createRecordHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx  <- ZIO.service[UserNamespaceContext]
      info <- req.as[CreateRecord]
      record <- recordsService.create( // TODO pass namespace and check that account is in correct namespace
        info.account,
        info.amount,
        info.`type`,
        info.category,
        info.description,
        info.time,
        ctx.user
      )
    } yield Response.json(record.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private val getRecordsHandler = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx     <- ZIO.service[UserNamespaceContext]
      records <- recordsService.list(ctx.namespace)
    } yield Response.json(records.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  private def getRecordHandler(idStr: String) = Handler.fromFunctionZIO[Request] { req =>
    for {
      ctx    <- ZIO.service[UserNamespaceContext]
      id     <- Record.Id.from(idStr)
      record <- recordsService.get(ctx.namespace, id)
    } yield Response.json(record.toJson)
  } @@ auth.ctx[UserNamespaceContext]

  def routes = Http.collectHandler[Request] {
    case Method.POST -> !! / "api" / "records"     => createRecordHandler
    case Method.GET -> !! / "api" / "records"      => getRecordsHandler
    case Method.GET -> !! / "api" / "records" / id => getRecordHandler(id)
  }
}

object RecordsRoutes {
  val layer = ZLayer.fromFunction(RecordsRoutes.apply _)
}
