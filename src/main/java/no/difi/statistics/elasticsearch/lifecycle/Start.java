package no.difi.statistics.elasticsearch.lifecycle;

import no.difi.statistics.config.AppConfig;
import no.difi.statistics.elasticsearch.config.ElasticsearchConfig;
import org.springframework.boot.SpringApplication;

public class Start {

    public static void main(String...args) {
        SpringApplication.run(new Class[]{AppConfig.class, ElasticsearchConfig.class}, args);
    }

}
