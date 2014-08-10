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

sealed abstract class DataType
case object BooleanType extends DataType
case object IntType extends DataType
case object LongType extends DataType
case object FloatType extends DataType
case object DoubleType extends DataType
case object StringType extends DataType
final case class OptionalType(valueType: DataType) extends DataType
final case class ListType(itemType: DataType) extends DataType
final case class MapType(keyType: DataType, valueType: DataType) extends DataType
final case class CustomType(name: String) extends DataType

final case class Field(name: String, dataType: DataType)
final case class Operation(name: String, requestType: CustomType, responseType: CustomType)

sealed abstract class Container(name: String)
final case class Struct(name: String, fields: Seq[Field]) extends Container(name)
final case class Enum(name: String, values: Seq[String]) extends Container(name)
final case class Service(name: String, operations: Seq[Operation]) extends Container(name)

final case class Module(name: String, containers: Seq[Container]) {

  val enums = containers.filter(_.isInstanceOf[Enum]).asInstanceOf[Seq[Enum]]
  val structs = containers.filter(_.isInstanceOf[Struct]).asInstanceOf[Seq[Struct]]
  val services = containers.filter(_.isInstanceOf[Service]).asInstanceOf[Seq[Service]]
  validate()
  def isEnum(name: String) = enums.exists(_.name == name)
  def isStruct(name: String) = structs.exists(_.name == name)
  def isService(name: String) = services.exists(_.name == name)

  private def validate(): Unit = {
    for {
      struct <- structs
      field <- struct.fields
    } field.dataType match {
      case CustomType(name) =>
        if (!(isEnum(name) || isStruct(name)))
          throw new IllegalArgumentException(s"No such struct or enum: '$name'; in field '${field.name}' of struct '${struct.name}'")
      case _ => ()
    }
    for {
      service <- services
      operation <- service.operations
      dataType <- List(operation.requestType, operation.responseType)
    } dataType match {
      case CustomType(name) =>
        if (!isStruct(name))
          throw new IllegalArgumentException(s"No such struct: '$name'; in operation '${operation.name}' of service '${service.name}'")
      case _ =>
        throw new IllegalArgumentException(s"Not a struct: $dataType; in operation '${operation.name}' of service '${service.name}'")
    }
  }

}
