package com.jiawa.train.member.config;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;

/** 会员微服务启动类。 */
@SpringBootApplication
@ComponentScan("com.jiawa")
@MapperScan("com.jiawa.train.*.mapper")
public class MemberApplication {

    private static final Logger LOG = LoggerFactory.getLogger(MemberApplication.class);

    /**
     * 应用启动入口，启动后打印本地测试地址。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MemberApplication.class);
        Environment env = app.run(args).getEnvironment();
        LOG.info("启动成功！！");
        LOG.info("测试地址: \thttp://127.0.0.1:{}{}/hello", env.getProperty("server.port"), env.getProperty("server.servlet.context-path"));
    }
}
