package com.kamilachyla.bajki;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MainTest {


    @Test
    void throwsWhenPathIsNull() {
        assertThrows(NullPointerException.class, () -> new Main(null).run());
    }

    @Test
    void testTomlAndPdfInSameDir(){
        var testTomlDirPath = Path.of("src/test/resources");
        Optional<Path> optionalPath = new Main(testTomlDirPath).run();
        assertEquals (Optional.empty(), optionalPath);
    }

    @Test
    void generatesPdf() {
        var path = Path.of("src/test/resources/test1.toml");
        new Main(path).run();
    }

    @Test
    void testGetTomlFileDefaultName() {
        var actual = Main.getTomlFileName(Path.of("/one/two/three"), "default.toml", (Path p) -> true);
        var expected = Path.of("/one/two/three/default.toml");
        assertPathsEqual(expected, actual);
    }
    @Test
    void testGetTomlFileRelativeDefaultName() {
        var workingDir = Path.of(".");
        final var relPath = "one/two/three";
        final var defaultFileName = "default.toml";
        var actual = Main.getTomlFileName(Path.of(relPath), defaultFileName, (Path p) -> true);
        var expected = Path.of(workingDir.toString(), relPath, defaultFileName);
        assertPathsEqual(expected, actual);
    }

    private void assertPathsEqual(Path expected, Path actual) {
        assertEquals(expected.toAbsolutePath().normalize().toString(), actual.toAbsolutePath().normalize().toString());
    }


    @Test
    void testGetTomlFileActualFile() {
        var actual = Main.getTomlFileName(Path.of("/one/two/three"), "default.toml", (Path p) -> false);
        var expected = Path.of("/one/two/three");
        assertPathsEqual(expected, actual);
    }
    @Test
    void testGetTomlFileRelativeActualFile() {
        var workingDir = Path.of(".");
        final var relPath = "one/two/three/description.toml";
        final var defaultFileName = "default.toml";
        var actual = Main.getTomlFileName(Path.of(relPath), defaultFileName, (Path p) -> false);
        var expected = Path.of(workingDir.toString(), relPath).toAbsolutePath().normalize();
        assertPathsEqual(expected, actual);
    }
}