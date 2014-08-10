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

class ScalaServiceClient(moduleName: String, service: Service) {

  def toCode: String = {
    val serviceName = service.name
    val operations = mapJoin(service.operations , "\n\n") { operation =>
      val operationName = operation.name
      val requestType = operation.requestType.name
      val responseType = operation.responseType.name
      ai"""
def $operationName(request: $requestType): $responseType =
  execute("$operationName", request, ${requestType}Codec, ${responseType}Codec)
      """
    }

    ai"""
package $moduleName

import java.net.URI
import java.io.IOException
import org.apache.http.client.HttpClient
import io.encoded.jersik.scala.runtime.RpcCodec
import io.encoded.jersik.scala.runtime.client.RpcServiceClient

class ${serviceName}Client(httpClient: HttpClient, serviceUri: URI, rpcCodec: RpcCodec)
    extends RpcServiceClient(httpClient, serviceUri, rpcCodec) with ${serviceName} {

    $operations

}
    """
  }

}
