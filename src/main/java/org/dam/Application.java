package org.dam;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot 应用启动入口
 * 扫描 org.dam 包下的所有组件
 * 扫描 org.dam.mapper 包下的所有 Mapper 接口
 *
 * @author zhengbing
 * @since 2026-08-18
 **/
@SpringBootApplication
@ComponentScan("org.dam")
@MapperScan("org.dam.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
