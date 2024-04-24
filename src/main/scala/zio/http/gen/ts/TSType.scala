package zio.http.gen.ts

import zio.Chunk

sealed trait TSType {
  val `type`: String

  def renderType: String = `type`

  def optional[T <: TSType]: TSType.TSOption[T] = TSType.TSOption[T](this.asInstanceOf[T])

  val visibleTypes: Chunk[TSType]
  val allNestedTypes: Chunk[TSType]
}

object TSType {

  sealed trait TSStandartType extends TSType {
    val visibleTypes: Chunk[TSType]   = Chunk(this)
    val allNestedTypes: Chunk[TSType] = visibleTypes
  }

  object TSNull    extends TSStandartType { val `type`: String = "null"    }
  object TSUnit    extends TSStandartType { val `type`: String = "void"    }
  object TSString  extends TSStandartType { val `type`: String = "string"  }
  object TSNumber  extends TSStandartType { val `type`: String = "number"  }
  object TSBoolean extends TSStandartType { val `type`: String = "boolean" }

  final case class TSOption[T <: TSType](ofType: T) extends TSStandartType {
    val `type`: String = ofType.`type`

    override def renderType: String = ofType.renderType

    override val visibleTypes: Chunk[TSType]   = ofType.visibleTypes
    override val allNestedTypes: Chunk[TSType] = ofType.allNestedTypes
  }

  final case class TSArray[T <: TSType](ofType: T) extends TSStandartType {
    val `type`: String = s"${ofType.`type`}"

    override def renderType: String = s"${ofType.renderType}[]"

    override val visibleTypes: Chunk[TSType]   = ofType.visibleTypes
    override val allNestedTypes: Chunk[TSType] = ofType.allNestedTypes
  }

  // TODO validate this/not testes
  final case class TSMap[K <: TSType, V <: TSType](key: K, value: V) extends TSStandartType {
    val `type`: String = s"Map<${key.`type`}, ${value.`type`}>"

    override def renderType: String = s"Map<${key.renderType}, ${value.renderType}>"

    override val visibleTypes: Chunk[TSType]   = key.visibleTypes ++ value.visibleTypes
    override val allNestedTypes: Chunk[TSType] = key.allNestedTypes ++ value.allNestedTypes
  }

  sealed trait TSCustomType extends TSType {
    def fileName: String                 = s"${`type`.toLowerCase}.ts"
    def importFrom(path: String): String = s"import { ${`type`} } from '$path/${fileName.replace(".ts", "")}';"
    def exportFrom(path: String): String = s"export { ${`type`} } from '$path/${fileName.replace(".ts", "")}';"

    def renderContent: String
  }

  final case class TSEnum(name: String, cases: Chunk[String]) extends TSCustomType {
    val `type`: String = name

    val visibleTypes: Chunk[TSType]   = Chunk(this)
    val allNestedTypes: Chunk[TSType] = visibleTypes

    def renderContent: String =
      s"""export enum $name {
         |${cases.map(c => s"$c = '$c'").mkString("  ", ",\n  ", "")}
         |}
         |""".stripMargin
  }

  final case class TSInterface private[TSType] (name: String, fields: Chunk[TSField]) extends TSCustomType {
    val `type`: String = name

    val visibleTypes: Chunk[TSType]   = Chunk(this)
    val allNestedTypes: Chunk[TSType] = Chunk(this) ++ fields.flatMap(_.`type`.allNestedTypes)

    def renderContent: String = {
      val necessariesImports =
        fields.flatMap(_.`type`.visibleTypes).collect { case ct: TSCustomType => ct }.distinctBy(_.`type`)

      s"""${necessariesImports.map(_.importFrom(".")).mkString("", "\n", "\n")}
         |export interface ${`type`} {
         |${fields.map(_.render).mkString("  ", ";\n  ", ";")}
         |}
         |""".stripMargin
    }
  }

  final case class TSInterface1[T <: TSType] private[TSType] (name: String, genType: T, fields: Chunk[TSField])
      extends TSCustomType {
    val `type`: String = name

    val visibleTypes: Chunk[TSType]   = Chunk(this)
    val allNestedTypes: Chunk[TSType] = Chunk(this) ++ fields.flatMap(_.`type`.allNestedTypes)

    override def renderType: String = s"${`type`}<${genType.`type`}>"

    def renderContent: String = {
      val imports =
        fields
          .filterNot(_.isGenericField)
          .flatMap(_.`type`.visibleTypes)
          .collect { case ct: TSCustomType => ct }
          .distinctBy(_.`type`)

      def renderField(f: TSField): String = if (f.isGenericField) {
        f.`type` match {
          case _: TSType.TSOption[_]        => s"${f.name}?: T"
          case _: TSType.TSArray[_]         => s"${f.name}: T[]"
          case TSType.TSInterface1(_, _, _) => s"${f.name}: ????" // ?????
          case _                            => s"${f.name}: T"
        }
      } else f.render

      s"""${imports.map(_.importFrom(".")).mkString("", "\n", "")}
         |export interface ${`type`}<T> {
         |${fields.map(renderField).mkString("  ", ";\n  ", ";")}
         |}
         |""".stripMargin
    }
  }

  object TSInterface {
    def build(name: String, fields: Chunk[TSField]): TSCustomType = {
      val genericTypes = fields.filter(_.isGenericField).map(_.`type`).distinctBy(_.`type`)

      genericTypes.toList match {
        case Nil if !name.contains('[')        => TSInterface(name, fields)
        case genT :: Nil if name.endsWith("]") => TSInterface1(name.takeWhile(_ != '['), genT, fields)
        case types                             => throw new Exception(s"Unsupported generic type: [$name, $types]")
      }
    }
  }

}
