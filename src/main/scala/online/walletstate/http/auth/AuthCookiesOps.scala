package online.walletstate.http.auth

import zio.http.*

object AuthCookiesOps {

  private val AuthCookiesName = "auth"

  extension (headers: Headers)
    def getAuthCookies: Option[String] =
      headers.get(Header.Cookie).flatMap(_.value.find(_.name == AuthCookiesName).map(_.content))

  extension (response: Response)
    def withAuthCookies(content: String): Response =
      response.addCookie(Cookie.Response(name = AuthCookiesName, path = Some(!!), content = content))
      
    def clearAuthCookies: Response =
      response.addCookie(Cookie.clear(AuthCookiesName))
}
