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

class ScalaStruct(moduleName: String, struct: Struct) {

  val structName = struct.name

  def toCode: String = {

    val constructorArguments = mapJoin(struct.fields, ",\n") { field =>
      val fieldName = field.name
      val fieldType = ScalaType.forType(field.dataType)
      field.dataType match {
        case OptionalType(_) => s"$fieldName: $fieldType = None"
        case _ => s"$fieldName: $fieldType"
      }
    }

    ai"""
package $moduleName

case class $structName(
    $constructorArguments
)
    """
  }

}
