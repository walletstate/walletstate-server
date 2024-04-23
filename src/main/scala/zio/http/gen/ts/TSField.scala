package zio.http.gen.ts

import zio.Chunk
import zio.http.gen.annotations.genericField
import zio.http.gen.ts.TSField.FieldDestination

final case class TSField(name: String, `type`: TSType, dest: FieldDestination, annotations: Chunk[Any] = Chunk.empty) {

  def render: String = `type` match {
    case _: TSType.TSOption[_] => s"$name?: ${`type`.renderType}"
    case _                     => s"$name: ${`type`.renderType}"
  }

  def isGenericField: Boolean = annotations.collectFirst { case genericField() => () }.nonEmpty

  def isOptional: Boolean = `type`.isInstanceOf[TSType.TSOption[_]]
  def isRequired: Boolean = !isOptional
}

object TSField {

  sealed trait FieldDestination

  object FieldDestination {
    case object InterfaceField extends FieldDestination
    case object PathParam      extends FieldDestination
    case object QueryParam     extends FieldDestination
    case object BodyParam      extends FieldDestination
  }

  def formated(name: String, `type`: TSType, dest: FieldDestination): TSField =
    TSField(formatFieldName(name), `type`, dest)

  def field(name: String, `type`: TSType, annotations: Chunk[Any] = Chunk.empty): TSField =
    TSField(formatFieldName(name), `type`, FieldDestination.InterfaceField, annotations)

  def pathParam(name: String, `type`: TSType): TSField =
    TSField(formatFieldName(name), `type`, FieldDestination.PathParam)

  def queryParam(name: String, `type`: TSType): TSField =
    TSField(formatFieldName(name), `type`, FieldDestination.QueryParam)

  def bodyParam(`type`: TSType): TSField =
    TSField("body", `type`, FieldDestination.BodyParam)

  private def formatFieldName(string: String): String =
    val (firstLetter, tail) = string.split("-").map(_.capitalize).mkString("").splitAt(1)
    firstLetter.toLowerCase.concat(tail)
}
