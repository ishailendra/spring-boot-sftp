package dev.techsphere.sftpconfigurer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI openAPIConfiguration() {
        return new OpenAPI().info(appInfo());
    }
    private Info appInfo() {
        return new Info().title("SFTP AutoConfiguration Service")
                .description("SFTP AutoConfiguration Service")
                .version("1.0")
                .contact(apiContact())
                .license(apiLicense());
    }

    private Contact apiContact() {
        return new Contact().name("Techsphere.dev").email("shail@techsphere.dev");
    }

    private License apiLicense() {
        return new License().name("APACHE LICENSE, VERSION 2.0").url("https://www.apache.org/licenses/LICENSE-2.0");
    }
}
