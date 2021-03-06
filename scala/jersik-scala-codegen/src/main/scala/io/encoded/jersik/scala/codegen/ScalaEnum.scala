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

class ScalaEnum(moduleName: String, enum: Enum) {

  def toCode: String = {
    val enumName = enum.name
    val valueObjects = mapJoin(enum.values, "\n") { valueName => s"""final case object $valueName extends $enumName("$valueName")""" }
    val valueCases = mapJoin(enum.values, "\n") { valueName => s"""case "$valueName" => $valueName""" }
    val valueNames = enum.values.mkString(", ")

    ai"""
package $moduleName

sealed abstract class $enumName(val name: String)
final object Suit {

  $valueObjects

  def withName(name: String): Suit = name match {
    $valueCases
    case _ => throw new IllegalArgumentException("no such $enumName: "+ name)
  }

  def values: Seq[Suit] = List($valueNames)

}
    """
  }

}
