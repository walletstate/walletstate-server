package zio.http.gen

import online.walletstate.http.api.{
  AccountsEndpoints,
  AssetsEndpoints,
  CategoriesEndpoints,
  ExchangeRatesEndpoints,
  GroupsEndpoints,
  IconsEndpoints,
  TransactionsEndpoints,
  WalletsEndpoints
}
import online.walletstate.models.Wallet
import zio.http.Method
import zio.http.codec.{Doc, QueryCodec}
import zio.http.endpoint.Endpoint
import zio.http.gen.annotations.genericField
import zio.http.gen.parsers.EndpointParser
import zio.http.gen.ts.TSHttpService
import zio.schema.{DeriveSchema, Schema}
import zio.{Chunk, ZIOAppDefault}

object GenPlayground extends ZIOAppDefault {

  /** ZIO Endpoint doesn't support `operationId` and `tags` yet which can be used as method name and class name So for
    * now decided to user following structure `Map(ServiceName -> Map(MethodName -> Endpoint)`
    */
  val apiEndpoints = Map(
    "AccountsHttpClient"      -> AccountsEndpoints.endpointsMap,
    "AssetsHttpClient"        -> AssetsEndpoints.endpointsMap,
    "CategoriesHttpClient"    -> CategoriesEndpoints.endpointsMap,
    "ExchangeRatesHttpClient" -> ExchangeRatesEndpoints.endpointsMap,
    "GroupsHttpClient"        -> GroupsEndpoints.endpointsMap,
    "TransactionsHttpClient"  -> TransactionsEndpoints.endpointsMap,
    "WalletsHttpClient"       -> WalletsEndpoints.endpointsMap,
    "IconsHttpClient"         -> IconsEndpoints.endpointsMap
  )

  def run = {
    val httpServices = apiEndpoints.map { (serviceName, endpoints) =>
      val methods = endpoints.map((methodName, endpoint) => EndpointParser.parse(methodName, endpoint))
      TSHttpService(serviceName, Chunk.from(methods))
    }

//    val testHttpServices = Map("TestService" -> Map("testEndpoint" -> TestData.testEndpoint)).map {
//      (serviceName, endpoints) =>
//        val methods = endpoints.map((methodName, endpoint) => EndpointParser.parse(methodName, endpoint))
//        TSHttpService(serviceName, Chunk.from(methods))
//    }

    AngularLibraryBuilder.build(Chunk.from(httpServices), version = "0.0.2")
  }
}

object TestData {
  final case class CaseClass[A](@genericField() a: A, b: Option[String], c: String)

  object CaseClass {
    given schema[A: Schema]: Schema[CaseClass[A]] = DeriveSchema.gen[CaseClass[A]]
  }

  enum SomeEnum {
    case Accounts, Categories
  }

  object SomeEnum {
    given schema: Schema[SomeEnum] = DeriveSchema.gen[SomeEnum]
  }

  final case class WithNestedClass[T](
      nested: CaseClass[Int],
      testInt: Int,
      testOptionInt: Option[Int],
      testChunk: Chunk[CaseClass[Boolean]],
      testMap: Map[String, Boolean],
      testOptionMap: Option[Map[Long, CaseClass[Int]]],
      testEnum: SomeEnum,
      @genericField() generic: T
  )

  object WithNestedClass {
    given schema[T: Schema]: Schema[WithNestedClass[T]] = DeriveSchema.gen[WithNestedClass[T]]
  }

  val testEndpoint =
    Endpoint(Method.PUT / "test" / Wallet.Id.path)
      .in[WithNestedClass[Long]]
      .query(QueryCodec.query("optional").optional.??(Doc.h1("Test optional query")))
      .query(QueryCodec.query("required").??(Doc.h1("Test required query param")))
      .query(QueryCodec.queryAllTo[Int]("array").??(Doc.h1("Test array query")))
      .out[CaseClass[Long]]

}
