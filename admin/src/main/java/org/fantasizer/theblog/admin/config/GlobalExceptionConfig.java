package org.fantasizer.theblog.admin.config;

import org.fantasizer.theblog.common.handler.TheBlogHandlerExceptionResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author Cruise Qu
 * @Date 2020-01-29 22:43
 */
@Configuration
public class GlobalExceptionConfig {

    @Bean
    public TheBlogHandlerExceptionResolver getHandlerExceptionResolver() {
        return new TheBlogHandlerExceptionResolver();
    }
}
