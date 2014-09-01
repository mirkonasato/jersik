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

object JavaType {

  val List = "java.util.List"
  val Map = "java.util.Map"
  val Optional = "io.encoded.jersik.runtime.types.Optional"

  def forType(dataType: DataType): String = dataType match {
    case BooleanType => "boolean"
    case IntType => "int"
    case LongType => "long"
    case FloatType => "float"
    case DoubleType => "double"
    case StringType => "String"
    case OptionalType(valueType) => Optional +"<"+ objectTypeFor(valueType) +">"
    case ListType(itemType) =>  List +"<"+ objectTypeFor(itemType) +">"
    case MapType(keyType, valueType) => Map +"<"+ objectTypeFor(keyType) +", "+ objectTypeFor(valueType) +">"
    case CustomType(typeName) => typeName
  }

  def objectTypeFor(dataType: DataType): String = dataType match {
    case BooleanType => "Boolean"
    case IntType => "Integer"
    case LongType => "Long"
    case FloatType => "Float"
    case DoubleType => "Double"
    case _ => forType(dataType)
  }

  def isPrimitive(dataType: DataType): Boolean = dataType match {
    case BooleanType | IntType | LongType | FloatType | DoubleType => true
    case _ => false
  }

}
