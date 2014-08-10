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
package io.encoded.jersik.schema

import scala.util.parsing.combinator.RegexParsers

object SchemaParser extends RegexParsers {

  protected override val whiteSpace = """(\s|//.*)+""".r
  
  private def identifier: Parser[String] = "[a-zA-Z]+".r
  private def moduleIdentifier: Parser[String] = """[a-z]+(\.[a-z]+)*""".r

  private def dataType: Parser[DataType] = type2 | type1 | type0

  private def type0: Parser[DataType] = ("Boolean" | "Int" | "Long" | "Float" | "Double" | "String" | identifier) ^^ {
    case "Boolean" => BooleanType
    case "Int" => IntType
    case "Long" => LongType
    case "Float" => FloatType
    case "Double" => DoubleType
    case "String" => StringType
    case identifier => CustomType(identifier)
  }

  private def type1: Parser[DataType] = ("List" | "Optional") ~"<"~ dataType ~">" ^^ {
    case "List" ~"<"~ itemType ~">" => ListType(itemType)
    case "Optional" ~"<"~ valueType ~">" => OptionalType(valueType)
  }

  private def type2: Parser[DataType] = "Map" ~"<"~ dataType ~","~ dataType ~">" ^^ {
    case "Map" ~"<"~ keyType ~","~ valueType ~">" => MapType(keyType, valueType)
  }

  private def field: Parser[Field] = identifier ~":"~ dataType ^^ {
    case name ~":"~ dataType => Field(name, dataType)
  }

  private def struct: Parser[Container] = "struct" ~> identifier ~"{"~ field.* ~"}" ^^ {
    case name ~"{"~ fields ~"}" => Struct(name, fields)
  }

  private def enum: Parser[Container] = "enum" ~> identifier ~"{"~ identifier.* ~"}" ^^ {
    case name ~"{"~ values ~"}" => Enum(name, values)
  }

  private def operation: Parser[Operation] = identifier ~":"~ identifier ~"->"~ identifier ^^ {
    case name ~":"~ requestType ~"->"~ responseType =>
      Operation(name, CustomType(requestType), CustomType(responseType))
  }

  private def service: Parser[Service] = "service" ~> identifier ~"{"~ operation.* ~"}" ^^ {
    case name ~"{"~ operations ~"}" => Service(name, operations)
  }

  private def module: Parser[Module] = "module" ~> moduleIdentifier ~ (enum | struct | service).* ^^ {
    case name ~ containers => Module(name, containers)
  }

  def parseModule(in: CharSequence): ParseResult[Module] = parseAll(module, in)

}
