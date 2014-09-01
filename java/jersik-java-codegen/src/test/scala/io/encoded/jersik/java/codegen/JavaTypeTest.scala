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

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import io.encoded.jersik.schema._

@RunWith(classOf[JUnitRunner])
class JavaTypeTest extends FlatSpec with Matchers {

  "A Boolean" should "be a boolean" in {
    JavaType.forType(BooleanType) should be("boolean")
  }

  "An Int" should "be an int" in {
    JavaType.forType(IntType) should be("int")
  }

  "A Long" should "be a long" in {
    JavaType.forType(LongType) should be("long")
  }

  "A Float" should "be a float" in {
    JavaType.forType(FloatType) should be("float")
  }

  "A Double" should "be a double" in {
    JavaType.forType(DoubleType) should be("double")
  }

  "A String" should "be a String" in {
    JavaType.forType(StringType) should be("String")
  }

  "An optional Int" should "be an Optional<Integer>" in {
    JavaType.forType(OptionalType(IntType)) should be("io.encoded.jersik.runtime.types.Optional<Integer>")
  }

  "An optional String" should "be an Optional<String>" in {
    JavaType.forType(OptionalType(StringType)) should be("io.encoded.jersik.runtime.types.Optional<String>")
  }

  "A List" should "be a List" in {
    JavaType.forType(ListType(StringType)) should be("java.util.List<String>")
  }

  "A Map" should "be a Map" in {
    JavaType.forType(MapType(StringType, IntType)) should be("java.util.Map<String, Integer>")
  }

  "A custom struct" should "be its name" in {
    JavaType.forType(CustomType("Foo")) should be("Foo")
  }

}
