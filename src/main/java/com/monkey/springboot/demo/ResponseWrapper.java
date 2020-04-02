package com.monkey.springboot.demo;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ResponseWrapper extends HttpServletResponseWrapper {
    private ByteArrayOutputStream output;
    private ServletOutputStream filterOutput;

    public ResponseWrapper(HttpServletResponse response) {
        super(response);
        output = new ByteArrayOutputStream();
    }

    /**
     * 巧妙将ServletOutputStream放到公共变量，解决不能多次读写问题
     *
     * @return
     * @throws IOException
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (filterOutput == null) {
            filterOutput = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    output.write(b);
                }

                @Override
                public boolean isReady() {
                    return false;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                }
            };
        }
        return filterOutput;
    }

    public byte[] toByteArray() {
        return output.toByteArray();
    }
}
