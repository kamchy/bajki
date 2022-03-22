package com.kamilachyla.bajki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
/**
 * Main class for generating .pdf books
 * */
public class Main {

    public static final String NOTHING_CHECK_LOGS_FOR_DETAILS = "nothing. Check logs for details.";
    private final Path tomlPath;
    /** Default name of generated output file*/
    public static final String PDF_OUTPUT_NAME = "output.pdf";
    /** Default name of .toml file which should contain book data*/
    public static final String DEFAULT_TOML_FILE_NAME = "description.toml";
    public static final Logger logger = LoggerFactory.getLogger("Main");

    public Main(Path tomlFilePath) {
        this.tomlPath = Objects.requireNonNull(tomlFilePath);
    }

    /**
     * Runs pdf generation.
     *
     * @return Optional<Path> path where the .pdf is stored or {@link Optional#empty()} if there was an error
     * */
    public Optional<Path> run() {
        final var tomlFilePath = getTomlFileName(tomlPath, DEFAULT_TOML_FILE_NAME, p -> p.toFile().isDirectory());
        final var tomlFileName = tomlFilePath.toAbsolutePath().normalize().toString();
        logger.info("resolved file name: {}", tomlFileName);
        try (var fileReader = new FileReader(tomlFileName)) {
            Optional<Book> meta = new MyTomlParser().parse(fileReader);
            return new Generator(tomlFilePath::resolveSibling).generate(meta.orElseThrow(), () -> PDF_OUTPUT_NAME);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return Optional.empty();
        }
    }

    public static Path getTomlFileName(Path path, String defaultFileName, Predicate<Path> shouldUseDefaultFileName) {
        return shouldUseDefaultFileName.test(path) ? path.resolve(defaultFileName) : path;
    }

    public static void main(String[] args) {
        for (String arg: args) {
            var path = Path.of(arg);
            logger.info("Reading toml from {} ...",path.toAbsolutePath());
            var res = new Main(path).run();
            logger.info("generated {}", res.map(p ->
                    p.toAbsolutePath().toString()).orElse(NOTHING_CHECK_LOGS_FOR_DETAILS));
        }
    }
}
