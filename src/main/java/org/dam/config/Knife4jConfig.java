package org.dam.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j (OpenAPI3) 接口文档配置
 * 文档访问地址：<a href="http://localhost:8080/doc.html">...</a>
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@Configuration
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 文档信息
     *
     * @return OpenAPI 实例
     */
    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("dam-server 接口文档")
                        .description("Spring Boot + MyBatis Plus 基础架构接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("zhengbing")
                                .email("zhengbing@dam.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }

}
