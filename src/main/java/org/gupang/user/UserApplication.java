package org.gupang.user;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.gupang.common.config.JpaConfig;
import org.springframework.context.annotation.Import;

@Import(JpaConfig.class)
@EnableFeignClients(basePackages = "org.gupang.user.Infrastructure.Client")
@SpringBootApplication
public class UserApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserApplication.class, args);
	}

}
