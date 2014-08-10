package io.encoded.jersik.testservice

import io.encoded.jersik.scala.runtime.JsonCodec
import io.encoded.jersik.scala.runtime.servlet.RpcServlet
import io.encoded.jersik.testsuite.TestServiceOperations

class TestServiceServlet extends RpcServlet(TestServiceOperations.toMap(ValidatingTestService), JsonCodec)
