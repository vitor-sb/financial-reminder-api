package com.reminder.financial.api.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.reminder.financial.api.domains.repositories.jpa")
public class JpaConfiguration {

}
