package com.jiawa.train.business.config;

import com.jiawa.train.common.interceptor.LogInterceptor;
import com.jiawa.train.common.interceptor.MemberInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Spring MVC 拦截器配置 —— 区分用户端与管理端的鉴权策略。 */
@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

   @Resource
   LogInterceptor logInterceptor;

   @Resource
   MemberInterceptor memberInterceptor;

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(logInterceptor)
               .addPathPatterns("/**");

       registry.addInterceptor(memberInterceptor)
               .addPathPatterns("/**")
               .excludePathPatterns(
                       "/hello",
                       "/admin/**",
                       "/daily-train-ticket/warm-up",
                       "/daily-train-ticket/query-list-db"
               );
   }
}
