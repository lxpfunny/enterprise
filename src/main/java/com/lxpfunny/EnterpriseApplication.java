package com.lxpfunny;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EnterpriseApplication {
	private static Logger logger = LoggerFactory.getLogger(EnterpriseApplication.class);
	public static void main(String[] args) {
		logger.info("启动应用");
		SpringApplication.run(EnterpriseApplication.class, args);
	}
}
