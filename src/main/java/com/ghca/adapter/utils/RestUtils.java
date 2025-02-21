/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.ghca.adapter.utils;

import com.cloud.apigateway.sdk.utils.Client;
import com.cloud.apigateway.sdk.utils.Request;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

/**
 * RestUtils
 *
 * @version v1.0
 * @description:
 * @author: SU on 2020/3/26 15:34
 * @since 2022-02-10
 */
@Component
public class RestUtils {
    private static final String DEFAULT_X_AUTH_TOKEN = "X-Auth-Token";

    private static final int INITIAL_CAPACITY = 2;

    private static RestTemplate restTemplate;

    @Autowired
    private RestTemplate restTemplateValue;

    private static Logger logger = LoggerFactory.getLogger(RestUtils.class);

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        restTemplate = restTemplateValue;
    }

    /**
     * buildUrl
     *
     * @param hostAddress hostAddress
     * @param uri         uri
     * @return String
     */
    public static String buildUrl(String hostAddress, String uri) {
        return hostAddress + uri;
    }

    /**
     * buildUrl
     *
     * @param scheme scheme
     * @param host   host
     * @param port   port
     * @param uri    uri
     * @return String
     */
    public static String buildUrl(String scheme, String host, String port, String uri) {
        return scheme + "://" + host  + uri;
    }

    /**
     * get
     *
     * @param uri           uri
     * @param headers       headers
     * @param uriVariable   uriVariable
     * @param queryVariable queryVariable
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> get(String uri, Map<String, String> headers, Map<String, Object> uriVariable,
                                            Map<String, Object> queryVariable, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.GET, headers, uriVariable, queryVariable, null, clazz, ak, sk);
    }

    /**
     * get
     *
     * @param uri           uri
     * @param uriVariable   uriVariable
     * @param queryVariable queryVariable
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> get(String uri, Map<String, Object> uriVariable,
                                            Map<String, Object> queryVariable, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.GET, null, uriVariable, queryVariable, null, clazz, ak, sk);
    }

    /**
     * get
     *
     * @param uri           uri
     * @param token         token
     * @param queryVariable queryVariable
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> get(String uri, String token, Map<String, Object> queryVariable,
                                            Class<T> clazz,String ak, String sk) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.GET, headers, null, queryVariable, null, clazz,null,null);
    }

    /**
     * get
     *
     * @param uri           uri
     * @param queryVariable queryVariable
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> get(String uri, Map<String, Object> queryVariable, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.GET, null, null, queryVariable, null, clazz, ak, sk);
    }

    /**
     * get
     *
     * @param uri   uri
     * @param token token
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> get(String uri, String token, Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.GET, headers, null, null, null, clazz,null,null);
    }

    /**
     * get
     *
     * @param uri   uri
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> get(String uri, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.GET, null, null, null, null, clazz, ak, sk);
    }

    /**
     * getByJson
     *
     * @param url     url
     * @param token   token
     * @param jsonMap json格式的查询条件
     * @param clazz   返回值类型
     * @param <T>     T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> getByJson(String url, String token, Map<String, String> jsonMap,
                                                  Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        HttpHeaders httpHeaders = buildHeader(headers);
        HttpEntity httpEntity = new HttpEntity(null, httpHeaders);
        String conditionStr = null;
        String executeUrl = url;
        if (jsonMap != null && jsonMap.size() > 0) {
            for (Map.Entry<String, String> entry : jsonMap.entrySet()) {
                conditionStr = entry.getValue();
                executeUrl = url + (url.contains("?") ? "&" : "?") + entry.getKey() + "={json}";
            }
        }
        return restTemplate.exchange(executeUrl, HttpMethod.GET, httpEntity, clazz, conditionStr);
    }

    /**
     * post
     *
     * @param uri           uri
     * @param headers       headers
     * @param uriVariable   uriVariable
     * @param queryVariable queryVariable
     * @param body          body
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> post(String uri, Map<String, String> headers, Map<String, Object> uriVariable,
                                             Map<String, Object> queryVariable, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.POST, headers, uriVariable, queryVariable, body, clazz, ak, sk);
    }

    /**
     * post
     *
     * @param uri         uri
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> post(String uri, Map<String, Object> uriVariable, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.POST, null, uriVariable, null, body, clazz, ak, sk);
    }

    /**
     * post
     *
     * @param uri   uri
     * @param body  body
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> post(String uri, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.POST, null, null, null, body, clazz, ak, sk);
    }


    /**
     * post
     *
     * @param uri         uri
     * @param token       token
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> post(String uri, String token, Map<String, Object> uriVariable, Object body,
                                             Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.POST, headers, uriVariable, null, body, clazz,null,null);
    }

    /**
     * post
     *
     * @param uri   uri
     * @param token token
     * @param body  body
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> post(String uri, String token, Object body, Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.POST, headers, null, null, body, clazz, null, null);
    }

    /**
     * put
     *
     * @param uri           uri
     * @param headers       headers
     * @param uriVariable   uriVariable
     * @param queryVariable queryVariable
     * @param body          body
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> put(String uri, Map<String, String> headers, Map<String, Object> uriVariable,
                                            Map<String, Object> queryVariable, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.PUT, headers, uriVariable, queryVariable, body, clazz, ak, sk);
    }

    /**
     * put
     *
     * @param uri         uri
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> put(String uri, Map<String, Object> uriVariable, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.PUT, null, uriVariable, null, body, clazz, ak, sk);
    }

    /**
     * put
     *
     * @param uri         uri
     * @param token       token
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> put(String uri, String token, Map<String, Object> uriVariable, Object body,
                                            Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.PUT, headers, uriVariable, null, body, clazz,null,null);
    }

    /**
     * put
     *
     * @param uri         uri
     * @param token       token
     * @param headers     headers
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> put(String uri, String token, Map<String, String> headers,
                                            Map<String, Object> uriVariable, Object body, Class<T> clazz) {
        Map headersTemp = new HashMap<String, String>(INITIAL_CAPACITY);
        if (StringUtils.isNotBlank(token)) {
            headersTemp.put(DEFAULT_X_AUTH_TOKEN, token);
        }

        if (headers != null && headers.size() > 0) {
            headersTemp.putAll(headers);
        }
        return execute(uri, HttpMethod.PUT, headersTemp, uriVariable, null, body, clazz,null,null);
    }

    /**
     * put
     *
     * @param uri   uri
     * @param body  body
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> put(String uri, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.PUT, null, null, null, body, clazz, ak, sk);
    }

    /**
     * put
     *
     * @param uri   uri
     * @param token token
     * @param body  body
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> put(String uri, String token, Object body, Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.PUT, headers, null, null, body, clazz, null, null);
    }

    /**
     * patch
     *
     * @param uri           uri
     * @param headers       headers
     * @param uriVariable   uriVariable
     * @param queryVariable queryVariable
     * @param body          body
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> patch(String uri, Map<String, String> headers, Map<String, Object> uriVariable,
                                              Map<String, Object> queryVariable, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.PATCH, headers, uriVariable, queryVariable, body, clazz, ak, sk);
    }

    /**
     * patch
     *
     * @param uri         uri
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> patch(String uri, Map<String, Object> uriVariable, Object body,
                                              Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.PATCH, null, uriVariable, null, body, clazz, ak, sk);
    }

    /**
     * patch
     *
     * @param uri         uri
     * @param token       token
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> patch(String uri, String token, Map<String, Object> uriVariable, Object body,
                                              Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.PATCH, headers, uriVariable, null, body, clazz, null, null);
    }

    /**
     * patch
     *
     * @param uri   uri
     * @param body  body
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> patch(String uri, Object body, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.PATCH, null, null, null, body, clazz, ak, sk);
    }

    /**
     * patch
     *
     * @param uri   uri
     * @param token token
     * @param body  body
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> patch(String uri, String token, Object body, Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.PATCH, headers, null, null, body, clazz, null, null);
    }

    /**
     * delete
     *
     * @param uri           uri
     * @param headers       headers
     * @param uriVariable   uriVariable
     * @param queryVariable queryVariable
     * @param clazz         clazz
     * @param <T>           T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> delete(String uri, Map<String, String> headers, Map<String, Object> uriVariable,
                                               Map<String, Object> queryVariable, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.DELETE, headers, uriVariable, queryVariable, null, clazz, ak, sk);
    }

    /**
     * delete
     *
     * @param uri         uri
     * @param uriVariable uriVariable
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> delete(String uri, Map<String, Object> uriVariable, Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.DELETE, null, uriVariable, null, null, clazz, ak, sk);
    }

    /**
     * delete
     *
     * @param uri         uri
     * @param uriVariable uriVariable
     * @param body        body
     * @param clazz       clazz
     * @param <T>         T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> delete(String uri, Map<String, Object> uriVariable, Object body,
                                               Class<T> clazz,String ak, String sk) {
        return execute(uri, HttpMethod.DELETE, null, uriVariable, null, body, clazz, ak, sk);
    }

    /**
     * delete
     *
     * @param uri   uri
     * @param token token
     * @param clazz clazz
     * @param <T>   T
     * @return ResponseEntity
     */
    public static <T> ResponseEntity<T> delete(String uri, String token, Class<T> clazz) {
        Map headers = new HashMap<String, String>(INITIAL_CAPACITY);
        headers.put(DEFAULT_X_AUTH_TOKEN, token);
        return execute(uri, HttpMethod.DELETE, headers, null, null, null, clazz, null, null);
    }

    /**
     * HTTP/HTTPS请求
     *
     * @param uri           请求地址
     * @param httpMethod    请求方法
     * @param headers       请求头
     * @param uriVariables  路径参数
     * @param queryVariable 查询参数
     * @param body          请求体
     * @param clazz         返回值类型
     * @return ResponseEntity
     */
    private static <T> ResponseEntity<T> execute(String uri, HttpMethod httpMethod, Map<String, String> headers,
                                                 Map<String, Object> uriVariables, Map<String, Object> queryVariable,
                                                 Object body, Class<T> clazz, String ak, String sk) {
        Request httpClientRequest = new Request();
        try {
            httpClientRequest.setKey(ak);
            httpClientRequest.setSecret(sk);
            httpClientRequest.setMethod(httpMethod.name());
            httpClientRequest.setUrl(buildUri(uri, uriVariables, queryVariable).toString());
            Map<String, String> headerMap = buildHeaderMap(headers);
            for (Map.Entry<String, String> entry : headerMap.entrySet()) {
                httpClientRequest.addHeader(entry.getKey(), entry.getValue());
            }
            if (body != null){
                httpClientRequest.setBody(JsonUtils.parseObject2Str(body));
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("build request error", e);
            throw new RuntimeException(e);
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        try {
            HttpRequestBase signedRequest = Client.sign(httpClientRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256);
            for (int i = 0; i < signedRequest.getAllHeaders().length; i++) {
                Header header = signedRequest.getAllHeaders()[i];
                // 设置请求头
                httpHeaders.set(header.getName(), header.getValue());
            }
        } catch (Exception e) {
            logger.error("sign error", e);
            throw new RuntimeException(e);
        }

        HttpEntity httpEntity = new HttpEntity(body, httpHeaders);
        ResponseEntity<T> responseEntity = restTemplate.exchange(buildUri(uri, uriVariables, queryVariable), httpMethod,
                httpEntity, clazz);
        return responseEntity;
    }

    /**
     * 组装请求头
     *
     * @param headers headers
     * @return ResponseEntity
     */
    private static HttpHeaders buildHeader(Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        // 设置请求头
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.set(entry.getKey(), entry.getValue());
            }
        }
        return httpHeaders;
    }

    private static Map<String, String> buildHeaderMap(Map<String, String> headers) {
        if (headers == null){
            headers = new HashMap<>();
        }
        if(!headers.containsKey("Content-Type")){
            headers.put("Content-Type", "application/json; charset=UTF-8");
        }
        if(!headers.containsKey("Accept-Charset")){
            headers.put("Accept-Charset", "UTF-8");
        }
        if(!headers.containsKey("Accept")){
            headers.put("Accept", "application/json; charset=UTF-8");
        }
        return headers;
    }

    /**
     * 组装查询参数
     *
     * @param uri           uri
     * @param queryVariable queryVariable
     * @return URI
     */
    private static URI buildUri(String uri, Map<String, Object> uriVariables, Map<String, Object> queryVariable) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromUriString(uri);
        if (Objects.nonNull(uriVariables)) {
            uriComponentsBuilder.uriVariables(uriVariables);
        }
        if (queryVariable != null) {
            for (Map.Entry<String, Object> entry : queryVariable.entrySet()) {
                uriComponentsBuilder.queryParam(entry.getKey(), entry.getValue());
            }
        }
        return uriComponentsBuilder.build().encode().toUri();
    }
}
