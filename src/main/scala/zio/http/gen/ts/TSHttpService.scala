package zio.http.gen.ts

import zio.Chunk
import zio.http.gen.ts.TSType.TSCustomType

final case class TSHttpService(name: String, methods: Chunk[TSHttpServiceMethod], defaultImports: Chunk[String]) {

  val fileName = s"${name.toLowerCase}.ts"

  val allCustomTypes: Chunk[TSCustomType]     = methods.flatMap(_.allNestedTypes).distinctBy(_.`type`)
  val visibleCustomTypes: Chunk[TSCustomType] = methods.flatMap(_.visibleCustomTypes).distinctBy(_.`type`)

  def exportFrom(path: String): String = s"export { $name } from '$path/${fileName.replace(".ts", "")}';"

  def render: String = {
    s"""${defaultImports.mkString("\n")}
      |${visibleCustomTypes.map(_.importFrom("./models")).mkString("\n")}
      |
      |@Injectable({
      |  providedIn: 'root'
      |})
      |export class $name {
      |
      |  constructor(private http: HttpClient) {
      |  }
      |
      |${methods.map(_.render).mkString("\n")}
      |}
      |""".stripMargin
  }

}

object TSHttpService {
  val defaultImports = Chunk(
    "import { Injectable } from '@angular/core';",
    "import { HttpClient, HttpParams } from '@angular/common/http';",
    "import { Observable } from 'rxjs';"
  )

  def apply(name: String, methods: Chunk[TSHttpServiceMethod]): TSHttpService =
    TSHttpService(name, methods, defaultImports)
}
