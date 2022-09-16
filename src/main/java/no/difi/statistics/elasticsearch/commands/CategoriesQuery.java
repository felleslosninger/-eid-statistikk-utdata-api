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
import java.util.stream.Collectors;

public class CategoriesQuery {

    private RestClient elasticSearchClient;

    private static final Logger logger = LoggerFactory.getLogger(CategoriesQuery.class);

    private CategoriesQuery() { }

    public Set<OwnerCategories> execute() throws IOException {
        // Put index-name and categories in Map, and copy to a Set before returning.
        Map<String, OwnerCategories> ownerCategoriesMap = new HashMap<>();

        Request request = new Request("GET", "/*@*@*/_mapping");
        String mappings = EntityUtils.toString(elasticSearchClient.performRequest(request).getEntity());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(mappings);
        Iterator<String> index = jsonNode.fieldNames();
        for (JsonNode mappingsOfIndex : jsonNode) {
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

            OwnerCategories ownerCategories = new OwnerCategories(owner, name, distance);
            Set<String> uniqueCategories = getUniqueCategories(mappingsOfIndex);
            ownerCategories.getCategories().addAll(uniqueCategories);

            if (ownerCategoriesMap.containsKey(key)) {
                ownerCategoriesMap.get(key).getCategories().addAll(ownerCategories.getCategories());
            } else {
                ownerCategoriesMap.put(key, ownerCategories);
            }
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
