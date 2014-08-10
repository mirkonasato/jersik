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

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SchemaParserTest extends FlatSpec with Matchers {

  "A valid schema" can "be parsed correctly" in {
    val source = """
module com.example

// this is a comment

struct Foo {
  booleanField: Boolean
  intField: Int
  longField: Long
  floatField: Float
  doubleField: Double
  stringField: String
  barField: Bar
  optionalStringField: Optional<String>
  listField: List<String>
  mapField: Map<String, Int>
  complexField: Optional<Map<String, List<Bar>>>
}

enum Suit { Hearts Diamonds Clubs Spades }

struct Bar { }

service TestService {
  add: Foo -> Bar
}
    """
    val result = SchemaParser.parseModule(source)
    assert(result.successful, result)
    val module = SchemaParser.parseModule(source).get
    module shouldBe Module("com.example",
        List(
            Struct("Foo", List(
                Field("booleanField", BooleanType),
                Field("intField", IntType),
                Field("longField", LongType),
                Field("floatField", FloatType),
                Field("doubleField", DoubleType),
                Field("stringField", StringType),
                Field("barField", CustomType("Bar")),
                Field("optionalStringField", OptionalType(StringType)),
                Field("listField", ListType(StringType)),
                Field("mapField", MapType(StringType, IntType)),
                Field("complexField", OptionalType(MapType(StringType, ListType(CustomType("Bar")))))
            )),
            Enum("Suit", List("Hearts", "Diamonds", "Clubs", "Spades")),
            Struct("Bar", List()),
            Service("TestService", List(
                Operation("add", CustomType("Foo"), CustomType("Bar"))))
    ))
  }

  "Undeclared struct field types" should "cause a parsing error" in {
    val source = """
module com.example

struct Foo {
  bar: Bar
}
    """
    val error = evaluating {
      SchemaParser.parseModule(source)
    } should produce[IllegalArgumentException]
    error.getMessage shouldBe "No such struct or enum: 'Bar'; in field 'bar' of struct 'Foo'"
  }

  "Undeclared service operation argument types" should "cause a parsing error" in {
    val source = """
module com.example

struct Foo { }

service TestService {
  add: Foo -> Bar
}
    """
    val error = evaluating {
      SchemaParser.parseModule(source)
    } should produce[IllegalArgumentException]
    error.getMessage shouldBe "No such struct: 'Bar'; in operation 'add' of service 'TestService'"
  }

}
