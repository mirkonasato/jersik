//
// Copyright (c) 2014 Mirko Nasato
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package io.encoded.jersik.scala.codegen

import io.encoded.jersik.schema._
import io.encoded.jersik.codegen.CodeGeneration._

class ScalaStructCodec(module: Module, struct: Struct) {

  class ScalaField(field: Field) {

    val fieldName = field.name
    val fieldType = ScalaType.forType(field.dataType)

    def fieldReader: String = {
      val reader = valueReader(field.dataType, "_"+ fieldName +"_value")
      s"$reader\n_$fieldName = Option(_${fieldName}_value)"
    }

    private def valueReader(dataType: DataType, result: String, nestLevel: Int = 0): String = {
      val resultType = ScalaType.forType(dataType)
      dataType match {
        case BooleanType => s"val $result: $resultType = parser.getBooleanValue()"
        case IntType => s"val $result: $resultType = parser.getIntValue()"
        case LongType => s"val $result: $resultType = parser.getLongValue()"
        case FloatType => s"val $result: $resultType = parser.getFloatValue()"
        case DoubleType => s"val $result: $resultType = parser.getDoubleValue()"
        case StringType => s"val $result: $resultType = parser.getText()"
        case CustomType(typeName) =>
          if (module.isStruct(typeName))
            s"val $result: $resultType = ${typeName}Codec.decode(parser)"
          else if (module.isEnum(typeName))
            s"""
val $result: $resultType = try {
  $typeName.withName(parser.getText())
} catch {
  case e: IllegalArgumentException => null
}
            """.trim
          else
            throw new IllegalArgumentException("not a struct nor an enum: "+ typeName)
        case OptionalType(valueType) => valueReader(valueType, result, nestLevel)
        case ListType(elementType) => {
          val typeParam = ScalaType.forType(elementType)
          val reader = valueReader(elementType, "element", nestLevel + 1)
          val list = if (nestLevel == 0) "_list" else "_list"+ nestLevel
          ai"""
if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
  throw new JsonParseException("expected "+ JsonToken.START_ARRAY
      +" but found "+ parser.getCurrentToken(), parser.getCurrentLocation())
}
parser.nextToken()
val $list = List.newBuilder[$typeParam]
while (parser.getCurrentToken() != JsonToken.END_ARRAY) {
  $reader
  $list += element
  parser.nextToken()
}
val $result: $resultType = $list.result
        """
        }
        case MapType(keyType, valueType) => {
          val entryValueType = ScalaType.forType(valueType)
          val map = if (nestLevel == 0) "_map" else "_map"+ nestLevel
          val key = if (nestLevel == 0) "key" else "key"+ nestLevel
          val entryValueReader = valueReader(valueType, "value", nestLevel + 1)
          ai"""
if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
  throw new JsonParseException("expected " + JsonToken.START_OBJECT + " but found "
      + parser.getCurrentToken(), parser.getCurrentLocation())
}
parser.nextToken()
val $map = Map.newBuilder[String, $entryValueType]
while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
  val $key = parser.getText()
  parser.nextToken()
  $entryValueReader
  $map += ($key -> value)
  parser.nextToken()
}
val $result: $resultType = $map.result
          """
        }
      }
    }

    def fieldWriter: String = field.dataType match {
      case OptionalType(valueType) => {
        val writer = valueWriter(valueType, s"instance.$fieldName.get")
        ai"""
if (instance.$fieldName.isDefined) {
  generator.writeFieldName("$fieldName")
  $writer
}
        """
      }
      case dataType => {
        val writer = valueWriter(field.dataType, s"instance.$fieldName")
        s"""generator.writeFieldName("$fieldName")\n$writer"""
      }
    }

    private def valueWriter(valueType: DataType, accessor: String, nestLevel: Int = 0): String = valueType match {
      case BooleanType => s"generator.writeBoolean($accessor)"
      case IntType => s"generator.writeNumber($accessor)"
      case LongType => s"generator.writeNumber($accessor)"
      case FloatType => s"generator.writeNumber($accessor)"
      case DoubleType => s"generator.writeNumber($accessor)"
      case StringType => s"generator.writeString($accessor)"
      case CustomType(typeName) =>
        if (module.isStruct(typeName))
          s"${typeName}Codec.encode($accessor, generator)"
        else if (module.isEnum(typeName))
          s"generator.writeString($accessor.name)"
        else
          throw new IllegalArgumentException("not a struct nor an enum: "+ typeName)
      case OptionalType(valueType) => throw new AssertionError("valueWriter should never be called with an OptionalType")
      case ListType(valueType) => {
        val element = if (nestLevel == 0) "element" else "element"+ nestLevel
        val writer = valueWriter(valueType, element, nestLevel + 1)
        val elementType = ScalaType.forType(valueType)
        ai"""
generator.writeStartArray()
for ($element: $elementType <- $accessor) {
  $writer
}
generator.writeEndArray()
        """
      }
      case MapType(keyType, valueType) => {
        if (keyType != StringType) throw new AssertionError("Map keyType should be String but was "+ keyType)
        val entry = if (nestLevel == 0) "entry" else "entry"+ nestLevel
        val entryValueType = ScalaType.forType(valueType)
        val entryValueWriter = valueWriter(valueType, s"$entry._2", nestLevel + 1)
        ai"""
generator.writeStartObject()
for ($entry <- $accessor) {
  generator.writeFieldName($entry._1)
  $entryValueWriter
}
generator.writeEndObject()
        """
      }
    }

  }

  def toCode: String = {
    val moduleName = module.name
    val structName = struct.name
    val fields = struct.fields.map(new ScalaField(_))

    val localFieldDeclarations = mapJoin(struct.fields, "\n") { field =>
      val fieldName = field.name
      val fieldType = ScalaType.forType(field.dataType)
      field.dataType match {
        case OptionalType(valueType) => {
          val typeParam = ScalaType.forType(valueType)
          s"var _$fieldName = Option.empty[$typeParam]"
        }
        case _ => s"var _$fieldName = Option.empty[$fieldType]"
      }
    }
    val fieldReaders = mapJoin(fields, "\n") { field =>
      val fieldName = field.fieldName
      val reader = field.fieldReader
      ai"""
case "$fieldName" => {
  $reader
}
      """
    }
    val constructorAssignments = mapJoin(struct.fields, ",\n") { field =>
      val fieldName = field.name
      field.dataType match {
        case OptionalType(valueType) => s"$fieldName = _$fieldName"
        case _ => s"$fieldName = _$fieldName.get"
      }
    }
    val fieldWriters = mapJoin(fields, "\n") { _.fieldWriter }

    ai"""
package $moduleName

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import io.encoded.jersik.scala.runtime.ObjectCodec

object ${structName}Codec extends ObjectCodec[$structName] {

  def decode(parser: JsonParser): $structName = {
    if (parser.getCurrentToken() != JsonToken.START_OBJECT)
      throw new JsonParseException("expected "+ JsonToken.START_OBJECT +" but found "+ parser.getCurrentToken(), parser.getCurrentLocation())
    $localFieldDeclarations
    while (parser.nextToken() != JsonToken.END_OBJECT) {
      val fieldName = parser.getCurrentName();
      if (parser.nextToken() != JsonToken.VALUE_NULL) fieldName match {
        $fieldReaders
        case _ => parser.skipChildren()
      }
      else parser.skipChildren()
    }
    $structName(
        $constructorAssignments
    )
  }

  def encode(instance: $structName, generator: JsonGenerator): Unit = {
    generator.writeStartObject()
    $fieldWriters
    generator.writeEndObject()
  }

}
    """
  }

}
