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
package io.encoded.jersik.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

public final class JsonCodec implements RpcCodec {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    public static final JsonCodec INSTANCE = new JsonCodec();

    private JsonCodec() { /* singleton */ }

    @Override
    public String getContentType() {
        return "application/json";
    }

    public <T> T decode(InputStream in, ObjectCodec<T> codec) throws IOException {
        JsonParser parser = JSON_FACTORY.createParser(in);
        try {
            parser.nextToken();
            return codec.decode(parser);
        } finally {
            parser.close();
        }
    }

    public <T> void encode(T instance, OutputStream out, ObjectCodec<T> codec) throws IOException {
        JsonGenerator generator = JSON_FACTORY.createGenerator(out);
        try {
            codec.encode(generator, instance);
        } finally {
            generator.close();
        }
    }

}
