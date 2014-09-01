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
package io.encoded.jersik.runtime.client;

import io.encoded.jersik.runtime.JsonCodec;
import io.encoded.jersik.runtime.ObjectCodec;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;

public abstract class AbstractServiceClient {

    private final HttpClient httpClient;
    private final URI serviceUri;

    public AbstractServiceClient(HttpClient httpClient, URI serviceUri) {
        this.httpClient = httpClient;
        this.serviceUri = serviceUri;
    }

    protected <S, T> T execute(String operationName, S request, ObjectCodec<S> requestCodec, ObjectCodec<T> responseCodec) throws IOException {
        URI operationUri = serviceUri.resolve(operationName);
        HttpPost post = new HttpPost(operationUri);
        post.setEntity(new RequestEntity<S>(request, requestCodec, JsonCodec.INSTANCE));
        HttpResponse httpResponse = httpClient.execute(post);
        StatusLine statusLine = httpResponse.getStatusLine();
        if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
            consume(httpResponse.getEntity());
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());
        }
        return JsonCodec.INSTANCE.decode(httpResponse.getEntity().getContent(), responseCodec);
    }

    // the httpclient version included in Android lacks EntityUtils.consume()
    private static void consume(HttpEntity entity) throws IOException {
        if (entity != null && entity.isStreaming()) {
            InputStream in = entity.getContent();
            if (in != null) in.close();
        }
    }

}
