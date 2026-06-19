package com.agent.ops.adapter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * AgentOps 平台后端应用启动类。
 */
@SpringBootApplication(scanBasePackages = "com.agent.ops")
@MapperScan(basePackages = "com.agent.ops.infra")
@EnableScheduling
public class AgentOpsApplication {
    /**
     * 应用入口方法。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AgentOpsApplication.class, args);
    }
}