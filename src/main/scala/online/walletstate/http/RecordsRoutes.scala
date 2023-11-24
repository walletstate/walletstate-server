package online.walletstate.http

import online.walletstate.http.auth.{AuthMiddleware, WalletContext}
import online.walletstate.models.api.CreateRecord
import online.walletstate.models.{Account, Record}
import online.walletstate.services.RecordsService
import online.walletstate.utils.RequestOps.as
import zio.*
import zio.http.*
import zio.json.*

case class RecordsRoutes(auth: AuthMiddleware, recordsService: RecordsService) {

  private val createRecordHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      info <- req.as[CreateRecord]
      record <- recordsService.create( // TODO pass wallet and check that account is in correct wallet
        info.account,
        info.amount,
        info.`type`,
        info.category,
        info.description,
        info.time,
        ctx.user
      )
    } yield Response.json(record.toJson)
  }

  private val getRecordsHandler = Handler.fromFunctionZIO[(WalletContext, Request)] { (ctx, req) =>
    for {
      records <- recordsService.list(ctx.wallet)
    } yield Response.json(records.toJson)
  }

  private val getRecordHandler = Handler.fromFunctionZIO[(Record.Id, WalletContext, Request)] { (id, ctx, req) =>
    for {
      record <- recordsService.get(ctx.wallet, id)
    } yield Response.json(record.toJson)
  }

  def routes = Routes(
    Method.POST / "api" / "records"                 -> auth.walletCtx -> createRecordHandler,
    Method.GET / "api" / "records"                  -> auth.walletCtx -> getRecordsHandler,
    Method.GET / "api" / "records" / Record.Id.path -> auth.walletCtx -> getRecordHandler
  )
}

object RecordsRoutes {
  val layer = ZLayer.fromFunction(RecordsRoutes.apply _)
}
