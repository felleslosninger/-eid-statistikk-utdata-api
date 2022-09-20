package no.difi.statistics;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.difi.statistics.elasticsearch.commands.CategoriesQuery;
import no.difi.statistics.model.OwnerCategories;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriesQueryTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testMinuteDistance() {
        CategoriesQuery categoriesQuery = new CategoriesQuery();
        String distanceToken = "minute2022";
        String distance = categoriesQuery.determineDistance(distanceToken);
        assertEquals("minutes", distance);
    }

    @Test
    public void testHourDistance() {
        CategoriesQuery categoriesQuery = new CategoriesQuery();
        String distanceToken = "hour2022";
        String distance = categoriesQuery.determineDistance(distanceToken);
        assertEquals("hours", distance);
    }

    @Test
    public void testKeyIsPresent() throws IOException {
        Path path = Paths.get("src/test/java/no/difi/statistics/CategoriesMappingInput.json");
        String mappings = Files.lines(path).reduce("", String::concat);

        JsonNode jsonNode = mapper.readTree(mappings);

        CategoriesQuery categoriesQuery = new CategoriesQuery();
        Map<String, OwnerCategories> ownerCategoriesMap = categoriesQuery.traverseJsonNode(jsonNode);

        assertFalse(ownerCategoriesMap.containsKey("991825827:foo-bar-baz:hours"));
        assertFalse(ownerCategoriesMap.containsKey("123456789:idporten-innlogging:hours"));
        assertFalse(ownerCategoriesMap.containsKey("991825827:idporten-innlogging:minutter"));

        assertTrue(ownerCategoriesMap.containsKey("991825827:efmtest:hours"));
        assertTrue(ownerCategoriesMap.containsKey("991825827:kontaktregister:hours"));
        assertTrue(ownerCategoriesMap.containsKey("991825827:idporten-innlogging:hours"));
    }

    @Test
    public void testCorrectLengthInSetIsReturned() throws IOException {
        Path path = Paths.get("src/test/java/no/difi/statistics/CategoriesMappingInput.json");
        String mappings = Files.lines(path).reduce("", String::concat);

        JsonNode jsonNode = mapper.readTree(mappings);

        CategoriesQuery categoriesQuery = new CategoriesQuery();
        Map<String, OwnerCategories> ownerCategoriesMap = categoriesQuery.traverseJsonNode(jsonNode);

        OwnerCategories efmtestCategories = ownerCategoriesMap.get("991825827:efmtest:hours");
        assertEquals(5, efmtestCategories.getCategories().size());

        OwnerCategories krrCategories = ownerCategoriesMap.get("991825827:kontaktregister:hours");
        assertEquals(0, krrCategories.getCategories().size());

        OwnerCategories idportenCategories = ownerCategoriesMap.get("991825827:idporten-innlogging:hours");
        assertEquals(9, idportenCategories.getCategories().size());
    }

    @Test
    public void testValidSetIsReturned() throws IOException {
        Path path = Paths.get("src/test/java/no/difi/statistics/CategoriesMappingInput.json");
        String mappings = Files.lines(path).reduce("", String::concat);

        JsonNode jsonNode = mapper.readTree(mappings);

        CategoriesQuery categoriesQuery = new CategoriesQuery();
        Map<String, OwnerCategories> ownerCategoriesMap = categoriesQuery.traverseJsonNode(jsonNode);

        OwnerCategories efmtestCategories = ownerCategoriesMap.get("991825827:efmtest:hours");
        OwnerCategories krrCategories = ownerCategoriesMap.get("991825827:kontaktregister:hours");
        OwnerCategories idportenCategories = ownerCategoriesMap.get("991825827:idporten-innlogging:hours");

        Set<String> testSet;

        testSet = Set.of("a", "b", "c");
        assertNotEquals(testSet, efmtestCategories.getCategories());

        testSet = Set.of("service_identifier", "document_identifier", "process_identifier", "orgnr", "status");
        assertEquals(testSet, efmtestCategories.getCategories());

        testSet = Set.of("a", "b", "c");
        assertNotEquals(testSet, krrCategories.getCategories());

        testSet = Set.of();
        assertEquals(testSet, krrCategories.getCategories());

        testSet = Set.of("d", "e", "f");
        assertNotEquals(testSet, idportenCategories.getCategories());

        testSet = Set.of("TE-orgnum",
                "TL-orgnum",
                "TE",
                "TL-entityId",
                "TE-entityId",
                "TL",
                "Kategori 2/3",
                "Kategori 3/3",
                "Kategori 1/3");
        assertEquals(testSet, idportenCategories.getCategories());
    }
}
