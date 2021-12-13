package com.jayram.bookreads;

import java.nio.file.Path;

import com.jayram.bookreads.connection.DataStaxAstraProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableConfigurationProperties(DataStaxAstraProperties.class) //To load the configuration
public class BookReadsSpringSecurityGithubStarterApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookReadsSpringSecurityGithubStarterApplication.class, args);
	}

	@RequestMapping("/user")
	public String user(@AuthenticationPrincipal OAuth2User principal) {
		return principal.getAttribute("name");
	}

	//Exposes bean necessary for app to use the Astra secure bundle to connect to the database
	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path path = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(path);
	}
}
