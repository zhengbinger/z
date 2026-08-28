package org.dam.web;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * Controller @WebMvcTest 测试专用启动类
 *
 * 背景：项目主类 Application 配置了 @ComponentScan("org.dam") + @MapperScan("org.dam.mapper")，
 * 会覆盖 @WebMvcTest 默认的 TypeExcludeFilter 过滤策略，导致：
 *   1. 所有 @RestController 被扫描（不止 @WebMvcTest(controllers=...) 指定的那个）
 *   2. 所有 @Mapper 接口被注册，需要 sqlSessionFactory（切片测试无配置）
 *   3. @Aspect/@Component 的 RbacAspect 被加载，依赖 AccessControlService → 依赖 Mapper
 *   4. @Configuration 的 RedisConfig 被加载，依赖 RedisConnectionFactory
 *
 * 本启动类不配置 @ComponentScan("org.dam") 和 @MapperScan("org.dam.mapper")，
 * @SpringBootApplication 默认的 @ComponentScan 只扫描 org.dam.web 包（启动类所在包），
 * 不会扫描 org.dam.service / org.dam.component / org.dam.config / org.dam.mapper 等包。
 *
 * 同时排除 DataSource / MyBatis-Plus / Redis 自动配置：
 *   - 项目用 mybatis-plus-boot-starter，其自动配置类是 MybatisPlusAutoConfiguration，
 *     若不 exclude 它，@Mapper 注解接口会被 AutoConfiguredMapperScannerRegistrar 自动扫描注册，
 *     导致 MapperFactoryBean 因缺少 sqlSessionFactory 抛 NPE。
 *
 * 用法：测试类用 @SpringBootTest(classes = TestWebApplication.class) + @AutoConfigureMockMvc
 *      + @Import({XxxController.class, GlobalExceptionHandler.class}) + @MockBean XxxService
 *
 * @author zhengbing
 * @since 2026-08-28
 **/
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    MybatisPlusAutoConfiguration.class,
    RedisAutoConfiguration.class,
    RedisRepositoriesAutoConfiguration.class
})
public class TestWebApplication {
}
