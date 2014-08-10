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

object ScalaType {

  def forType(dataType: DataType): String = dataType match {
    case BooleanType => "Boolean"
    case IntType => "Int"
    case LongType => "Long"
    case FloatType => "Float"
    case DoubleType => "Double"
    case StringType => "String"
    case OptionalType(valueType) =>  "Option["+ forType(valueType) +"]"
    case ListType(itemType) =>  "Seq["+ forType(itemType) +"]"
    case MapType(keyType, valueType) => "Map["+ forType(keyType) +", "+ forType(valueType) +"]"
    case CustomType(typeName) => typeName
  }

}
