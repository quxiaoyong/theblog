package org.fantasizer.theblog.admin.config;

import feign.auth.BasicAuthRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TODO:用户名和密码是硬编码
 *
 * @Author Cruise Qu
 * @Date 2020-01-29 22:42
 */
@Configuration
public class FeignConfiguration {

    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("quxiaoyong", "quxiaoyong2020");
    }

}
