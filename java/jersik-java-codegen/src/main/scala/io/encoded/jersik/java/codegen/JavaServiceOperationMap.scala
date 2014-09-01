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

class JavaServiceOperationMap(moduleName: String, service: Service) {

  def toCode: String = {
    val serviceName = service.name
    val operationMapAdders = mapJoin(service.operations, "\n\n") { operation =>
      val operationName = operation.name
      val requestType = operation.requestType.name
      val responseType = operation.responseType.name
      ai"""
operationByName.put("$operationName", new RpcOperation<$requestType, $responseType>(${requestType}Codec.INSTANCE, ${responseType}Codec.INSTANCE) {
    protected $responseType apply($requestType request) throws IOException {
        return service.$operationName(request);
    }
});
      """
    }

    ai"""
package $moduleName;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import io.encoded.jersik.runtime.servlet.RpcOperation;
import io.encoded.jersik.runtime.servlet.RpcOperationMap;

public class ${serviceName}OperationMap implements RpcOperationMap {

    private final Map<String, RpcOperation<?, ?>> operationByName;

    public ${serviceName}OperationMap(final $serviceName service) {
        Map<String, RpcOperation<?, ?>> operationByName = new HashMap<String, RpcOperation<?, ?>>();
        $operationMapAdders
        this.operationByName = Collections.unmodifiableMap(operationByName);
    }

    public boolean contains(String operationName) {
        return operationByName.containsKey(operationName);
    }

    public RpcOperation<?, ?> get(String operationName) {
        return operationByName.get(operationName);
    }

}
    """
  }

}
