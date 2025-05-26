package com.project.manager.finance_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FinanceManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceManagementApplication.class, args);
	}

}
