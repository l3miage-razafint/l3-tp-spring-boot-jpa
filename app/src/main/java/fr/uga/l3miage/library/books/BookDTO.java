package fr.uga.l3miage.library.books;

import fr.uga.l3miage.library.authors.AuthorDTO;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;

public record BookDTO(
        @NotNull Long id,
        String title,
        long isbn,
        String publisher,
        short year,
        String language,
        Collection<AuthorDTO> authors
) {
}
