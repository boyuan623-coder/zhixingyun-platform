package com.jiawa.train.batch.config;

import com.jiawa.train.common.interceptor.LogInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * batch 模块 Spring MVC 配置。
 * <p>注册拦截器等 Web 层通用配置。
 */

@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

   @Resource
   LogInterceptor logInterceptor;

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(logInterceptor)
               .addPathPatterns("/**");

   }
}
