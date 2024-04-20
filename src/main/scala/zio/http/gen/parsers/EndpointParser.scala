package zio.http.gen.parsers

import zio.Chunk
import zio.http.codec.HttpCodec.Metadata
import zio.http.codec.HttpCodec.Query.QueryParamHint
import zio.http.codec.{HttpCodec, SegmentCodec, TextCodec}
import zio.http.endpoint.Endpoint
import zio.http.gen.ts.{TSHttpServiceMethod, TSField, TSType}
import zio.http.{Method, RoutePattern}

object EndpointParser {

  def parse(name: String, endpoint: Endpoint[_, _, _, _, _]): TSHttpServiceMethod = {

    val atomInputCodecs  = collectAtomCodecs(endpoint.input, Chunk.empty)
    val atomOutputCodecs = collectAtomCodecs(endpoint.output, Chunk.empty)

    val (method, path, pathParams) = parseRoute(endpoint.route)
    val queryParams                = parseQueryParams(atomInputCodecs)
    val body                       = parseBody(atomInputCodecs)
    val responseType               = parseOutputType(atomOutputCodecs)

    TSHttpServiceMethod(name, method, path, pathParams ++ body.toList ++ queryParams, responseType)
  }

  private def collectAtomCodecs(
      httpCodec: HttpCodec[_, _],
      acc: Chunk[(HttpCodec.Atom[_, _], Chunk[HttpCodec.Metadata[_]])],
      metadata: Chunk[HttpCodec.Metadata[_]] = Chunk.empty
  ): Chunk[(HttpCodec.Atom[_, _], Chunk[HttpCodec.Metadata[_]])] = {
    httpCodec match {
      case atom: HttpCodec.Atom[_, _]           => (atom, metadata) +: acc
      case HttpCodec.Annotated(codec, meta)     => collectAtomCodecs(codec, acc, meta +: metadata)
      case HttpCodec.TransformOrFail(api, f, g) => collectAtomCodecs(api, acc, metadata)
      case HttpCodec.Empty                      => acc
      case HttpCodec.Halt                       => acc
      case HttpCodec.Combine(left, right, _) =>
        collectAtomCodecs(left, collectAtomCodecs(right, acc, metadata), metadata)
      case HttpCodec.Fallback(left, right, _, _) =>
        collectAtomCodecs(left, collectAtomCodecs(right, acc, metadata), metadata)
    }
  }

  private def parseRoute(route: RoutePattern[_]): (Method, String, Chunk[TSField]) = {
    val pathParams: Chunk[Option[String | TSField]] = route.pathCodec.segments.map {
      case SegmentCodec.Empty          => None
      case SegmentCodec.Literal(value) => Some(value)
      case SegmentCodec.BoolSeg(name)  => Some(TSField.pathParam(name, TSType.TSBoolean))
      case SegmentCodec.IntSeg(name)   => Some(TSField.pathParam(name, TSType.TSNumber))
      case SegmentCodec.LongSeg(name)  => Some(TSField.pathParam(name, TSType.TSNumber))
      case SegmentCodec.Text(name)     => Some(TSField.pathParam(name, TSType.TSString))
      case SegmentCodec.UUID(name)     => Some(TSField.pathParam(name, TSType.TSString))
      case SegmentCodec.Trailing       => None
    }

    val path = pathParams
      .flatMap {
        _.map {
          case string: String => string
          case field: TSField => s"$${${field.name}}"
        }
      }
      .mkString("`/", "/", "`")

    val params: Chunk[TSField] = pathParams.collect { case Some(field: TSField) => field }
    val method                 = route.method

    (method, path, params)
  }

  private def parseBody(atomCodecs: Chunk[(HttpCodec.Atom[_, _], Chunk[Metadata[_]])]): Option[TSField] = {
    atomCodecs
      .collect { case (HttpCodec.Content(codec, name, index), meta) =>
        SchemaParser.parse(codec.schema)
      }
      .map(t => TSField.bodyParam(t))
      .headOption
  }

  private def parseQueryParams(atomCodecs: Chunk[(HttpCodec.Atom[_, _], Chunk[Metadata[_]])]): Chunk[TSField] = {
    atomCodecs.collect { case (HttpCodec.Query(name, textCodec, hint, index), metadata) =>
      val isOptional = metadata.collect { case m: Metadata.Optional[_] => m }.nonEmpty

      val queryParamType = hint match {
        case QueryParamHint.Many | QueryParamHint.Any => TSType.TSArray(textCodecToTSType(textCodec))
        case _                                        => textCodecToTSType(textCodec)
      }
      TSField.queryParam(name, if (isOptional) queryParamType.optional else queryParamType)
    }
  }

  private def parseOutputType(atomCodecs: Chunk[(HttpCodec.Atom[_, _], Chunk[Metadata[_]])]): TSType = {
    atomCodecs
      .collect { case (HttpCodec.Content(codec, name, index), meta) =>
        SchemaParser.parse(codec.schema)
      }
      .headOption
      .getOrElse(TSType.TSNull)

  }

  private def textCodecToTSType(textCodec: TextCodec[_]) = {
    textCodec match
      case TextCodec.Constant(string) => TSType.TSString // ??? investigate
      case TextCodec.StringCodec      => TSType.TSString
      case TextCodec.IntCodec         => TSType.TSNumber
      case TextCodec.LongCodec        => TSType.TSNumber
      case TextCodec.BooleanCodec     => TSType.TSBoolean
      case TextCodec.UUIDCodec        => TSType.TSString
  }
}
