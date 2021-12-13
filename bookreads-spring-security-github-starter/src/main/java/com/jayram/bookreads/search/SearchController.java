package com.jayram.bookreads.search;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Controller
public class SearchController {

    private final String COVER_IMAGE_ROOT = "https://covers.openlibrary.org/b/id/";
    
    private final WebClient webClient;

    public SearchController(WebClient.Builder webClientBuilder) { //Using Webclient To make Rest api call to Search API from OpenLibrary 
        this.webClient = webClientBuilder.exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)).build())
                .baseUrl("http://openlibrary.org/search.json").build(); // Building a webclient
    }

    @GetMapping(value = "/search")
    //To get the things from the model in template, we need to do DI on model here so that we can put things on it
    public String getSearchResult(@RequestParam String query, Model model) {
        Mono<SearchResult> resultsMono = this.webClient.get()
                .uri("?q={query}", query)
                .retrieve().bodyToMono(SearchResult.class); // Building a single request for the webclient that's already built
        SearchResult result = resultsMono.block();
        List<SearchResultBook> books = result.getDocs().stream().map(bookResult -> {
            bookResult.setKey(bookResult.getKey().replace("/works/", ""));
            String coverId = bookResult.getCover_i();
            if(StringUtils.hasText(coverId)) {
                coverId = COVER_IMAGE_ROOT + coverId+"-M.jpg";
            }
            else {
                coverId = "/images/no-image.png";
            }
            bookResult.setCover_i(coverId);
            return bookResult;
        }).collect(Collectors.toList());
        
        model.addAttribute("searchResults", books);
        return "search"; //Tells to render search.html template
    }
}
