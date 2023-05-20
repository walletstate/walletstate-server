package online.walletstate.domain.errors

trait AppError extends Throwable

case object UserNotFound extends AppError
