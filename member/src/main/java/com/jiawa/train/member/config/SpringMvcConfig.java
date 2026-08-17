package com.jiawa.train.member.config;

import com.jiawa.train.common.interceptor.LogInterceptor;
import com.jiawa.train.common.interceptor.MemberInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Spring MVC 拦截器配置类。 */
@Configuration
public class SpringMvcConfig implements WebMvcConfigurer {

   @Resource
   LogInterceptor logInterceptor;

   @Resource
   MemberInterceptor memberInterceptor;

   /**
    * 注册拦截器链：先记录日志，再解析会员身份。
    * 发码、登录、健康检查接口排除 MemberInterceptor，避免未登录用户被拦截。
    *
    * @param registry Spring MVC 拦截器注册器
    */
   @Override
   public void addInterceptors(InterceptorRegistry registry) {
       registry.addInterceptor(logInterceptor);

       registry.addInterceptor(memberInterceptor)
               .addPathPatterns("/**")
               .excludePathPatterns(
                       "/member/hello",
                       "/member/member/send-code",
                       "/member/member/login"
               );
   }
}
