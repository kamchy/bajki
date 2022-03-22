package com.kamilachyla.bajki;

import java.util.List;
import java.util.Optional;

public record Book(String title, String author, String titleImagePath, String footer, List<BookPage> pages) {
    public Book {
        pages = Optional.ofNullable(pages).orElse(List.of());
    }

    public Book withEmptyPages() {
        return new Book(title, author, titleImagePath, footer, null);
    }
}
