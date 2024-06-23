package online.walletstate.db

import org.flywaydb.core.Flyway
import zio.*

import javax.sql.DataSource

case class Migrations(dataSource: DataSource) {

  private val load: Task[Flyway] =
    ZIO.attempt {
      Flyway
        .configure()
        .dataSource(dataSource)
        .baselineOnMigrate(true)
        .baselineVersion("0")
        .load()
    }

  val migrate: Task[Unit] =
    for {
      flyway <- load
      _      <- ZIO.attempt(flyway.migrate())
    } yield ()

}

object Migrations {
  val layer = ZLayer.fromFunction(Migrations.apply _)
}
