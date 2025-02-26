/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2018-2023. All rights reserved.
 */

package com.huawei.apig.sdk.demo;

// Import the external dependencies.
import com.huawei.apig.sdk.util.Constant;
import com.huawei.apig.sdk.util.HostName;
import com.huawei.apig.sdk.util.SSLCipherSuiteUtil;

import com.cloud.apigateway.sdk.utils.Client;
import com.cloud.apigateway.sdk.utils.Request;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientDemo {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientDemo.class);

    public static void main(String[] args) throws Exception {
        // Create a new request.
        Request httpClientRequest = new Request();
        try {
            // Set the request parameters.
            // AppKey, AppSecrect, Method and Url are required parameters.
            // 认证用的ak和sk硬编码到代码中或者明文存储都有很大的安全风险，建议在配置文件或者环境变量中密文存放，使用时解密，确保安全；
            // 本示例以ak和sk保存在环境变量中为例，运行本示例前请先在本地环境中设置环境变量HUAWEICLOUD_SDK_AK和HUAWEICLOUD_SDK_SK。
            httpClientRequest.setKey(System.getenv("HUAWEICLOUD_SDK_AK"));
            httpClientRequest.setSecret(System.getenv("HUAWEICLOUD_SDK_SK"));
            httpClientRequest.setMethod("POST");
            httpClientRequest.setUrl("your url");
            httpClientRequest.addHeader("Content-Type", "text/plain");
            httpClientRequest.setBody("demo");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return;
        }
        CloseableHttpClient client = null;
        try {
            // Sign the request.
            HttpRequestBase signedRequest = Client.sign(httpClientRequest, Constant.SIGNATURE_ALGORITHM_SDK_HMAC_SHA256);
            if (Constant.DO_VERIFY) {
                // creat httpClient and verify ssl certificate
                HostName.setUrlHostName(httpClientRequest.getHost());
                client = (CloseableHttpClient) SSLCipherSuiteUtil.createHttpClientWithVerify(Constant.INTERNATIONAL_PROTOCOL);
            } else {
                // creat httpClient and do not verify ssl certificate
                client = (CloseableHttpClient) SSLCipherSuiteUtil.createHttpClient(Constant.INTERNATIONAL_PROTOCOL);
            }
            HttpResponse response = client.execute(signedRequest);
            // Print the body of the response.
            HttpEntity resEntity = response.getEntity();
            if (resEntity != null) {
                LOGGER.info("Processing Body with name: {} and value: {}", System.getProperty("line.separator"),
                        EntityUtils.toString(resEntity, "UTF-8"));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}