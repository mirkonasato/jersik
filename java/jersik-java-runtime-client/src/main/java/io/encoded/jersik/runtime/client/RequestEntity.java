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

import io.encoded.jersik.runtime.ObjectCodec;
import io.encoded.jersik.runtime.RpcCodec;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;

class RequestEntity<T> extends EntityTemplate {

    private static class JsonContentProducer<T> implements ContentProducer {

        private final T request;
        private final ObjectCodec<T> requestCodec;
        private final RpcCodec codec;

        public JsonContentProducer(T request, ObjectCodec<T> requestCodec, RpcCodec codec) {
            this.request = request;
            this.requestCodec = requestCodec;
            this.codec = codec;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            codec.encode(request, out, requestCodec);
        }
    }

    public RequestEntity(T request, ObjectCodec<T> requestCodec, RpcCodec codec) {
        super(new JsonContentProducer<T>(request, requestCodec, codec));
        setContentType(codec.getContentType());
    }

}
