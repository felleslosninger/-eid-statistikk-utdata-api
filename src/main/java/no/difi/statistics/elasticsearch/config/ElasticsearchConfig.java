package no.difi.statistics.elasticsearch.config;

import no.difi.statistics.QueryService;
import no.difi.statistics.config.BackendConfig;
import no.difi.statistics.elasticsearch.Client;
import no.difi.statistics.elasticsearch.CommandFactory;
import no.difi.statistics.elasticsearch.ElasticsearchQueryService;
import no.difi.statistics.elasticsearch.commands.*;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;

@Configuration
@Profile({"!unittest"})
public class ElasticsearchConfig implements BackendConfig {

    private final String elasticSearchHost;
    private final int elasticSearchPort;
    private final String elasticSearchApiKey;

    @Autowired
    public ElasticsearchConfig(
            @Value("${no.difi.statistics.elasticsearch.host}") String elasticSearchHost,
            @Value("${no.difi.statistics.elasticsearch.port}") int elasticSearchPort,
            @Value("${no.difi.statistics.elasticsearch.apikey}") String elasticSearchApiKey) {
        this.elasticSearchHost = elasticSearchHost;
        this.elasticSearchPort = elasticSearchPort;
        this.elasticSearchApiKey = elasticSearchApiKey;
    }

    @Override
    @Bean
    public QueryService queryService() {
        return new ElasticsearchQueryService(commandFactory());
    }

    @Bean
    public CommandFactory commandFactory() {
        return new CommandFactory();
    }

    @Bean
    @Scope("prototype")
    public TimeSeriesQuery.Builder queryCommandBuilder() {
        return TimeSeriesQuery.builder().elasticsearchClient(elasticsearchHighLevelClient()).sumHistogramCommand(sumHistogramCommandBuilder());
    }

    @Bean
    @Scope("prototype")
    public AvailableSeriesQuery.Builder listAvailableTimeSeriesCommandBuilder() {
        return AvailableSeriesQuery.builder().elasticsearchClient(elasticsearchLowLevelClient().build());
    }

    @Bean
    @Scope("prototype")
    public CategoriesQuery.Builder listCategoriesCommandBuilder() {
        return CategoriesQuery.builder().elasticsearchClient(elasticsearchLowLevelClient().build());
    }

    @Bean
    @Scope("prototype")
    public CategoryValuesQuery.Builder listCategoryValuesCommandBuilder() {
        return CategoryValuesQuery.builder().elasticsearchClient(elasticsearchHighLevelClient());
    }

    @Bean
    @Scope("prototype")
    public LastHistogramQuery.Builder lastHistogramCommandBuilder() {
        return LastHistogramQuery.builder().elasticsearchClient(elasticsearchHighLevelClient());
    }

    @Bean
    @Scope("prototype")
    public LastQuery.Builder lastCommandBuilder() {
        return LastQuery.builder().elasticsearchClient(elasticsearchHighLevelClient());
    }

    @Bean
    @Scope("prototype")
    public SumHistogramQuery.Builder sumHistogramCommandBuilder() {
        return SumHistogramQuery.builder().elasticsearchClient(elasticsearchHighLevelClient());
    }

    @Bean
    @Scope("prototype")
    public SumQuery.Builder sumCommandBuilder() {
        return SumQuery.builder().elasticsearchClient(elasticsearchHighLevelClient());
    }

    @Bean
    @Scope("prototype")
    public PercentileQuery.Builder percentileCommandBuilder() {
        return PercentileQuery.builder().elasticsearchClient(elasticsearchHighLevelClient());
    }

    @Bean
    @Scope("prototype")
    public GetMeasurementIdentifiers.Builder measurementIdentifiersCommandBuilder() {
        return GetMeasurementIdentifiers.builder().elasticsearchClient(elasticsearchLowLevelClient().build());
    }

    @Bean
    public Client elasticsearchClient() {
        return new Client(
                elasticsearchHighLevelClient(),
                getScheme()+"://" + elasticSearchHost + ":" + elasticSearchPort
        );
    }

    @Bean(destroyMethod = "close")
    public RestHighLevelClient elasticsearchHighLevelClient() {
        return new RestHighLevelClient(elasticsearchLowLevelClient());
    }

    private RestClientBuilder elasticsearchLowLevelClient() {
        RestClientBuilder builder = RestClient.builder(new HttpHost(elasticSearchHost, elasticSearchPort, getScheme()));
        Header[] headers = new Header[]{new BasicHeader("Authorization","ApiKey " + elasticSearchApiKey)};
        builder.setDefaultHeaders(headers);
        return  builder;
    }

    private String getScheme() {
        String scheme = "http";
        // TODO put this into config?
        if (elasticSearchHost != null && elasticSearchHost.endsWith("elastic-cloud.com")) {
            scheme = "https";
        }
        return scheme;
    }
}
