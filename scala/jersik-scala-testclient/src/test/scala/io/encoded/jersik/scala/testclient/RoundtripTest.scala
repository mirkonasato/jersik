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
package io.encoded.jersik.scala.testclient

import java.net.URI
import org.apache.http.impl.client.HttpClients
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import io.encoded.jersik.scala.runtime.JsonCodec
import org.scalatest.junit.JUnitRunner
import java.io.FileInputStream
import io.encoded.jersik.scala.runtime.ObjectCodec
import org.scalatest.FunSuite
import io.encoded.jersik.testsuite._

@RunWith(classOf[JUnitRunner])
class RoundtripTest extends FunSuite with Matchers {

  val client = new TestServiceClient(HttpClients.createDefault(), URI.create("http://localhost:8080/test/"), JsonCodec)

  roundtripTest("Empty.json", EmptyCodec, client.testEmpty)
  roundtripTest("ScalarFieldsA.json", ScalarFieldsCodec, client.testScalarFieldsA)
  roundtripTest("ScalarFieldsB.json", ScalarFieldsCodec, client.testScalarFieldsB)
  roundtripTest("ScalarFieldsC.json", ScalarFieldsCodec, client.testScalarFieldsC)
  roundtripTest("OptionalFieldsA.json", OptionalFieldsCodec, client.testOptionalFieldsA)
  roundtripTest("OptionalFieldsB.json", OptionalFieldsCodec, client.testOptionalFieldsB)
  roundtripTest("ListFieldsA.json", ListFieldsCodec, client.testListFieldsA)
  roundtripTest("ListFieldsB.json", ListFieldsCodec, client.testListFieldsB)
  roundtripTest("MapFieldsA.json", MapFieldsCodec, client.testMapFieldsA)
  roundtripTest("MapFieldsB.json", MapFieldsCodec, client.testMapFieldsB)
  roundtripTest("StructFieldsA.json", StructFieldsCodec, client.testStructFieldsA)
  roundtripTest("StructFieldsB.json", StructFieldsCodec, client.testStructFieldsB)

  private def roundtripTest[T](fixture: String, objectCodec: ObjectCodec[T], operation: T => T): Unit = {
    test(fixture) {
      val request = JsonCodec.decode(new FileInputStream("../../test-suite/fixtures/"+ fixture), objectCodec)
      println(fixture +": "+ request)
      val response = operation(request)
      assert(response == request)
    }
  }

}
