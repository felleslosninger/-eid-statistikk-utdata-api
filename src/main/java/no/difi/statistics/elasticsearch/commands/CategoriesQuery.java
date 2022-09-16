package no.difi.statistics.elasticsearch.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import no.difi.statistics.model.IndexName;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CategoriesQuery {

    private RestClient elasticSearchClient;

    private static final Logger logger = LoggerFactory.getLogger(CategoriesQuery.class);

    private CategoriesQuery() { }

    public Set<IndexName> execute() throws IOException {
        // Put index-name and categories in Map, and copy to a Set before returning.
        Map<IndexName, Set<String>> indexNameMap = new HashMap<>();

        // Search for year in index-name (to remove it).
        String pattern = "\\d{4}$";
        Pattern r = Pattern.compile(pattern);

        Request request = new Request("GET", "/*@*@*/_mapping");
        String mappings = EntityUtils.toString(elasticSearchClient.performRequest(request).getEntity());
        //logger.info("mapper:\n{}", mappings);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mappings);
        Iterator<String> index = jsonNode.fieldNames();
        for (JsonNode indexObject : jsonNode) {
            Set<String> categorySet = new HashSet<>();

            // Example of an index-name: 991825827@idporten-innlogging@hour2022
            String[] name = index.next().split("@", 3);
            Matcher matcher = r.matcher(name[2]);

            String distance = "hour";
            if (matcher.find()) {
                distance = name[2].substring(0, matcher.start());
            }

            if ("hour".equals(distance)) {
                distance = "hours";
            } else {
                distance = "minutes";
            }

            IndexName indexName = new IndexName(name[0], name[1], distance);

            if (indexNameMap.containsKey(indexName)) {
                categorySet = indexNameMap.get(indexName);
            } else {
                indexNameMap.put(indexName, categorySet);
            }

            // We want categories.
            JsonNode properties = indexObject.get("mappings").get("properties").get("category");
            //logger.info("parent: {}, indexObject: {}", name, properties);
            if (properties != null) {
                for (JsonNode categories : properties) {
                    Iterator<String> category = categories.fieldNames();
                    while (category.hasNext()) {
                        String c = category.next();
                        categorySet.add(c);
                        //logger.info("index-name: {}, category: {}", name, c);
                    }
                }
            }
            indexNameMap.put(indexName, categorySet);
        }

        return indexNameMap.entrySet().stream()
                .map(entry -> {
                        IndexName indexName = new IndexName(entry.getKey().getOwner(), entry.getKey().getName(), entry.getKey().getDistance());
                        indexName.setCategories(entry.getValue());
                        return indexName;
                })
                .collect(Collectors.toSet());

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
