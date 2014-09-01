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

class JavaStruct(moduleName: String, struct: Struct) {

  class JavaField(field: Field) {

    val fieldName = field.name
    val capitalFieldName = field.name.capitalize
    val fieldType = JavaType.forType(field.dataType)
    val fieldObjectType = JavaType.objectTypeFor(field.dataType)
    val isPrimitive = JavaType.isPrimitive(field.dataType)
    val getterName = (if (field.dataType == BooleanType) "is" else "get") + capitalFieldName

    def declaration = s"$fieldType $fieldName"
    def memberDeclaration = s"private final $fieldType $fieldName;"
    def memberAssignment = s"this.$fieldName = $fieldName;"
    def getterMethod = s"public $fieldType $getterName() {\n    return $fieldName;\n}"
    def nullCheck = s"""if ($fieldName == null) throw new NullPointerException("$fieldName cannot be null");"""

    def equalCheck =
      if (isPrimitive) s"if (this.$fieldName != that.$fieldName) return false;"
      else s"if (this.$fieldName != that.$fieldName && !this.$fieldName.equals(that.$fieldName)) return false;"

    def hashValue = field.dataType match {
        case BooleanType => s"($fieldName ? 1231 : 1237)"
        case IntType => fieldName
        case LongType => s"(int)($fieldName ^ ($fieldName >>> 32))"
        case FloatType => s"Float.floatToIntBits($fieldName)"
        case DoubleType => s"(int)(Double.doubleToLongBits($fieldName) ^ (Double.doubleToLongBits($fieldName) >>> 32))"
        case _ => s"(($fieldName == null) ? 0 : $fieldName.hashCode())"
    }

    def builderMemberDeclaration = field.dataType match {
      case OptionalType(_) => s"private $fieldType $fieldName = ${JavaType.Optional}.empty();"
      case _ => s"private $fieldObjectType $fieldName;"
    }

    def builderSetter = s"""
public Builder with$capitalFieldName($fieldType $fieldName) {
    this.$fieldName = $fieldName;
    return this;
}
    """.trim

  }

  def toCode: String = {
    val structName = struct.name
    val fields = struct.fields.map(new JavaField(_))

    val fieldNames = mapJoin(fields, ", ") { _.fieldName }
    val methodArguments = mapJoin(fields, ", ") { _.declaration }
    val memberDeclarations = mapJoin(fields, "\n") { _.memberDeclaration }
    val memberAssignments = mapJoin(fields, "\n") { _.memberAssignment }
    val nullChecks = mapJoin(fields.filterNot(_.isPrimitive), "\n") { _.nullCheck }
    val builderNullChecks = mapJoin(fields.filter(_.isPrimitive), "\n") { _.nullCheck }
    val getterMethods = mapJoin(fields, "\n\n") { _.getterMethod }
    val equalChecks = mapJoin(fields, "\n") { _.equalCheck }
    val hashCodeAppenders = mapJoin(fields, "\n") { "hash = 37 * hash + "+ _.hashValue +";" }

    val builderMemberDeclarations = mapJoin(fields, "\n") { _.builderMemberDeclaration }
    val builderSetters = mapJoin(fields, "\n\n") { _.builderSetter }

    ai"""
package $moduleName;

public class $structName {

    $memberDeclarations

    public $structName($methodArguments) {
        $nullChecks
        $memberAssignments
    }

    $getterMethods

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null) return false;
        if (!(other instanceof $structName)) return false;
        $structName that = ($structName) other;
        $equalChecks
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        $hashCodeAppenders
        return hash;
    }

    public static class Builder {

        $builderMemberDeclarations

        $builderSetters

        public $structName build() {
            $builderNullChecks
            return new $structName($fieldNames);
        }

    }

}
    """
  }

}
