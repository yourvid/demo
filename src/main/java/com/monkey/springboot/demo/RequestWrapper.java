package com.monkey.springboot.demo;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RequestWrapper extends HttpServletRequestWrapper {
    private ByteArrayOutputStream output;
    private ServletInputStream filterInput;

    public RequestWrapper(HttpServletRequest response) {
        super(response);
        output = new ByteArrayOutputStream();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (filterInput == null) {
            filterInput = new ServletInputStream() {

                @Override
                public int read() throws IOException {
                    return filterInput.read();
                }

                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setReadListener(ReadListener readListener) {

                }
            };
        }
        return filterInput;
    }

    public byte[] toByteArray() {
        return output.toByteArray();
    }
}
