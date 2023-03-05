package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.authors.AuthorMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;
    private final AuthorService authorService;
    private AuthorMapper authorMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper, AuthorMapper authorMapper,
            AuthorService authorService) {
        this.bookService = bookService;
        this.booksMapper = booksMapper;
        this.authorMapper = authorMapper;
        this.authorService = authorService;
    }

    @GetMapping("/books")
    public Collection<BookDTO> books(@RequestParam(value = "q", required = false) String query) {
        Collection<Book> books;
        if (query == null) {
            books = bookService.list();
        } else {
            books = bookService.findByTitle(query);
        }
        return books.stream()
                .map(booksMapper::entityToDTO)
                .toList();
    }

    @GetMapping("/books/{id}")
    public BookDTO book(@PathVariable("id") Long id) {
        try {
            return booksMapper.entityToDTO(bookService.get(id));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
        }
    }

    @PostMapping("/authors/{authorId}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO newBook(@PathVariable("authorId") Long authorId, @RequestBody @Valid BookDTO book) {
        try {
            return booksMapper.entityToDTO(bookService.save(authorId, booksMapper.dtoToEntity(book)));
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, null, e);
        }
    }

    @PutMapping("/books/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO updateBook(@PathVariable("id") Long authorId, @RequestBody @Valid BookDTO book) {
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est
        // mauvaise
        if (authorId != book.id()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            Book updated = this.bookService.update(this.booksMapper.dtoToEntity(book));
            return this.booksMapper.entityToDTO(updated);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The book was not found");
        }
    }

    @DeleteMapping("/books/{id}")
    public void deleteBook(@PathVariable("id") Long id) {
        try {
            bookService.delete(id);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, null, e);
        }
    }

    @PutMapping("/books/{authorId}/authors")
    @ResponseStatus(HttpStatus.OK)
    public void addAuthor(@PathVariable("authorId") Long authorId, @RequestBody @Valid AuthorDTO author) {
        if (authorId != author.id()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        try {
            Author upd = this.authorService.update(this.authorMapper.dtoToEntity(author));
            this.authorMapper.entityToDTO(upd);
        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "The book was not found");
        }
    }

}
