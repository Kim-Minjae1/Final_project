package com.fiveLink.linkOffice;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpringWebConfig implements WebMvcConfigurer {
	// 기본 파일 설정
    private String mappingDigital = "/linkOfficeImg/**";
    private String locationDigital = "file:///C:/linkoffice/upload/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(mappingDigital)
                .addResourceLocations(locationDigital);
    }
    // cors 오류 관련 메소드(날씨 api)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
    	registry.addMapping("/**")
    		.allowedOrigins("http://localhost:8080");
    }
}

