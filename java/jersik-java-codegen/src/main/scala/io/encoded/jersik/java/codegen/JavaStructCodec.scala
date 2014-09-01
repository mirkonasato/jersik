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
package io.encoded.jersik.java.codegen

import io.encoded.jersik.schema._
import io.encoded.jersik.codegen.CodeGeneration._

class JavaStructCodec(module: Module, struct: Struct) {

  class JavaField(field: Field) {

    val fieldName = field.name
    val fieldType = JavaType.forType(field.dataType)
    val fieldObjectType = JavaType.objectTypeFor(field.dataType)
    val getterName = (if (field.dataType == BooleanType) "is" else "get") + fieldName.capitalize
    val setterName = "with"+ fieldName.capitalize

    def initializer = s"$fieldObjectType $fieldName = null;"

    def fieldReader: String = {
      val reader = valueReader(field.dataType, fieldName)
      s"$reader\ninstance.$setterName($fieldName);"
    }

    private def valueReader(dataType: DataType, result: String, nestLevel: Int = 0): String = {
      val resultType = JavaType.forType(dataType)
      dataType match {
        case BooleanType => s"$resultType $result = parser.getBooleanValue();"
        case IntType => s"$resultType $result = parser.getIntValue();"
        case LongType => s"$resultType $result = parser.getLongValue();"
        case FloatType => s"$resultType $result = parser.getFloatValue();"
        case DoubleType => s"$resultType $result = parser.getDoubleValue();"
        case StringType => s"$resultType $result = parser.getText();"
        case CustomType(typeName) =>
          if (module.isEnum(typeName))
            ai"""
$resultType $result;
try {
    $result = $typeName.valueOf(parser.getText());
} catch (IllegalArgumentException unknownEnumException) {
    $result = null;
}
            """
          else s"$resultType $result = ${typeName}Codec.INSTANCE.decode(parser);"
        case OptionalType(valueType) => {
          val reader = valueReader(valueType, result +"Value")
          s"$reader\n$resultType $result = ${JavaType.Optional}.ofNullable(${result}Value);"
        }
        case ListType(elementType) => {
          val typeParam = JavaType.objectTypeFor(elementType)
          val reader = valueReader(elementType, "element", nestLevel + 1)
          val list = if (nestLevel == 0) "_list" else "_list"+ nestLevel
          ai"""
if (parser.getCurrentToken() != JsonToken.START_ARRAY) {
    throw new JsonParseException("expected "+ JsonToken.START_ARRAY
        +" but found "+ parser.getCurrentToken(), parser.getCurrentLocation());
}
parser.nextToken();
java.util.ArrayList<$typeParam> $list = new java.util.ArrayList<$typeParam>();
while (parser.getCurrentToken() != JsonToken.END_ARRAY) {
    $reader
    $list.add(element);
    parser.nextToken();
}
$resultType $result = java.util.Collections.unmodifiableList($list);
        """
        }
        case MapType(keyType, valueType) => {
          val entryValueType = JavaType.objectTypeFor(valueType)
          val map = if (nestLevel == 0) "_map" else "_map"+ nestLevel
          val key = if (nestLevel == 0) "key" else "key"+ nestLevel
          val entryValueReader = valueReader(valueType, "value", nestLevel + 1)
          ai"""
if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
    throw new JsonParseException("expected " + JsonToken.START_OBJECT + " but found "
            + parser.getCurrentToken(), parser.getCurrentLocation());
}
parser.nextToken();
java.util.LinkedHashMap<String, $entryValueType> $map = new java.util.LinkedHashMap<String, $entryValueType>(); 
while (parser.getCurrentToken() != JsonToken.END_OBJECT) {
    String $key = parser.getText();
    parser.nextToken();
    $entryValueReader
    $map.put($key, value);
    parser.nextToken();
}
$resultType $result = java.util.Collections.unmodifiableMap($map);
          """
        }
      }
    }

    def fieldWriter: String = field.dataType match {
      case OptionalType(valueType) => {
        val writer = valueWriter(valueType, s"instance.$getterName().get()")
        ai"""
if (instance.$getterName().isPresent()) {
    generator.writeFieldName("$fieldName");
    $writer
}
        """
      }
      case dataType => {
        val writer = valueWriter(field.dataType, s"instance.$getterName()")
        s"""generator.writeFieldName("$fieldName");\n$writer"""
      }
    }

    private def valueWriter(valueType: DataType, accessor: String, nestLevel: Int = 0): String = valueType match {
      case BooleanType => s"generator.writeBoolean($accessor);"
      case IntType => s"generator.writeNumber($accessor);"
      case LongType => s"generator.writeNumber($accessor);"
      case FloatType => s"generator.writeNumber($accessor);"
      case DoubleType => s"generator.writeNumber($accessor);"
      case StringType => s"generator.writeString($accessor);"
      case CustomType(typeName) =>
        if (module.isEnum(typeName)) s"generator.writeString($accessor.name());"
        else s"""${typeName}Codec.INSTANCE.encode(generator, $accessor);"""
      case OptionalType(valueType) => throw new AssertionError("valueWriter should never be called with an OptionalType")
      case ListType(valueType) => {
        val element = if (nestLevel == 0) "element" else "element"+ nestLevel
        val writer = valueWriter(valueType, element, nestLevel + 1)
        val elementType = JavaType.forType(valueType)
        ai"""
generator.writeStartArray();
for ($elementType $element : $accessor) {
    $writer
}
generator.writeEndArray();
        """
      }
      case MapType(keyType, valueType) => {
        if (keyType != StringType) throw new AssertionError("Map keyType should be String but was "+ keyType)
        val entry = if (nestLevel == 0) "entry" else "entry"+ nestLevel
        val entryValueType = JavaType.objectTypeFor(valueType)
        val entryValueWriter = valueWriter(valueType, s"$entry.getValue()", nestLevel + 1)
        ai"""
generator.writeStartObject();
for (java.util.Map.Entry<String, $entryValueType> $entry : $accessor.entrySet()) {
    generator.writeFieldName($entry.getKey());
    $entryValueWriter
}
generator.writeEndObject();
        """
      }
    }

  }

  def toCode: String = {
    val moduleName = module.name
    val structName = struct.name
    val fields = struct.fields.map(new JavaField(_))

    val fieldNames = mapJoin(fields, ", ") { _.fieldName }
    val fieldInitializers = mapJoin(fields, "\n") { _.initializer }
    val fieldWriters = mapJoin(fields, "\n") { _.fieldWriter }
    val fieldReaders = mapJoin(fields, "\n") { field =>
      val fieldName = field.fieldName
      val reader = field.fieldReader
      ai"""
if (fieldName.equals("$fieldName")) {
    $reader
    continue;
}
      """
    }

    ai"""
package $moduleName;

import java.io.IOException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.encoded.jersik.runtime.ObjectCodec;

public class ${structName}Codec implements ObjectCodec<$structName> {

    public static final ${structName}Codec INSTANCE = new ${structName}Codec();

    public $structName decode(JsonParser parser) throws IOException {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
            throw new JsonParseException("expected " + JsonToken.START_OBJECT + " but found "
                    + parser.getCurrentToken(), parser.getCurrentLocation());
        }
        $structName.Builder instance = new $structName.Builder();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            if (parser.nextToken() != JsonToken.VALUE_NULL) {
                $fieldReaders
            }
            parser.skipChildren();
        }
        return instance.build();
    }

    public void encode(JsonGenerator generator, $structName instance) throws IOException {
        generator.writeStartObject();
        $fieldWriters
        generator.writeEndObject();
    }

}
    """
  }

}
