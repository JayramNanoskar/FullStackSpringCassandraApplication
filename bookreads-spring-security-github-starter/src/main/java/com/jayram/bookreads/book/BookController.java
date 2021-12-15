package com.jayram.bookreads.book;

import java.util.Optional;

import com.jayram.bookreads.userbooks.UserBooks;
import com.jayram.bookreads.userbooks.UserBooksPrimaryKey;
import com.jayram.bookreads.userbooks.UserBooksRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {
    
    private final String COVER_IMAGE_ROOT = "https://covers.openlibrary.org/b/id/";

    @Autowired
    BookRepository bookRepository;

    @Autowired
    UserBooksRepository userBooksRepository;

    @GetMapping(value = "/books/{bookId}")
    //To get the things from the model in template, we need to do DI on model here so that we can put things on it
    public String getBook(@PathVariable String bookId, Model model, @AuthenticationPrincipal OAuth2User principal) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if(optionalBook.isPresent()) {
            Book book = optionalBook.get();
            String coverImageUrl = "/images/no-image.png";
            if(book.getCoverIds() != null && book.getCoverIds().size()>0) {
                coverImageUrl = COVER_IMAGE_ROOT +book.getCoverIds().get(0)+"-L.jpg"; //Creating url to leverage Covers API from OpenLibrary
            }
            model.addAttribute("coverImage", coverImageUrl);
            model.addAttribute("book", book);
            
            if(principal != null && principal.getAttribute("login") != null) {
                String userId = principal.getAttribute("login");
                model.addAttribute("loginId", userId);

                UserBooksPrimaryKey key = new UserBooksPrimaryKey();
                key.setBookId(bookId);
                key.setUserId(userId);

                Optional<UserBooks> userBooks = userBooksRepository.findById(key); //Fetching user interaction with book
                if(userBooks.isPresent()) {
                    model.addAttribute("userBooks", userBooks.get());
                } else {
                    model.addAttribute("userBooks", new UserBooks());
                }
            }
            return "book"; //Tells to render book.html
        }
        return "book-not-found"; //Tells to render book-not-found.html
    }
}
