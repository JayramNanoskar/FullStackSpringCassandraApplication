package com.jayram.bookreads.book;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class BookController {
    
    private final String COVER_IMAGE_ROOT = "https://covers.openlibrary.org/b/id/";

    @Autowired
    BookRepository bookRepository;

    @GetMapping(value = "/books/{bookId}")
    //To get the things from the model in template, we need to do DI on model here so that we can put things on it
    public String getBook(@PathVariable String bookId, Model model) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if(optionalBook.isPresent()) {
            Book book = optionalBook.get();
            String coverImageUrl = "/images/no-image.png";
            if(book.getCoverIds() != null && book.getCoverIds().size()>0) {
                coverImageUrl = COVER_IMAGE_ROOT +book.getCoverIds().get(0)+"-L.jpg";
            }
            model.addAttribute("coverImage", coverImageUrl);
            model.addAttribute("book", book);
            return "book"; //Tells to render book.html
        }
        return "book-not-found"; //Tells to render book-not-found.html
    }
}
