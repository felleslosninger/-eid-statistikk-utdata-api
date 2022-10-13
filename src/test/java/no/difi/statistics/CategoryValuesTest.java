package no.difi.statistics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.difi.statistics.elasticsearch.commands.CategoryValuesQuery;
import no.difi.statistics.model.CategoryValues;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CategoryValuesTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDistanceIsCorrect() {
        CategoryValuesQuery categoryValuesQuery = new CategoryValuesQuery();

        String indexDistance = "second2022";
        String distance = categoryValuesQuery.getDistance(indexDistance);
        assertEquals("minutes", distance);

        indexDistance = "minute2022";
        distance = categoryValuesQuery.getDistance(indexDistance);
        assertEquals("minutes", distance);

        indexDistance = "hour2022";
        distance = categoryValuesQuery.getDistance(indexDistance);
        assertEquals("hours", distance);
    }

    @Test
    public void testEformidlingCategoryValuesHaveCorrectSize() throws IOException {
        HashMap<Map<String, Object>, CategoryValues> categoriesAndIndexNameMap = new HashMap<>();
        CategoryValuesQuery categoryValuesQuery = new CategoryValuesQuery();
        Set<Map<String, Object>> empty = new HashSet<>();

        Path path = Paths.get("src/test/java/no/difi/statistics/CategoryValuesInputEformidling.json");
        String mappings = Files.lines(path).reduce("", String::concat);

        JsonNode jsonNode = mapper.readTree(mappings);

        for (JsonNode j : jsonNode) {
            String[] index = j.get("_index").toString().replace("\"", "").split("@", 3);
            String distance = categoryValuesQuery.getDistance(index[2]);

            JsonNode sourceNode = mapper.readTree(j.get("_source").toString());
            Map<String, Object> sourceMap = mapper.convertValue(sourceNode, new TypeReference<>(){});
            Map<String, Object> sortedSourceMap = new TreeMap<>(sourceMap);
            CategoryValues indexName = new CategoryValues(index[0], index[1], distance, empty);
            categoriesAndIndexNameMap.put(sortedSourceMap, indexName);
        }

        Map<CategoryValues, Set<Map<String, Object>>> categoryValuesMap = categoryValuesQuery.getIndexNameAsKey(categoriesAndIndexNameMap);
        CategoryValues key = new CategoryValues("991825827", "efmtest", "hours", empty);
        for (Map.Entry<CategoryValues, Set<Map<String, Object>>> entry : categoryValuesMap.entrySet()) {
            assertEquals("991825827", entry.getKey().getOwner());
            assertEquals("efmtest", entry.getKey().getName());
            assertEquals("hours", entry.getKey().getDistance());
            assertEquals(6, entry.getValue().size());
            Iterator<Map<String, Object>> cvIterator = entry.getValue().iterator();
            // Test first json-object.
            Map<String, Object> test = cvIterator.next();
            assertEquals("", test.get("category.document_identifier"));
            assertEquals("964965226", test.get("category.orgnr"));
            assertEquals("urn:no:difi:profile:einnsyn:journalpost:ver1.0", test.get("category.process_identifier"));
            assertEquals("DPE", test.get("category.service_identifier"));
            assertEquals("OPPRETTET", test.get("category.status"));
        }
    }

    @Test
    public void testIdPortenCategoryValuesHaveCorrectSize() throws IOException {
        HashMap<Map<String, Object>, CategoryValues> categoriesAndIndexNameMap = new HashMap<>();
        CategoryValuesQuery categoryValuesQuery = new CategoryValuesQuery();
        Set<Map<String, Object>> empty = new HashSet<>();

        Path path = Paths.get("src/test/java/no/difi/statistics/CategoryValuesInputIdPorten.json");
        String mappings = Files.lines(path).reduce("", String::concat);

        JsonNode jsonNode = mapper.readTree(mappings);

        for (JsonNode j : jsonNode) {
            String[] index = j.get("_index").toString().replace("\"", "").split("@", 3);
            String distance = categoryValuesQuery.getDistance(index[2]);

            JsonNode sourceNode = mapper.readTree(j.get("_source").toString());
            Map<String, Object> sourceMap = mapper.convertValue(sourceNode, new TypeReference<>(){});
            Map<String, Object> sortedSourceMap = new TreeMap<>(sourceMap);
            CategoryValues indexName = new CategoryValues(index[0], index[1], distance, empty);
            categoriesAndIndexNameMap.put(sortedSourceMap, indexName);
        }

        Map<CategoryValues, Set<Map<String, Object>>> categoryValuesMap = categoryValuesQuery.getIndexNameAsKey(categoriesAndIndexNameMap);
        CategoryValues key = new CategoryValues("991825827", "efmtest", "hours", empty);
        for (Map.Entry<CategoryValues, Set<Map<String, Object>>> entry : categoryValuesMap.entrySet()) {
            assertEquals("991825827", entry.getKey().getOwner());
            assertEquals("idporten-innlogging", entry.getKey().getName());
            assertEquals("hours", entry.getKey().getDistance());
            assertEquals(7, entry.getValue().size());
            Iterator<Map<String, Object>> cvIterator = entry.getValue().iterator();
            // Test first json-object.
            Map<String, Object> test = cvIterator.next();
            assertEquals("Alver kommune", test.get("category.TE"));
            assertEquals("vismainnsyn.alver.kommune.no", test.get("category.TE-entityId"));
            assertEquals("920290922", test.get("category.TE-orgnum"));
            assertEquals("Alver kommune", test.get("category.TL"));
            assertEquals("vismainnsyn.alver.kommune.no", test.get("category.TL-entityId"));
            assertEquals("920290922", test.get("category.TL-orgnum"));
        }
    }

    @Test
    public void testKRRCategoryValuesHaveCorrectSize() throws IOException {
        HashMap<Map<String, Object>, CategoryValues> categoriesAndIndexNameMap = new HashMap<>();
        CategoryValuesQuery categoryValuesQuery = new CategoryValuesQuery();
        Set<Map<String, Object>> empty = new HashSet<>();

        Path path = Paths.get("src/test/java/no/difi/statistics/CategoryValuesInputKRR.json");
        String mappings = Files.lines(path).reduce("", String::concat);

        JsonNode jsonNode = mapper.readTree(mappings);

        for (JsonNode j : jsonNode) {
            String[] index = j.get("_index").toString().replace("\"", "").split("@", 3);
            String distance = categoryValuesQuery.getDistance(index[2]);

            JsonNode sourceNode = mapper.readTree(j.get("_source").toString());
            Map<String, Object> sourceMap = mapper.convertValue(sourceNode, new TypeReference<>(){});
            Map<String, Object> sortedSourceMap = new TreeMap<>(sourceMap);
            CategoryValues indexName = new CategoryValues(index[0], index[1], distance, empty);
            categoriesAndIndexNameMap.put(sortedSourceMap, indexName);
        }

        Map<CategoryValues, Set<Map<String, Object>>> categoryValuesMap = categoryValuesQuery.getIndexNameAsKey(categoriesAndIndexNameMap);
        CategoryValues key = new CategoryValues("991825827", "efmtest", "hours", empty);
        for (Map.Entry<CategoryValues, Set<Map<String, Object>>> entry : categoryValuesMap.entrySet()) {
            assertEquals("991825827", entry.getKey().getOwner());
            assertEquals("kontaktregister", entry.getKey().getName());
            assertEquals("hours", entry.getKey().getDistance());
            assertEquals(1, entry.getValue().size());
            Iterator<Map<String, Object>> cvIterator = entry.getValue().iterator();
            // Test first json-object.
            Map<String, Object> test = cvIterator.next();
            // Shall be empty.
            assertEquals("{}", test.toString());
        }
    }
}
