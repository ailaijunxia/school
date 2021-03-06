package com.jichuangsi.school.questionsrepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class SchoolQuestionsRepositoryserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(com.jichuangsi.school.questionsrepository.SchoolQuestionsRepositoryserviceApplication.class, args);
	}
}
