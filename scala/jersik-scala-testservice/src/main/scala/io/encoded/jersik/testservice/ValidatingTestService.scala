package io.encoded.jersik.testservice

import io.encoded.jersik.testsuite._
import io.encoded.jersik.scala.runtime.JsonCodec
import io.encoded.jersik.scala.runtime.ObjectCodec
import java.io.FileInputStream

object ValidatingTestService extends TestService {

  def testEmpty(request: Empty): Empty =
    validateAndReturn("Empty.json", request, EmptyCodec)

  def testScalarFieldsA(request: ScalarFields): ScalarFields =
    validateAndReturn("ScalarFieldsA.json", request, ScalarFieldsCodec)

  def testScalarFieldsB(request: ScalarFields): ScalarFields =
    validateAndReturn("ScalarFieldsB.json", request, ScalarFieldsCodec)

  def testScalarFieldsC(request: ScalarFields): ScalarFields =
    validateAndReturn("ScalarFieldsC.json", request, ScalarFieldsCodec)

  def testOptionalFieldsA(request: OptionalFields): OptionalFields =
    validateAndReturn("OptionalFieldsA.json", request, OptionalFieldsCodec)

  def testOptionalFieldsB(request: OptionalFields): OptionalFields =
    validateAndReturn("OptionalFieldsB.json", request, OptionalFieldsCodec)

  def testListFieldsA(request: ListFields): ListFields =
    validateAndReturn("ListFieldsA.json", request, ListFieldsCodec)

  def testListFieldsB(request: ListFields): ListFields =
    validateAndReturn("ListFieldsB.json", request, ListFieldsCodec)

  def testMapFieldsA(request: MapFields): MapFields =
    validateAndReturn("MapFieldsA.json", request, MapFieldsCodec)

  def testMapFieldsB(request: MapFields): MapFields =
    validateAndReturn("MapFieldsB.json", request, MapFieldsCodec)

  def testStructFieldsA(request: StructFields): StructFields =
    validateAndReturn("StructFieldsA.json", request, StructFieldsCodec)

  def testStructFieldsB(request: StructFields): StructFields =
    validateAndReturn("StructFieldsB.json", request, StructFieldsCodec)

  private def validateAndReturn[T](fixture: String, request: T, objectCodec: ObjectCodec[T]): T = {
    val expected = JsonCodec.decode(new FileInputStream("../../test-suite/fixtures/"+ fixture), objectCodec)
    if (request != expected) {
      throw new AssertionError(fixture +": expected: "+ expected +" but was: "+ request)
    }
    request
  }

}
