/**
 * Copyright (c) Huawei Technologies Co., Ltd. 2022-2022. All rights reserved.
 */

package com.ghca.adapter.config;

import org.apache.commons.io.IOUtils;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.net.ssl.SSLContext;

/**
 * RestTemplate配置类
 *
 * @version v1.0
 * @description:
 * @author: SU on 2020/3/25 14:27
 * @since 2022-03-01
 */
@Configuration
public class RestTemplateConfig {
    private static Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    private static final String UTF_8 = "UTF-8";

    private static final int ONE_THOUSAND = 1000;

    private static final String HEADERS = "Headers";

    private static final String URI = "URI";

    private static final String METHOD = "Method";

    private static final String JOINER = ": ";

    @Value("${spring.restTemplate.readTimeout}")
    private int readTimeout;

    @Value("${spring.restTemplate.connectTimeout}")
    private int connectTimeout;

    @Value("${spring.restTemplate.connectionRequestTimeout}")
    private int connectionRequestTimeout;

    @Value("${spring.restTemplate.httpclient.maxTotal}")
    private int maxTotal;

    @Value("${spring.restTemplate.httpclient.maxConnectPerRoute}")
    private int maxConnectPerRoute;

    @Value("${spring.restTemplate.httpclient.enableRetry}")
    private boolean isEnableRetry;

    @Value("${spring.restTemplate.httpclient.retryTimes}")
    private int retryTimes;

    @Value("#{${spring.restTemplate.httpclient.keepAliveTargetHosts}}")
    private Map<String, Long> keepAliveTargetHosts;

    @Value("${spring.restTemplate.httpclient.keepAliveTime}")
    private long keepAliveTime;

    /**
     * 配置RestTemplate
     *
     * @param clientHttpRequestFactory clientHttpRequestFactory
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);

        // 默认使用ISO-8859-1编码，此处修改为UTF-8
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        Iterator<HttpMessageConverter<?>> iterator = messageConverters.iterator();
        while (iterator.hasNext()) {
            HttpMessageConverter<?> converter = iterator.next();
            if (converter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) converter).setDefaultCharset(Charset.forName(UTF_8));
            }
        }

        // 设置自定义异常处理
        restTemplate.setErrorHandler(new MyErrorHandler());

        // 设置日志拦截器
        restTemplate.getInterceptors().add(new LoggingInterceptor());
        return restTemplate;
    }

    /**
     * 配置httpclient工厂
     *
     * @param httpClient httpClient
     * @return ClientHttpRequestFactory
     */
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setHttpClient(httpClient);

        // 客户端和服务端建立连接的超时时间,这里最大只能是20s,因为linux操作系统的tcp进行三次握手的默认超时时间是20s,,即便设置100s也是在20s后报错（单位毫秒）
        factory.setConnectTimeout(connectTimeout);

        // 即socketTime,数据响应读取超时时间，指的是两个相邻的数据包的间隔超时时间（单位毫秒）
        factory.setReadTimeout(readTimeout);

        // 连接不够用时从连接池获取连接的等待时间，必须设置。不宜过长，连接不够用时，等待时间过长将是灾难性的（单位毫秒）
        factory.setConnectionRequestTimeout(connectionRequestTimeout);

