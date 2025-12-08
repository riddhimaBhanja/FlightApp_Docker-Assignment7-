package com.flightapp.booking.config;

import feign.Logger;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
public class FeignClientConfig {

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public HttpMessageConverters messageConverters() {
        HttpMessageConverter<?> jacksonConverter = new MappingJackson2HttpMessageConverter();
        return new HttpMessageConverters(jacksonConverter);
    }
}
