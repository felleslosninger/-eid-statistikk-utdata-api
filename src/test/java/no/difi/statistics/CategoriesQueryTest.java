package no.difi.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.difi.statistics.elasticsearch.commands.CategoriesQuery;
import no.difi.statistics.model.OwnerCategories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CategoriesQueryTest {

    @BeforeAll
    static void setup() {}

    @BeforeEach
    void init() {}

    @Test
    public void testAddition() {
        assertEquals(2, 1 + 1);
    }

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
    public void testKeyIsPresent() {
        Path path = Paths.get("src/test/java/no/difi/statistics/CategoriesMappingInput.json");
        String mappings = null;
        try {
            mappings = Files.lines(path).reduce("", String::concat);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(mappings);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CategoriesQuery categoriesQuery = new CategoriesQuery();
        assert jsonNode != null;
        Map<String, OwnerCategories> ownerCategoriesMap = categoriesQuery.traverseJsonNode(jsonNode);

        assertFalse(ownerCategoriesMap.containsKey("991825827:foo-bar-baz:hours"));
        assertFalse(ownerCategoriesMap.containsKey("123456789:idporten-innlogging:hours"));
        assertFalse(ownerCategoriesMap.containsKey("991825827:idporten-innlogging:minutter"));

        assertTrue(ownerCategoriesMap.containsKey("991825827:efmtest:hours"));
        assertTrue(ownerCategoriesMap.containsKey("991825827:kontaktregister:hours"));
        assertTrue(ownerCategoriesMap.containsKey("991825827:idporten-innlogging:hours"));
    }

    @Test
    public void testCorrectLengthInSetIsReturned() {
        Path path = Paths.get("src/test/java/no/difi/statistics/CategoriesMappingInput.json");
        String mappings = null;
        try {
            mappings = Files.lines(path).reduce("", String::concat);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(mappings);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CategoriesQuery categoriesQuery = new CategoriesQuery();
        assert jsonNode != null;
        Map<String, OwnerCategories> ownerCategoriesMap = categoriesQuery.traverseJsonNode(jsonNode);

        OwnerCategories efmtestCategories = ownerCategoriesMap.get("991825827:efmtest:hours");
        assertEquals(5, efmtestCategories.getCategories().size());

        OwnerCategories krrCategories = ownerCategoriesMap.get("991825827:kontaktregister:hours");
        assertEquals(0, krrCategories.getCategories().size());

        OwnerCategories idportenCategories = ownerCategoriesMap.get("991825827:idporten-innlogging:hours");
        assertEquals(9, idportenCategories.getCategories().size());
    }

    @Test
    public void testValidSetIsReturned() {
        Path path = Paths.get("src/test/java/no/difi/statistics/CategoriesMappingInput.json");
        String mappings = null;
        try {
            mappings = Files.lines(path).reduce("", String::concat);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = mapper.readTree(mappings);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CategoriesQuery categoriesQuery = new CategoriesQuery();
        assert jsonNode != null;
        Map<String, OwnerCategories> ownerCategoriesMap = categoriesQuery.traverseJsonNode(jsonNode);

        OwnerCategories efmtestCategories = ownerCategoriesMap.get("991825827:efmtest:hours");
        OwnerCategories krrCategories = ownerCategoriesMap.get("991825827:kontaktregister:hours");
        OwnerCategories idportenCategories = ownerCategoriesMap.get("991825827:idporten-innlogging:hours");

        Set<String> testSet;

        testSet = Set.of("a", "b", "c");
        assertNotEquals(efmtestCategories.getCategories(), testSet);

        testSet = Set.of("service_identifier", "document_identifier", "process_identifier", "orgnr", "status");
        assertEquals(efmtestCategories.getCategories(), testSet);

        testSet = Set.of("a", "b", "c");
        assertNotEquals(krrCategories.getCategories(), testSet);

        testSet = Set.of();
        assertEquals(krrCategories.getCategories(), testSet);

        testSet = Set.of("d", "e", "f");
        assertNotEquals(idportenCategories.getCategories(), testSet);

        testSet = Set.of("TE-orgnum",
                "TL-orgnum",
                "TE",
                "TL-entityId",
                "TE-entityId",
                "TL",
                "Kategori 2/3",
                "Kategori 3/3",
                "Kategori 1/3");
        assertEquals(idportenCategories.getCategories(), testSet);
    }
}
