package dev.techsphere.sftpconfigurer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableIntegration
@EnableScheduling
public class SftpConfigurerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SftpConfigurerApplication.class, args);
	}

	@Bean
	public MessagingTemplate messagingTemplate() {
		return new MessagingTemplate();
	}

}
