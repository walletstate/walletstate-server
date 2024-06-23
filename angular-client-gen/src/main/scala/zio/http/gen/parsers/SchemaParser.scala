package zio.http.gen.parsers

import zio.http.gen.ts.{TSField, TSType}
import zio.schema.{Schema, StandardType, TypeId}

object SchemaParser {

  // TODO Make tail recursion
  def parse(schema: Schema[_]): TSType = {
    schema match {
      case s: Schema.Enum[_]            => parseEnum(s)
      case s: Schema.Record[_]          => parseRecord(s)
      case s: Schema.Lazy[_]            => parse(s.schema)
      case s: Schema.Primitive[_]       => parsePrimitive(s)
      case s: Schema.Optional[_]        => parse(s.schema).optional
      case s: Schema.Collection[_, _]   => parseCollection(s)
      case s: Schema.Transform[_, _, _] => parse(s.schema)
      case s: Schema.Fail[_]            => TSType.TSNull // TODO investigate
      case s: Schema.Tuple2[_, _]       => TSType.TSNull // TODO investigate
      case s: Schema.Either[_, _]       => TSType.TSNull // TODO investigate
      case s: Schema.Fallback[_, _]     => TSType.TSNull // TODO investigate
      case s: Schema.Dynamic            => TSType.TSNull // TODO investigate
    }
  }

  private def parseRecord(schema: Schema.Record[_]): TSType = {
    val fields = schema.fields.map(f => TSField.field(f.name, parse(f.schema), f.annotations))
    TSType.TSInterface.build(parseName(schema.id), fields)
  }

  private def parsePrimitive(schema: Schema.Primitive[_]): TSType = schema.standardType match {
    case StandardType.UnitType           => TSType.TSUnit
    case StandardType.StringType         => TSType.TSString
    case StandardType.BoolType           => TSType.TSBoolean
    case StandardType.ByteType           => TSType.TSNull   // TODO investigate corresponding ts type
    case StandardType.ShortType          => TSType.TSNumber
    case StandardType.IntType            => TSType.TSNumber
    case StandardType.LongType           => TSType.TSNumber
    case StandardType.FloatType          => TSType.TSNumber
    case StandardType.DoubleType         => TSType.TSNumber
    case StandardType.BinaryType         => TSType.TSNull   // TODO investigate corresponding ts type
    case StandardType.CharType           => TSType.TSString
    case StandardType.UUIDType           => TSType.TSString
    case StandardType.BigDecimalType     => TSType.TSNumber // TODO investigate corresponding ts type
    case StandardType.BigIntegerType     => TSType.TSNumber // TODO investigate corresponding ts type
    case StandardType.DayOfWeekType      => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.MonthType          => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.MonthDayType       => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.PeriodType         => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.YearType           => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.YearMonthType      => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.ZoneIdType         => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.ZoneOffsetType     => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.DurationType       => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.InstantType        => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.LocalDateType      => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.LocalTimeType      => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.LocalDateTimeType  => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.OffsetTimeType     => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.OffsetDateTimeType => TSType.TSString // TODO investigate corresponding ts type
    case StandardType.ZonedDateTimeType  => TSType.TSString // TODO investigate corresponding ts type
  }

  private def parseCollection(schema: Schema.Collection[_, _]): TSType = schema match {
    case Schema.Sequence(elementSchema, _, _, _, _) => TSType.TSArray(parse(elementSchema))
    case Schema.Map(keySchema, valueSchema, _)      => TSType.TSMap(parse(keySchema), parse(valueSchema))
    case Schema.Set(elementSchema, _)               => TSType.TSArray(parse(elementSchema))
  }

  private def parseEnum(schema: Schema.Enum[_]): TSType = {
    TSType.TSEnum(parseName(schema.id), schema.cases.map(_.id))
  }

  private def parseName(typeId: TypeId): String = typeId match {
    case TypeId.Structural                                  => typeId.name
    case TypeId.Nominal(packageName, objectNames, typeName) => (objectNames :+ typeName).mkString("")
  }
}
