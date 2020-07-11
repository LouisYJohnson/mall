package com.ibook.mall;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//这个文件是用于配置拦截器的
//加了这个注解才会启动执行,否则不会
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new UserLoginInterceptor()) //将我们写的拦截器注册进去
                .addPathPatterns("/**")    //默认对所有url都拦截,但是应该除去注册与登录的url
                .excludePathPatterns("/user/register",
                        "/user/login",
                        "/categories",
                        "/products",
                        "/products/*");  //不需要拦截的很少
    }
}
