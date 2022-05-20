package no.difi.statistics.api;

import no.difi.statistics.QueryService;
import no.difi.statistics.config.BackendConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile({"unittest"})
public class MockBackendConfig implements BackendConfig {

    @Bean
    public QueryService queryService() {
        return mock(QueryService.class);
    }

}
