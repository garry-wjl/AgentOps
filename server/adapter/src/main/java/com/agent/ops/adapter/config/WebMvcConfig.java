package com.agent.ops.adapter.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置，注册拦截器与 CORS。
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 管理员权限拦截器。
     */
    @Resource
    private AdminAuthInterceptor adminAuthInterceptor;

    /**
     * 访问令牌解析拦截器。
     */
    @Resource
    private TokenAuthInterceptor tokenAuthInterceptor;

    /**
     * 注册拦截器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/api/users/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
}