package com.zhang.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement //开启事务支持
//在SpringBootApplication上使用@ServletComponentScan注解后，
//Servlet（控制器）、Filter（过滤器）、Listener（监听器）
// 可以直接通过@WebServlet、@WebFilter、@WebListener注解自动注册到Spring容器中，无需其他代码。
@EnableCaching //开启缓存注解功能 springcache
public class ReggieApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class,args);
        log.info("项目启动成功");
    }
}
