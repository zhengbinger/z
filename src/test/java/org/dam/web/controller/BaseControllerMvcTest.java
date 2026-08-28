package org.dam.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dam.common.exception.GlobalExceptionHandler;
import org.dam.web.TestWebApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Controller Web 切片测试公共基类
 *
 * 使用 TestWebApplication（不扫描 org.dam.service/component/mapper/config 包）作为启动类，
 * 排除 DataSource/MyBatis/Redis 自动配置，避免初始化数据库连接池、Redis 连接池等重资源。
 * 子类需用 @Import({XxxController.class}) 导入要测试的 Controller，并 @MockBean 其 Service 依赖。
 *
 * 替代 @WebMvcTest 的原因：项目主类 Application 的 @ComponentScan("org.dam") + @MapperScan("org.dam.mapper")
 * 会覆盖 @WebMvcTest 默认的 TypeExcludeFilter 过滤策略，导致所有 @RestController、@Mapper、@Aspect、
 * @Configuration 被扫描，触发依赖链（Mapper → sqlSessionFactory、RedisConfig → RedisConnectionFactory），
 * 切片测试无这些配置，启动失败。
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@SpringBootTest(classes = TestWebApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public abstract class BaseControllerMvcTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
