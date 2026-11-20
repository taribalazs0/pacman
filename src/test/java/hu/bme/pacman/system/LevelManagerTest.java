package hu.bme.pacman.system;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LevelManagerTest {

    /** JUnit creates a fresh, empty folder for every test and deletes it afterwards. */
    @TempDir
    Path tempDir;

    private Path missingFile;
    private Path badSymbolFile;

    @BeforeEach
    void setUp() throws IOException {
        missingFile = tempDir.resolve("missing.txt");
        badSymbolFile = tempDir.resolve("bad.txt");
        // 'X' is not a map symbol
        Files.writeString(badSymbolFile, "#####\n#P.X#\n#####\n");
    }

    @Test
    void loadingAMissingMapFileThrowsIOException() {
        // Act + Assert: the lambda is only run inside assertThrows
        assertThrows(IOException.class, () -> LevelManager.loadFromCSV(missingFile.toString()));
    }

    @Test
    void anUnknownSymbolInTheMapIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> LevelManager.loadFromCSV(badSymbolFile.toString()));
    }
}
