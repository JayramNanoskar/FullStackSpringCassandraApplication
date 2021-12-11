package com.jayram.bookreadsdataloader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import com.jayram.bookreadsdataloader.author.Author;
import com.jayram.bookreadsdataloader.author.AuthorRepository;
import com.jayram.bookreadsdataloader.book.Book;
import com.jayram.bookreadsdataloader.book.BookRepository;
import com.jayram.bookreadsdataloader.connection.DataStaxAstraProperties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${datadump.location.author}")
	private String authorsDumpLocation;

	@Value("${datadump.location.works}")
	private String worksDumpLocation;

	@Autowired
	BookRepository bookRepository;

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
		initAuthors();
		initWorks();
	}

	private void initAuthors() {
		Path path = Paths.get(authorsDumpLocation); 
		try (Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {
				//Read and Parse the line
				String jsonString = line.substring(line.indexOf("{"));
				try {
					JSONObject jsonObject = new JSONObject(jsonString);

					//Construct Author object
					Author author = new Author();
					author.setName(jsonObject.optString("name"));
					author.setPersonalName(jsonObject.optString("personal_name"));
					author.setId(jsonObject.optString("key").replace("/authors/", ""));
					
					//Persist using Repository
					System.out.println("Saving Author "+author.getName()+"...");
					authorRepository.save(author);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			
			});
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	private void initWorks() {
		Path path = Paths.get(worksDumpLocation); 
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
		try (Stream<String> lines = Files.lines(path)) {
			lines.forEach(line -> {

				//Read and Parse the line
				String jsonString = line.substring(line.indexOf("{"));
				try {
					JSONObject jsonObject = new JSONObject(jsonString);

					//Construct Book object
					Book book = new Book();
					book.setId(jsonObject.getString("key").replace("/works/", ""));

					book.setName(jsonObject.optString("title"));

					JSONObject descriptionObj = jsonObject.optJSONObject("description");
					if(descriptionObj != null) {
						book.setDescription(descriptionObj.optString("value"));
					}

					JSONObject publishedObj = jsonObject.optJSONObject("created");
					if(publishedObj != null) {
						String dateString = publishedObj.optString("value");
						book.setPublishedDate(LocalDate.parse(dateString, dateFormat));
					}

					JSONArray coversJSONArray = jsonObject.optJSONArray("covers");
					if(coversJSONArray != null) {
						List<String> coverIds = new ArrayList<>();
						for(int i = 0; i < coversJSONArray.length(); i++) {
							coverIds.add(coversJSONArray.getString(i));
						}
						book.setCoverIds(coverIds);
					}

					JSONArray authorsJSONArray = jsonObject.optJSONArray("authors");
					if(authorsJSONArray != null) {
						List<String> authorIds = new ArrayList<>();
						for(int i = 0; i < authorsJSONArray.length(); i++) {
							String authorId = authorsJSONArray.getJSONObject(i).getJSONObject("author").getString("key")
											.replace("/authors/", "");
							authorIds.add(authorId);
						}
						book.setAuthorIds(authorIds);

						List<String> authorNames = authorIds.stream().map(id -> authorRepository.findById(id))
									.map(optionalAuthor -> {
										if(!optionalAuthor.isPresent()) return "Unknown Author";
										return optionalAuthor.get().getName();
									}).collect(Collectors.toList());
						book.setAuthorNames(authorNames);

						//Persist using Repository
						System.out.println("Saving Book "+book.getName()+"...");
						bookRepository.save(book); 
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
			});
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

}
