package no.difi.statistics.elasticsearch.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.difi.statistics.model.OwnerCategories;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CategoriesQuery {

    private RestClient elasticSearchClient;

    // Search for year in index-name (to remove it).
    private static final String yearRegex = "\\d{4}$";
    private static final Pattern yearPattern = Pattern.compile(yearRegex);

    private static final ObjectMapper mapper = new ObjectMapper();

    public CategoriesQuery() {
    }

    public Set<OwnerCategories> execute() throws IOException {
        // Put index-name and categories in Map, and copy to a Set before returning.
        Map<String, OwnerCategories> ownerCategoriesMap = new HashMap<>();

        Request request = new Request("GET", "/*@*@*/_mapping");
        String mappings = EntityUtils.toString(elasticSearchClient.performRequest(request).getEntity());

        JsonNode jsonNode = mapper.readTree(mappings);

        return new HashSet<>(traverseJsonNode(jsonNode).values());
    }

    public Map<String, OwnerCategories> traverseJsonNode(JsonNode jsonNode) {
        // Put index-name and categories in Map, and copy to a Set before returning.
        Map<String, OwnerCategories> ownerCategoriesMap = new HashMap<>();
        Iterator<String> index = jsonNode.fieldNames();

        for (JsonNode mappingsOfIndex : jsonNode) {
            // Example of an index-name: 991825827@idporten-innlogging@hour2022
            String[] indexNameTokens = index.next().split("@", 3);
            String owner = indexNameTokens[0];
            String name = indexNameTokens[1];
            String distance = determineDistance(indexNameTokens[2]);
            String key = owner + ":" +  name + ":" + distance;

            OwnerCategories ownerCategories = new OwnerCategories(owner, name, distance);
            Set<String> uniqueCategories = getUniqueCategories(mappingsOfIndex);
            ownerCategories.getCategories().addAll(uniqueCategories);

            if (ownerCategoriesMap.containsKey(key)) {
                ownerCategoriesMap.get(key).getCategories().addAll(ownerCategories.getCategories());
            } else {
                ownerCategoriesMap.put(key, ownerCategories);
            }
        }

        return ownerCategoriesMap;
    }

    public String determineDistance(String distanceToken) {
        Matcher matcher = yearPattern.matcher(distanceToken);
        String distance = "hour";
        if (matcher.find()) {
            distance = distanceToken.substring(0, matcher.start());
        }

        if ("hour".equals(distance)) {
            distance = "hours";
        } else {
            distance = "minutes";
        }

        return distance;
    }

    private Set<String> getUniqueCategories(JsonNode mappingsOfIndex) {
        Set<String> uniqueCategories = new HashSet<>();
        JsonNode properties = mappingsOfIndex.get("mappings").get("properties").get("category");
        if (properties != null) {
            for (JsonNode categories : properties) {
                Iterator<String> category = categories.fieldNames();
                while (category.hasNext()) {
                    String categoryString = category.next();
                    uniqueCategories.add(categoryString);
                }
            }
        }

        return uniqueCategories;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final CategoriesQuery instance = new CategoriesQuery();

        public Builder elasticsearchClient(RestClient client) {
            instance.elasticSearchClient = client;
            return this;
        }

        public CategoriesQuery build() {
            return instance;
        }

    }
}
