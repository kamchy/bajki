package com.kamilachyla.bajki;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MyTomlParserTest {

    public static Stream<Arguments> fileAndMetaProvider() {
        return Stream.of(
                Arguments.arguments(
                        Path.of("src/test/resources/test1.toml"),
                        new Book("House", "Little Mom", "house.jpg", "Simple Living", null)),
                Arguments.arguments(
                        Path.of("src/test/resources/test2.toml"),
                        new Book("Sunny day", "Little Mommy", "sun.jpg", "Nice!", null))
        );
    }
    @ParameterizedTest()
    @MethodSource("fileAndMetaProvider")
    void testTomlParserGetsCorrectMetadata(Path file, Book expected) {
        try(var fileReader = new FileReader(file.toFile())) {
            Optional<Book> actual = new MyTomlParser().parse(fileReader);

            Book actualWithoutPages = actual.orElseThrow().withEmptyPages();
            assertEquals(expected, actualWithoutPages);
        } catch (IOException e) {
            fail("File %s not found".formatted(file));
        }

    }

    @ParameterizedTest
    @CsvSource("""
            src/test/resources/test1.toml, 3
            src/test/resources/test2.toml, 0""")
    void testPagesListSize(String file, String count) {
        try(var fileReader = new FileReader(file)) {
            Optional<Book> actual = new MyTomlParser().parse(fileReader);
            var expected = Integer.parseInt(count);
            assertEquals(expected, actual.orElseThrow().pages().size());
        } catch (IOException e) {
            fail("File %s not found".formatted(file));
        }
    }

    static class PageTest {
        private List<BookPage> pages;
        private final Map<Integer, BookPage> expected = Map.of(
                1, new BookPage(1, "house1.jpg", "line1\nline2\nline3"),
                2, new BookPage(2, "house2.jpg", "foo1\nfoo2"),
                3, new BookPage(3, "house3.jpg", "test")
        );

        @BeforeEach
        void parse() {
            try {
                FileReader fileReader = new FileReader("src/test/resources/test1.toml");
                Optional<Book> actual = new MyTomlParser().parse(fileReader);
                pages = actual.orElseThrow().pages();
                System.out.printf("Before each: pages=%s", pages);
            } catch (FileNotFoundException e) {
                fail();
            }
        }

       @Test
        void testPageContents() {
           pages.forEach(actualPage -> {
               var expectedPage = expected.get(actualPage.number());
               assertEquals(expectedPage, actualPage);
           });
        }
    }
}