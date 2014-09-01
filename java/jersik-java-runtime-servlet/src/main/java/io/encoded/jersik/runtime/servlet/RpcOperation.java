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
package io.encoded.jersik.runtime.servlet;

import io.encoded.jersik.runtime.RpcCodec;
import io.encoded.jersik.runtime.ObjectCodec;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;

public abstract class RpcOperation<S, T> {

    private final ObjectCodec<S> requestCodec;
    private final ObjectCodec<T> responseCodec;

    public RpcOperation(ObjectCodec<S> requestCodec, ObjectCodec<T> responseCodec) {
        this.requestCodec = requestCodec;
        this.responseCodec = responseCodec;
    }

    protected abstract T apply(S request) throws IOException;

    public void execute(RpcTransport transport, RpcCodec codec) throws IOException {
        S request;
        try {
            request = codec.decode(transport.getRequestStream(), requestCodec);
        } catch (JsonProcessingException parseException) {
            transport.sendInvalidRequestResponse(parseException);
            return;
        }
        T response = apply(request);
        transport.setResponseContentType(codec.getContentType());
        codec.encode(response, transport.getResponseStream(), responseCodec);
    }

}
