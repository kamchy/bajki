package com.kamilachyla.bajki;


import com.moandjiezana.toml.Toml;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MyTomlParser {
    public static final String NUMBER = "number";
    public static final String IMAGE_PATH = "imagePath";
    public static final String TEXT = "text";
    final String TITLE = "title";
    public static final String AUTHOR = "author";
    public static final String TITLE_IMAGE_PATH = "titleImagePath";
    public static final String FOOTER = "footer";
    public static final String PAGES = "pages";

    public Optional<Book> parse(FileReader fileReader) {
        final var toml = new Toml();
        var res = toml.read(fileReader);
        return Optional.of(
                new Book(res.getString(TITLE), res.getString(AUTHOR),
                        res.getString(TITLE_IMAGE_PATH), res.getString(FOOTER), parsePages(res)));
    }


    private List<BookPage> parsePages(Toml toml) {
        var li = new ArrayList<BookPage>();
        var pagesObjs = Optional.ofNullable(toml.getList(PAGES)).orElse(List.of());
        for (Object pagesObj : pagesObjs) {
            if (pagesObj instanceof Map m) {
                li.add(new BookPage(
                        ((Long) m.get(NUMBER)).intValue(),
                        (String) m.get(IMAGE_PATH),
                        ((String) m.get(TEXT)).trim()));
            }
        }
        return li;
    }
}
