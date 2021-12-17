package com.jayram.bookreads.userbooks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import com.jayram.bookreads.book.Book;
import com.jayram.bookreads.book.BookRepository;
import com.jayram.bookreads.user.BooksByUser;
import com.jayram.bookreads.user.BooksByUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserBooksController {
    
    @Autowired
    UserBooksRepository userBooksRepository;

    @Autowired
    BooksByUserRepository booksByUserRepository;

    @Autowired
    BookRepository bookRepository;

    @PostMapping("/addUserBook")
    //Using MultiValueMap to hold request value
    public ModelAndView addBookForUser(@RequestBody MultiValueMap<String, String> formData, @AuthenticationPrincipal OAuth2User principal) {
        UserBooks userBooks = new UserBooks();
        UserBooksPrimaryKey key = new UserBooksPrimaryKey();
        if(principal == null || principal.getAttribute("login") == null) {
            return null;
        }
        key.setUserId(principal.getAttribute("login"));
        String bookId = formData.getFirst("bookId");
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        Book book = optionalBook.get();

        key.setBookId(bookId);

        userBooks.setKey(key);

        userBooks.setStartedDate(LocalDate.parse(formData.getFirst("startDate")));
        userBooks.setCompletedDate(LocalDate.parse(formData.getFirst("completedDate")));
        userBooks.setRating(Integer.parseInt(formData.getFirst("rating")));
        userBooks.setReadingStatus(formData.getFirst("readingStatus"));

        userBooksRepository.save(userBooks); //Saving user interaction with book
        
        BooksByUser booksByUser = new BooksByUser();
        booksByUser.setId(principal.getAttribute("login"));
        booksByUser.setBookId(bookId);
        booksByUser.setBookName(book.getName());
        booksByUser.setCoverIds(book.getCoverIds());
        booksByUser.setAuthorNames(book.getAuthorNames());
        booksByUser.setReadingStatus(formData.getFirst("readingStatus"));
        booksByUser.setRating(Integer.parseInt(formData.getFirst("rating")));
        booksByUser.setTimestamp(LocalDateTime.now());
        booksByUserRepository.save(booksByUser);  //Saving detailed user interaction with book

        return new ModelAndView("redirect:/books/" + bookId); // Redirecting to a specify url
    }
}