        // 必须使用BufferingClientHttpRequestFactory这个包装类，否则默认实现只允许读取一次响应流，日志输出那里读取之后，请求调用处再读取时会报错
        BufferingClientHttpRequestFactory bufferingClientHttpRequestFactory = new BufferingClientHttpRequestFactory(
            factory);
        return bufferingClientHttpRequestFactory;
    }

    /**
     * 配置httpclient
     *
     * @param connectionKeepAliveStrategy connectionKeepAliveStrategy
     * @return HttpClient
     */
    @Bean
    public HttpClient httpClient(ConnectionKeepAliveStrategy connectionKeepAliveStrategy) {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (X509Certificate[] var1, String var2) -> true)
                .build();
        } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            logger.error(e.getMessage());
        }
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext,
            NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry
            = RegistryBuilder.<ConnectionSocketFactory>create().register("http",
            PlainConnectionSocketFactory.getSocketFactory()).register("https", sslConnectionSocketFactory).build();

        // 使用Httpclient连接池的方式配置(推荐)，同时支持netty，okHttp以及其他http框架
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(
            socketFactoryRegistry);

        // 最大tcp连接数
        poolingHttpClientConnectionManager.setMaxTotal(maxTotal);

        // 同路由最大tcp连接数
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(maxConnectPerRoute);
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // 配置连接池
        httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);

        // 是否允许重试和重试次数
        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(retryTimes, isEnableRetry));

        // 设置默认请求头
        // 设置长连接保持策略
        httpClientBuilder.setKeepAliveStrategy(connectionKeepAliveStrategy);

        // 禁止httpclient自动重定向，禁止cookie
        httpClientBuilder.setDefaultRequestConfig(
            RequestConfig.custom().setRedirectsEnabled(false).setCookieSpec(CookieSpecs.IGNORE_COOKIES).build());
        return httpClientBuilder.build();
    }

    /**
     * 配置长连接策略
     *
     * @return ConnectionKeepAliveStrategy
     */
    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            // Honor 'keep-alive' header
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            logger.debug("HeaderElement:{}", it);
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && "timeout".equalsIgnoreCase(param)) {
                    try {
                        // 1.服务器有时候会告诉客户端长连接的超时时间，如果有则设置为服务器的返回值
                        return Long.parseLong(value) * ONE_THOUSAND;
                    } catch (NumberFormatException ignore) {
                        logger.error("Parse long connection expiration time exception!", ignore);
                    }
                }
            }

            // 2.如果服务器没有返回超时时间则采用配置的时间
            // a.如果请求目标地址,单独配置了长连接保持时间,使用该配置 b.否则使用配置的的默认长连接保持时间keepAliveTime
            HttpHost target = (HttpHost) context.getAttribute(HttpClientContext.HTTP_TARGET_HOST);
            Optional<Map.Entry<String, Long>> any = Optional.ofNullable(keepAliveTargetHosts)
                .orElseGet(HashMap::new)
                .entrySet()
                .stream()
                .filter(e -> e.getKey().equalsIgnoreCase(target.getHostName()))
                .findAny();
            return any.map(e -> e.getValue()).orElse(keepAliveTime);
        };
    }

    /**
     * 自定义异常处理
     *
     * @since 2022-03-01
     */
    private class MyErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return !response.getStatusCode().is2xxSuccessful();
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // 响应状态码错误时不抛出异常
            // throw new HttpClientErrorException(response.getStatusCode(), response.getStatusText(),
            //     response.getHeaders(), IOUtils.toByteArray(response.getBody()), Charset.forName(UTF_8));
        }
    }

    /**
     * 日志拦截器
     *
     * @since 2022-03-01
     */
    private class LoggingInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
            traceRequest(request, body);
            long start = System.currentTimeMillis();
            ClientHttpResponse response = execution.execute(request, body);
            long end = System.currentTimeMillis();
            traceResponse(body, request, response, (end - start) / (float)ONE_THOUSAND);
            return response;
        }

        /**
         * traceRequest
         *
         * @param request request
         * @param body body
         */
        private void traceRequest(HttpRequest request, byte[] body) {
            StringBuilder log = new StringBuilder();
            log.append(System.lineSeparator())
                .append("===========================request begin===========================")
                .append(System.lineSeparator())
                .append(URI)
                .append(JOINER)
                .append(request.getURI())
                .append(System.lineSeparator())
                .append(METHOD)
                .append(JOINER)
                .append(request.getMethod())
                // .append(System.lineSeparator())
                // .append(HEADERS)
                // .append(JOINER)
                // .append(request.getHeaders())
                .append(System.lineSeparator())
                .append("Request body: ")
                .append(new String(body, Charset.forName(UTF_8)))
                .append(System.lineSeparator())
                .append("===========================request end==============================");
            logger.info(log.toString());
        }

        private void traceResponse(byte[] body, HttpRequest request, ClientHttpResponse response, float time)
            throws IOException {
            StringBuilder inputStringBuilder = new StringBuilder();
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), UTF_8))) {
                String line = bufferedReader.readLine();
                while (line != null) {
                    inputStringBuilder.append(line);
                    line = bufferedReader.readLine();
                }
            }
            if (response.getStatusCode().is2xxSuccessful()) {
                printSuccess(response, time, request, inputStringBuilder);
            } else {
                printError(response, time, request, inputStringBuilder, body);
            }
        }

        /**
         * 打印成功日志
         *
         * @param response
         * @param time
         * @param request
         * @param inputStringBuilder
         * @throws IOException
         */
        private void printSuccess(ClientHttpResponse response, float time, HttpRequest request,
            StringBuilder inputStringBuilder) throws IOException {
            StringBuilder log = new StringBuilder();
            log.append(System.lineSeparator())
                .append("===========================response begin===========================")
                .append(System.lineSeparator())
                .append("TIME: ")
                .append(time)
                .append("s")
                .append(System.lineSeparator())
                .append(URI)
                .append(JOINER)
                .append(request.getURI())
                .append(System.lineSeparator())
                .append(METHOD)
                .append(JOINER)
                .append(request.getMethod())
                .append(System.lineSeparator())
                .append("Status code: ")
                .append(response.getStatusCode())
                .append(System.lineSeparator())
                .append(HEADERS)
                .append(JOINER)
                .append(response.getHeaders())
                .append(System.lineSeparator())
                .append("Response body: ")
                .append(inputStringBuilder.toString())
                .append(System.lineSeparator())
                .append("===========================response end==============================");
            logger.info(log.toString());
        }

        /**
         * 打印错误日志
         *
         * @param response
         * @param time
         * @param request
         * @param inputStringBuilder
         * @param body
         * @throws IOException
         */
        private void printError(ClientHttpResponse response, float time, HttpRequest request,
            StringBuilder inputStringBuilder, byte[] body) throws IOException {
            StringBuilder log = new StringBuilder();
            log.append(System.lineSeparator())
                .append("===========================response begin===========================")
                .append(System.lineSeparator())
                .append("TIME: ")
                .append(time)
                .append("s")
                .append(System.lineSeparator())
                .append(URI)
                .append(JOINER)
                .append(request.getURI())
                .append(System.lineSeparator())
                .append(METHOD)
                .append(JOINER)
                .append(request.getMethod())
                .append(System.lineSeparator())
                .append("Headers: ")
                .append(request.getHeaders())
                .append(System.lineSeparator())
                .append("Request body : ")
                .append(new String(body, Charset.forName(UTF_8)))
                .append(System.lineSeparator())
                .append("Status code: ")
                .append(response.getStatusCode())
                .append(System.lineSeparator())
                .append(HEADERS)
                .append(JOINER)
                .append(response.getHeaders())
                .append(System.lineSeparator())
                .append("Response body: ")
                .append(inputStringBuilder.toString())
                .append(System.lineSeparator())
                .append("===========================response end==============================");
            logger.error(log.toString());
        }
    }
}
