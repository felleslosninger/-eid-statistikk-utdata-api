package no.difi.statistics.elasticsearch.commands;

import no.difi.statistics.model.CategoryValues;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CategoryValuesQuery {

    private RestHighLevelClient elasticSearchClient;

    // Search for year in index-name (to remove it).
    private static final String yearRegex = "\\d{4}$";
    private static final Pattern yearPattern = Pattern.compile(yearRegex);

    // Interested in lines like "category.TE-orgnum" : "983971636".
    private static final String splitValue = "category\\.";

    // Example of an index-name: 991825827@idporten-innlogging@hour2022
    private static final String searchTerm = "*@*@*";

    private static final Logger logger = LoggerFactory.getLogger(CategoryValuesQuery.class);

    private CategoryValuesQuery() { }

    public Set<CategoryValues> execute() throws IOException {
        Map<CategoryValues, Set<String>> categoryValuesMap = new HashMap<>();


        /*
        Put into a HashMap and use index as key, categories into a HashSet as value.
        When done merge key and value into a HashSet, so we can display in a proper json-format.

        From

        [{
            "_index": "991825827@idporten-innlogging@hour2022",
            "_source":
            {
                "category.TE": "Akershus universitetssykehus hf",
                "category.TL-entityId": "idfed.ad.ahus.no",
                "category.TE-orgnum": "983971636",
                "category.TL-orgnum": "983971636",
                "category.TE-entityId": "idfed.ad.ahus.no",
                "category.TL": "Akershus universitetssykehus hf"
            }
        },
        {
            "_index": "991825827@idporten-innlogging@hour2022",
            "_source":
            {
                "category.TE": "Altinn",
                "category.TL-entityId": "sp.altinn.no",
                "category.TE-orgnum": "991825827",
                "category.TL-orgnum": "991825827",
                "category.TE-entityId": "sp.altinn.no",
                "category.TL": "Altinn"
            }
        }]


        to

        [{
            "owner": "991825827",
            "name": "idporten-innlogging",
            "distance": "hour2022",
            "categories": [
                {
                    "category.TE": "Akershus universitetssykehus hf",
                    "category.TL-entityId": "idfed.ad.ahus.no",
                    "category.TE-orgnum": "983971636",
                    "category.TL-orgnum": "983971636",
                    "category.TE-entityId": "idfed.ad.ahus.no",
                    "category.TL": "Akershus universitetssykehus hf"
                },
                {
                    "category.TE": "Altinn",
                    "category.TL-entityId": "sp.altinn.no",
                    "category.TE-orgnum": "991825827",
                    "category.TL-orgnum": "991825827",
                    "category.TE-entityId": "sp.altinn.no",
                    "category.TL": "Altinn"
                }
            ]
        }]

         */

        // Get a cursor to scroll through the results, set a size for each batch. And a timeout of 1 minute.
        // https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-search-scroll.html
        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest(searchTerm);
        searchRequest.scroll(scroll);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.aggregation(AggregationBuilders.terms().field())
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(10000);
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();

        logger.info("total hits: {}", totalHits);

        SearchHit[] searchHits = hits.getHits();
        while (searchHits != null && searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                Set<String> categories = new HashSet<>();
                // 991825827@idporten-innlogging@hour2022
                String[] index = hit.getIndex().split("@", 3);
                if (index.length == 3) {
                    Matcher matcher = yearPattern.matcher(index[2]);
                    String distance = "hour";
                    if (matcher.find()) {
                        distance = index[2].substring(0, matcher.start());
                    }

                    if ("hour".equals(distance)) {
                        distance = "hours";
                    } else {
                        distance = "minutes";
                    }

                    CategoryValues indexName = new CategoryValues(index[0], index[1], distance);
                    if (categoryValuesMap.containsKey(indexName)) {
                        categories = categoryValuesMap.get(indexName);
                    } else {
                        // This else is needed if there is a timeseries without categories. We want to display them as well.
                        categoryValuesMap.put(indexName, categories);
                    }

                    Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                    for (Object key : sourceAsMap.keySet()) {
                        if (key.toString().startsWith("category.")) {
                            String[] category = key.toString().split(splitValue);
                            categories.add(category[1]);
                            categoryValuesMap.put(indexName, categories);
                        }
                    }
                }
            }

            SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
            scrollRequest.scroll(scroll);
            searchResponse = elasticSearchClient.scroll(scrollRequest, RequestOptions.DEFAULT);
            scrollId = searchResponse.getScrollId();
            searchHits = searchResponse.getHits().getHits();
        }

        ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
        clearScrollRequest.addScrollId(scrollId);
        ClearScrollResponse clearScrollResponse = elasticSearchClient.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
        boolean succeeded = clearScrollResponse.isSucceeded();
        logger.info("succeeded: {}", succeeded);

        return categoryValuesMap.entrySet().stream()
                .map(entry -> {
                    CategoryValues indexName = new CategoryValues(entry.getKey().getOwner(), entry.getKey().getName(), entry.getKey().getDistance());
                    indexName.setCategories(entry.getValue());
                    return indexName;
                })
                .collect(Collectors.toSet());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final CategoryValuesQuery instance = new CategoryValuesQuery();

        public Builder elasticsearchClient(RestHighLevelClient client) {
            instance.elasticSearchClient = client;
            return this;
        }

        public CategoryValuesQuery build() {
            return instance;
        }

    }
}
