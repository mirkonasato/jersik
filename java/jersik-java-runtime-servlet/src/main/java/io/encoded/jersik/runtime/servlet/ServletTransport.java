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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTransport implements RpcTransport {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public ServletTransport(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public InputStream getRequestStream() throws IOException {
        return request.getInputStream();
    }

    @Override
    public void sendInvalidRequestResponse(IOException cause) throws IOException {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    @Override
    public void setResponseContentType(String contentType) {
        response.setContentType(contentType);
    }

    @Override
    public OutputStream getResponseStream() throws IOException {
        return response.getOutputStream();
    }

}
