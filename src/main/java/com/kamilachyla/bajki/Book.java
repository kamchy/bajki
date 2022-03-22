package com.kamilachyla.bajki;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
/**
 * Book record with empty or by-number sorted list of {@link BookPage} pages.
 * @param title title of a book
 * @param author author of the book
 * @param titleImagePath path to cover image (relative to toml location)
 * @param pages list of {@link BookPage} pages; it is sorted according to page number and not null
 * */
public record Book(String title, String author, String titleImagePath, String footer, List<BookPage> pages) {
    public Book {
        pages = Optional.ofNullable(pages)
                .orElse(List.of())
                .stream().sorted(Comparator.comparing(BookPage::number))
                .toList();
    }

    public Book withEmptyPages() {
        return new Book(title, author, titleImagePath, footer, null);
    }
}
