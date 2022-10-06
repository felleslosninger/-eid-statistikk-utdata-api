package no.difi.statistics;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CategoryValuesTest {

    @Test
    public void testKontaktregisterCategoryValuesIsEmpty() throws IOException {
        Path path = Paths.get("src/test/java/no/difi/statistics/CategoryValuesInput.json");
        String mappings = Files.lines(path).reduce("", String::concat);
    }
}
