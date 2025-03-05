package com.ghca.adapter.config;

import com.google.common.collect.Sets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .pathMapping("/")
                .enable(true)
                .apiInfo(new ApiInfoBuilder()
                .title("Swagger API").version("1.0").build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ghca.adapter.controller"))
                .paths(PathSelectors.any())
                .build()
                .protocols(Sets.newHashSet("http"));
    }
}
