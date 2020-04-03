package com.monkey.springboot.demo;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class RequestWrapper extends HttpServletRequestWrapper {


    private Map<String , String[]> params;
    private Map<String , String> headers;

    private ByteArrayOutputStream output;
    private ServletInputStream filterInput;




    public RequestWrapper(HttpServletRequest request) {
        super(request);
        output = new ByteArrayOutputStream();
        this.params = new HashMap<String, String[]>();
        this.params.putAll(request.getParameterMap());
        this.headers = new HashMap<String, String>();
    }


    @Override
    public String getHeader(String name) {
        // check the custom headers first
        String headerValue = headers.get(name);
        if (headerValue != null){
            return headerValue;
        }
        // else return from into the original wrapped object
        return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        // create a set of the custom header names
        Set<String> set = new HashSet<>(headers.keySet());
        // now add the headers from the wrapped request object
        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (e.hasMoreElements()) {
            // add the names of the request headers into the list
            String n = e.nextElement();
            set.add(n);
        }
        // create an enumeration from the set and return
        return Collections.enumeration(set);
    }

    public Enumeration<String> getHeaderNames() {
        // create a set of the custom header names
        Set<String> set = new HashSet<>(headers.keySet());

        // now add the headers from the wrapped request object
        Enumeration<String> e = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (e.hasMoreElements()) {
            // add the names of the request headers into the list
            String n = e.nextElement();
            set.add(n);
        }

        // create an enumeration from the set and return
        return Collections.enumeration(set);
    }


    @Override
    public String getParameter(String name) {
        String[]values = params.get(name);
        if(values == null || values.length == 0) {
            return null;
        }
        return values[0];
    }


    @Override
    public String[] getParameterValues(String name) {
        return params.get(name);
    }




    public void putParams(Map<String , String[]> otherParams) {
        for(Map.Entry<String , String[]>entry : otherParams.entrySet()) {
            addParameter(entry.getKey() , entry.getValue());
        }
    }


    public void addParameter(String name , Object value) {
        if(value != null) {
            if(value instanceof String[]) {
                params.put(name , (String[])value);
            }else if(value instanceof String) {
                params.put(name , new String[] {(String)value});
            }else {
                params.put(name , new String[] {String.valueOf(value)});
            }
        }
    }

    public void putHeaders(Map<String,String> otherHeaders) {
        for(Map.Entry<String , String>entry : otherHeaders.entrySet()) {
            addHeader(entry.getKey() , entry.getValue());
        }
    }



    void addHeader(String name, String value){
        this.headers.put(name, value);
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
