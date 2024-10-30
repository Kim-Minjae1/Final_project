package com.fiveLink.linkOffice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 스케줄러 동작 가능 
@MapperScan("com.fiveLink.linkOffice.mapper") // 패키지 설정 확인
public class LinkOfficeApplication {

	public static void main(String[] args) {
		SpringApplication.run(LinkOfficeApplication.class, args);
	}

}

