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

class JavaServiceClient(moduleName: String, service: Service) {

  def toCode: String = {
    val serviceName = service.name
    val operations = mapJoin(service.operations , "\n\n") { operation =>
      val operationName = operation.name
      val requestType = operation.requestType.name
      val responseType = operation.responseType.name
      ai"""
public $responseType $operationName($requestType request) {
    try {
        return execute("$operationName", request, ${requestType}Codec.INSTANCE, ${responseType}Codec.INSTANCE);
    } catch (IOException ioException) {
        throw new RuntimeException(ioException);
    }
}
      """
    }

    ai"""
package $moduleName;

import java.net.URI;
import java.io.IOException;
import org.apache.http.client.HttpClient;
import io.encoded.jersik.runtime.client.AbstractServiceClient;

public class ${serviceName}Client extends AbstractServiceClient implements ${serviceName} {

    public ${serviceName}Client(HttpClient httpClient, URI serviceUri) {
        super(httpClient, serviceUri);
    }

    $operations

}
    """
  }

}
