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
package io.encoded.jersik.scala.runtime.servlet

import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import io.encoded.jersik.scala.runtime.ObjectCodec
import io.encoded.jersik.scala.runtime.RpcCodec
import com.fasterxml.jackson.core.JsonProcessingException

case class RpcOperation[T, R](requestCodec: ObjectCodec[T], function: T => R, responseCodec: ObjectCodec[R])

abstract class RpcServlet(operationMap: Map[String, RpcOperation[_, _]], codec: RpcCodec) extends HttpServlet {

  override def doPost(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    operationMap.get(getOperationName(request)) match {
      case Some(operation) => process(request, operation, response)
      case None => response.sendError(HttpServletResponse.SC_NOT_FOUND)
    }
  }

  private def process[T, R](request: HttpServletRequest, operation: RpcOperation[T, R], response: HttpServletResponse) {
    val requestObject = try {
      codec.decode(request.getInputStream(), operation.requestCodec)
    } catch {
      case e: JsonProcessingException => {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST)
        return
      }
    }
    val responseObject = operation.function.apply(requestObject)
    response.setContentType(codec.contentType)
    codec.encode(responseObject, response.getOutputStream(), operation.responseCodec)
  }

  private def getOperationName(request: HttpServletRequest): String = {
    val basePath = request.getContextPath() + request.getServletPath() +"/"
    request.getRequestURI().substring(basePath.length())
  }

}
