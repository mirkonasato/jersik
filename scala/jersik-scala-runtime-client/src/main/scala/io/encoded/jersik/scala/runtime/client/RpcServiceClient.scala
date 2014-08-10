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
package io.encoded.jersik.scala.runtime.client

import org.apache.http.client.HttpClient
import java.net.URI
import io.encoded.jersik.scala.runtime.ObjectCodec
import org.apache.http.HttpEntity
import org.apache.http.client.methods.HttpPost
import org.apache.http.HttpStatus
import org.apache.http.client.HttpResponseException
import io.encoded.jersik.scala.runtime.RpcCodec
import org.apache.http.entity.EntityTemplate
import org.apache.http.entity.ContentProducer
import java.io.OutputStream

class RpcContentProducer[T](request: T, requestCodec: ObjectCodec[T], rpcCodec: RpcCodec) extends ContentProducer {

  override def writeTo(out: OutputStream): Unit = rpcCodec.encode(request, out, requestCodec)

}

abstract class RpcServiceClient(httpClient: HttpClient, serviceUri: URI, rpcCodec: RpcCodec) {

  protected def execute[T, R](operationName: String, request: T, requestCodec: ObjectCodec[T], responseCodec: ObjectCodec[R]): R = {
    val operationUri = serviceUri.resolve(operationName)
    val post = new HttpPost(operationUri)
    val entity = new EntityTemplate(new RpcContentProducer(request, requestCodec, rpcCodec))
    entity.setContentType(rpcCodec.contentType)
    post.setEntity(entity)
    val httpResponse = httpClient.execute(post)
    val statusLine = httpResponse.getStatusLine();
    if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
      consume(httpResponse.getEntity());
      throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
    }
    rpcCodec.decode(httpResponse.getEntity().getContent(), responseCodec)
  }

  private def consume(entity: HttpEntity): Unit = {
    if (entity != null && entity.isStreaming()) {
      val in = entity.getContent()
      if (in != null) in.close()
    }
  }

}
