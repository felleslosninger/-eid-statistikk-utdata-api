package no.difi.statistics.elasticsearch.commands;

import no.difi.statistics.model.IndexName;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class CategoriesQuery {

    private RestHighLevelClient elasticSearchClient;

    private static final Logger logger = LoggerFactory.getLogger(CategoriesQuery.class);

    private CategoriesQuery() { }

    // Get a cursor to scroll through the results, set a size for each batch. And a timeout of 1 minute.
    public HashMap<IndexName, HashSet<String>> execute() throws IOException {
        HashSet<String> categories = new HashSet<>();
        HashMap<IndexName, HashSet<String>> indexNames = new HashMap<>();

        String splitValue = "category\\.";
        // Year 2022 is currently hardcoded.
        String searchTerm = "*@*@hour*2022";

        SearchRequest searchRequest = new SearchRequest(searchTerm);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(10000);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(TimeValue.timeValueMinutes(1L));
        SearchResponse searchResponse = elasticSearchClient.search(searchRequest, RequestOptions.DEFAULT);
        String scrollId = searchResponse.getScrollId();
        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();

        logger.info("scroll-id: {}", scrollId);
        logger.info("total hits: {}", totalHits);

        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            String[] index = hit.getIndex().split("@", 3);
            if (index.length == 3) {
                logger.info("index: {}", hit.getIndex());
                IndexName indexName = new IndexName(index[0],index[1],index[2]);
                if (indexNames.containsKey(indexName)) {
                    categories = indexNames.get(indexName);
                } else {
                    indexNames.put(indexName, new HashSet<String>());
                }

                //logger.info("hit: {}", hit);
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                for (Object key : sourceAsMap.keySet()) {
                    if (key.toString().startsWith("category.")) {
                        String category[] = key.toString().split(splitValue);
                        logger.info("key: {}, split: {}", key, category[1]);
                        categories.add(category[1]);
                    }
                }
            }
        }

        return indexNames;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CategoriesQuery instance = new CategoriesQuery();

        public Builder elasticsearchClient(RestHighLevelClient client) {
            instance.elasticSearchClient = client;
            return this;
        }

        public CategoriesQuery build() {
            return instance;
        }

    }
}
