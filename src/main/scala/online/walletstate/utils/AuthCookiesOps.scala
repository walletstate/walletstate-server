package online.walletstate.utils

import online.walletstate.models.AuthToken
import zio.Duration
import zio.http.*

object AuthCookiesOps {

  private val AuthCookiesName           = "auth"
  private val TokenExpirationHeaderName = "X-Auth-Token-Expire-In"

  extension (headers: Headers)
    def getAuthCookies: Option[String] =
      headers.get(Header.Cookie).flatMap(_.value.find(_.name == AuthCookiesName).map(_.content))

  extension (response: Response)
    def withAuthCookies(token: AuthToken): Response =
      response
        .addCookie(authCookies(token.token, token.expireIn))
        .addHeader(TokenExpirationHeaderName, token.expireIn.toSeconds.toString)

    def clearAuthCookies: Response =
      response.addCookie(authCookies())

  private def authCookies(content: String = "", expireIn: Duration = Duration.Zero) =
    Cookie.Response(
      name = AuthCookiesName,
      content = content,
      path = Some(Root),
      isHttpOnly = true,
      maxAge = Some(expireIn)
    )
}