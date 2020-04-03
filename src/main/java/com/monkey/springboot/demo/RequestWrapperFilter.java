package com.monkey.springboot.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.monkey.springboot.demo.utils.AesEncryptUtils;
import com.monkey.springboot.demo.utils.RSAUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.monkey.springboot.demo.advice.EncodeResponseBodyAdvice.getRandomString;

@Order(Ordered.LOWEST_PRECEDENCE - 1)
@WebFilter(urlPatterns = {"/testEncrypt"},filterName = "requestWrapperFilter")
public class RequestWrapperFilter extends OncePerRequestFilter {
    @Value("${server.private.key}")
    private String SERVER_PRIVATE_KEY;
    @Value("${client.public.key}")
    private String CLIENT_PUBLIC_KEY;
    @Value("${aes.private.key}")
    private String AES_PRIVATE_KEY;

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestWrapperFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        RequestWrapper requestWrapper = new RequestWrapper(request);
        ResponseWrapper responseWrapper = new ResponseWrapper(response);
        try {
            decodeReq(request,requestWrapper);
            encodeRes(response,responseWrapper);
            super.doFilter(requestWrapper,responseWrapper,filterChain);
        } catch (Exception e) {
            LOGGER.error("数据包装器执行出错....{}", e);
        }
    }

    /**
     * 解析验证请求参数
     * @param httpServletRequest
     * @param requestWrapper
     * @throws Exception
     */
    private void decodeReq(HttpServletRequest httpServletRequest,RequestWrapper requestWrapper) throws Exception {
        String requestStr = new String(requestWrapper.toByteArray(),httpServletRequest.getCharacterEncoding());
        Map<String,String> requestMap = new Gson().fromJson(requestStr,new TypeToken<Map<String,String>>() {}.getType());
        // 密文
        String data = requestMap.get("requestData");
        // 加密的ase秘钥
        String clientencrypted = requestMap.get("encrypted");

        if(StringUtils.isEmpty(data) || StringUtils.isEmpty(clientencrypted)){
            throw new RuntimeException("参数【requestData】缺失异常！");
        }else{
            String content = null;
            String aseKey = null;
            try {
                aseKey = RSAUtils.decryptDataOnJava(clientencrypted,SERVER_PRIVATE_KEY);
            }catch (Exception e){
                throw  new RuntimeException("参数【aseKey】解析异常！");
            }
            try {
                content  = AesEncryptUtils.decrypt(data, aseKey);
            }catch (Exception e){
                throw  new RuntimeException("参数【content】解析异常！");
            }
            if (StringUtils.isEmpty(content) || StringUtils.isEmpty(aseKey)){
                throw  new RuntimeException("参数【requestData】解析参数空指针异常!");
            }
            httpServletRequest.getInputStream().read(JSONObject.toJSONBytes(content));
        }
    }

    /**
     * 加密返回参数
     * @param httpServletResponse
     * @param responseWrapper
     */
    private void encodeRes(HttpServletResponse httpServletResponse,ResponseWrapper responseWrapper) throws Exception{
        String responseStr = new String(responseWrapper.toByteArray(), httpServletResponse.getCharacterEncoding());
        // 生成aes秘钥
        String aseKey = getRandomString(16);
        // rsa加密
        String serverencrypted = RSAUtils.encryptedDataOnJava(aseKey, CLIENT_PUBLIC_KEY);
        // aes加密
        String responseStrAes = AesEncryptUtils.encrypt(responseStr, aseKey);
        Map<String, String> map = new HashMap<>();
        map.put("encrypted", serverencrypted);
        map.put("responseData", responseStrAes);
        LOGGER.info("response is ============{}",JSONObject.toJSONString(map));
        //必须设置ContentLength
        httpServletResponse.setContentLength(JSONObject.toJSONString(map).length());
        httpServletResponse.getOutputStream().write(JSON.toJSONBytes(map));
    }
}
