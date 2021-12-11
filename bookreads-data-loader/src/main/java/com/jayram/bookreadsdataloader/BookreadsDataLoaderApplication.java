package com.jayram.bookreadsdataloader;

import java.nio.file.Path;

import javax.annotation.PostConstruct;

import com.jayram.bookreadsdataloader.author.Author;
import com.jayram.bookreadsdataloader.author.AuthorRepository;
import com.jayram.bookreadsdataloader.connection.DataStaxAstraProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.cassandra.CqlSessionBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;



@SpringBootApplication
@EnableConfigurationProperties(DataStaxAstraProperties.class) //To load the configuration
public class BookreadsDataLoaderApplication {

	@Autowired
	AuthorRepository authorRepository;

	public static void main(String[] args) {
		SpringApplication.run(BookreadsDataLoaderApplication.class, args);
	}


	//Exposes bean necessary for app to use the Astra secure bundle to connect to the database
	@Bean
	public CqlSessionBuilderCustomizer sessionBuilderCustomizer(DataStaxAstraProperties astraProperties) {
		Path path = astraProperties.getSecureConnectBundle().toPath();
		return builder -> builder.withCloudSecureConnectBundle(path);
	}

	//To run after everything get constructed
	@PostConstruct
	public void start() {
		Author author = new Author();
		author.setId("3");
		author.setName("Ok");
		author.setPersonalName("All right");
		authorRepository.save(author);
	}
}
