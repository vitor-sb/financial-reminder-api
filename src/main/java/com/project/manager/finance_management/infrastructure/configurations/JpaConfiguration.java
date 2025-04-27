package com.project.manager.finance_management.infrastructure.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.project.manager.finance_management.domains.repositories.jpa")
public class JpaConfiguration {

}
