package com.ghca.adapter.model.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @version v1.0
 * @description:
 * @author: SU on 2025/2/11 15:47
 */
@Component
@ConfigurationProperties(prefix = "cloud")
public class CloudProperties {

    private String scheme;
    private String host;
    private Map<String, String> api;

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, String> getApi() {
        return api;
    }

    public void setApi(Map<String, String> api) {
        this.api = api;
    }
}
