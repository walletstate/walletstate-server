package online

import online.walletstate.models.AuthContext.{UserContext, WalletContext}
import zio.{URIO, ZIO}

package object walletstate {

  type WalletIO[E, A] = ZIO[WalletContext, E, A]
  type WalletUIO[A]   = URIO[WalletContext, A]

  type UserIO[E, A] = ZIO[UserContext, E, A]
  type UserUIO[A]   = URIO[UserContext, A]

}
