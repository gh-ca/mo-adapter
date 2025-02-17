package com.ghca.adapter;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

import javax.net.ssl.SSLContext;

@EnableFeignClients(basePackages = "com.ghca.adapter")
@SpringBootApplication(scanBasePackages = "com.ghca.adapter")
public class MoAdapterApplication {

    private static Logger logger = LoggerFactory.getLogger(MoAdapterApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(MoAdapterApplication.class, args);
        logger.info("MoAdapterApplication start success!");
    }

    /**
     * 转发请求忽略证书
     * @return
     * @throws Exception
     */
    @Bean
    public CloseableHttpClient getIgnoeSslClient() throws Exception {
        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, (x, y) -> true).build();
        CloseableHttpClient client = HttpClients.custom().
                setSSLContext(sslContext).setDefaultRequestConfig(RequestConfig.custom().setRedirectsEnabled(false).
                setCookieSpec(CookieSpecs.IGNORE_COOKIES).build())
            .setSSLHostnameVerifier(new NoopHostnameVerifier())
            .build();
        return client;
    }

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory>
    containerCustomizer(){
        return new EmbeddedTomcatCustomizer();
    }

    private static class EmbeddedTomcatCustomizer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
        @Override
        public void customize(TomcatServletWebServerFactory factory) {
            factory.addConnectorCustomizers((TomcatConnectorCustomizer) connector -> {
                connector.setAttribute("relaxedPathChars", "\"<>[\\]^`{|}");
                connector.setAttribute("relaxedQueryChars", "\"<>[\\]^`{|}");
            });
        }
    }

}
