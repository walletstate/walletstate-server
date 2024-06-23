package zio.http.gen.ts

import zio.Chunk
import zio.http.Method
import TSType.{TSArray, TSBoolean, TSCustomType, TSNumber, TSOption, TSString}

final case class TSHttpServiceMethod(
    name: String,
    httpMethod: Method,
    path: String,
    params: Chunk[TSField],
    response: TSType
) {

  val allNestedTypes: Chunk[TSCustomType] =
    (response.allNestedTypes ++ params.flatMap(_.`type`.allNestedTypes))
      .collect { case ct: TSCustomType => ct }
      .distinctBy(_.`type`)

  val visibleCustomTypes: Chunk[TSCustomType] =
    (response.visibleTypes ++ params.flatMap(_.`type`.visibleTypes))
      .collect { case ct: TSCustomType => ct }
      .distinctBy(_.`type`)

  
  
  def render: String = {
    val pathParams  = params.filter(_.dest == TSField.FieldDestination.PathParam)
    val bodyParam   = params.filter(_.dest == TSField.FieldDestination.BodyParam)
    val queryParams = params.filter(_.dest == TSField.FieldDestination.QueryParam)
    // Optional parameters should be at the end
    val orderedQueryParams = queryParams.sortWith((a, b) => a.isRequired && b.isOptional)
    // TODO Validate that path and body params are not optional
    val orderedParams = pathParams ++ bodyParam ++ orderedQueryParams

    val (options, queryParamsBuildCode) = buildOptionsParam(orderedQueryParams)

    val httpMethodParams = httpMethod match {
      case Method.POST | Method.PUT | Method.PATCH =>
        val body = params.find(_.dest == TSField.FieldDestination.BodyParam).map(_.name).getOrElse("null")
        s"$path, $body, $options"
      case _ =>
        s"$path, $options"
    }

    s"""  $name(${orderedParams.map(_.render).mkString(", ")}): Observable<${response.renderType}> {
       |    $queryParamsBuildCode
       |    return this.http.${httpMethod.toString.toLowerCase}<${response.renderType}>($httpMethodParams);
       |  }
       |""".stripMargin
  }

  private def buildOptionsParam(fields: Chunk[TSField]): (String, String) = {
    val (constName, codeLines) = buildQueryParamsCode(fields)
    if (codeLines.nonEmpty) (s"{ params: $constName }", codeLines.mkString("\n    ")) else ("{}", "")
  }

  private def buildQueryParamsCode(
      fields: Chunk[TSField],
      codeLines: Chunk[String] = Chunk.empty,
      prevObject: String = "new HttpParams()"
  ): (String, Chunk[String]) = {
    fields.headOption match {
      case None => (prevObject, codeLines)
      case Some(f) =>
        val constName = s"${f.name}HttpParams"
        f.`type` match {
          case TSString | TSNumber | TSBoolean =>
            val codeLine = s"const $constName = $prevObject.set('${f.name}', ${f.name});"
            buildQueryParamsCode(fields.tail, codeLines :+ codeLine, constName)

          case TSOption(TSString) | TSOption(TSNumber) | TSOption(TSBoolean) =>
            val isDefined = s"(${f.name} !== undefined && ${f.name} !== null)"
            val codeLine  = s"const $constName = $isDefined ? $prevObject.set('${f.name}', ${f.name}) : $prevObject;"
            buildQueryParamsCode(fields.tail, codeLines :+ codeLine, constName)

          case TSArray(TSString) | TSArray(TSNumber) | TSArray(TSBoolean) =>
            val codeLine =
              s"const $constName = ${f.name}.length ? $prevObject.appendAll({${f.name}}) : $prevObject;"
            buildQueryParamsCode(fields.tail, codeLines :+ codeLine, constName)

          case field =>
            println("Unsupported type for query param. Skipping") 
            buildQueryParamsCode(fields.tail, codeLines, prevObject) //Throw exception???
        }

    }
  }

}
