package no.difi.statistics.api;

import no.difi.statistics.QueryService;
import no.difi.statistics.UtdataAPI;
import no.difi.statistics.config.BackendConfig;
import no.difi.statistics.model.*;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.util.Collections.singletonList;
import static no.difi.statistics.model.MeasurementDistance.minutes;
import static no.difi.statistics.model.QueryFilter.queryFilter;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests the REST API in isolation with Spring Mock MVC as driver and a Mockito-stubbed service.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"management.endpoints.enabled-by-default = false", "spring.autoconfigure.exclude = org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration"})
@ContextConfiguration(classes = {QueryRestController.class, UtdataAPI.class})
@AutoConfigureMockMvc
public class QueryRestControllerTest {

    @Autowired
    private BackendConfig backendConfig;
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryService queryServiceMock;

    @Test
    public void whenRequestingLastPointThenServiceReceivesCorrespondingRequest() throws Exception {
        final String timeSeries = "a_series";
        final String from = "2013-10-12T12:13:13.123+02:00";
        final String to = "2013-10-12T13:13:13.123+02:00";
        ResultActions result = mockMvc.perform(
                get("/{owner}/{seriesName}/minutes/last", anOwner(), timeSeries)
                        .param("from", from)
                        .param("to", to)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        assertNormalResponse(result);
        verify(backendConfig.queryService()).last(
                TimeSeriesDefinition.builder().name(timeSeries).distance(minutes).owner(anOwner()),
                queryFilter().range(parseTimestamp(from), parseTimestamp(to)).build()
        );
    }

    @Test
    public void correctTimeFormat() throws Exception {
        final String timeSeries = "a_series";
        final String from = "2013-10-12T12:13:13.123+02:00";
        final String to = "2013-10-12T13:13:13.123+02:00";
        final ZonedDateTime expectedDate = ZonedDateTime.of(2022, 8, 15, 12,
                0, 0, 0, ZoneOffset.ofHours(2));
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

        when(queryServiceMock.last(any(), any())).thenReturn(
                TimeSeriesPoint.builder()
                        .timestamp(expectedDate)
                        .measurement("x", 2)
                        .category("x", "y")
                        .build()
        );

        ResultActions result = mockMvc.perform(
                get("/{owner}/{seriesName}/minutes/last", anOwner(), timeSeries)
                        .param("from", from)
                        .param("to", to)
                        .contentType(MediaType.APPLICATION_JSON)
        );
        assertNormalResponse(result);
        String stringResult = result.andReturn().getResponse().getContentAsString();
        String timestampResult = new JSONObject(stringResult).getString("timestamp");
        ZonedDateTime responseDate = ZonedDateTime.parse(timestampResult, formatter);
        assertEquals(expectedDate, responseDate);
    }

    @Test
    public void whenSendingRequestWithFilterThenServiceReceivesCorrespondingRequest() throws Exception {
        final String timeSeries = "a_series";
        final String from = "2013-10-12T12:13:13.123+02:00";
        final String to = "2013-10-12T13:13:13.123+02:00";
        final int percentile = 3;
        final String measurementId = "anId";
        final RelationalOperator operator = RelationalOperator.gt;
        PercentileFilter filter = new PercentileFilter(percentile, measurementId, operator);
        ResultActions result;
        result = mockMvc.perform(
                get("/{owner}/{series}/minutes/percentile", anOwner(), timeSeries)
                        .param("from", from)
                        .param("to", to)
                        .param("percentile", String.valueOf(percentile))
                        .param("measurementId", measurementId)
                        .param("operator", operator.toString())
                        .contentType(MediaType.APPLICATION_JSON)
        );
        assertNormalResponse(result);
        verify(backendConfig.queryService()).query(
                TimeSeriesDefinition.builder().name(timeSeries).minutes().owner(anOwner()),
                queryFilter().range(parseTimestamp(from), parseTimestamp(to)).build(),
                filter
        );
    }

    @Test
    public void whenSendingRequestWithoutFromAndToThenExpectNormalResponseAndNoRangeInServiceCall() throws Exception {
        final String timeSeries = "test";
        ResultActions result = mockMvc.perform(get("/{owner}/{series}/minutes", anOwner(), timeSeries));
        assertNormalResponse(result);
        verify(backendConfig.queryService()).query(
                TimeSeriesDefinition.builder().name(timeSeries).minutes().owner(anOwner()),
                queryFilter().build()
        );
    }

    @Test
    public void whenSendingRequestWithoutFromThenExpectNormalResponseAndLeftOpenRangeInServiceCall() throws Exception {
        final String endTime = "2013-10-12T13:13:13.123+02:00";
        final String timeSeries = "test";
        ResultActions result = mockMvc.perform(get("/{owner}/{series}/minutes", anOwner(), timeSeries).param("to", endTime));
        assertNormalResponse(result);
        verify(backendConfig.queryService()).query(
                TimeSeriesDefinition.builder().name(timeSeries).minutes().owner(anOwner()),
                queryFilter().range(null, parseTimestamp(endTime)).build()
        );
    }

    @Test
    public void whenSendingRequestWithoutToThenExpectNormalResponseAndRightOpenRangeInServiceCall() throws Exception {
        final String startTime = "2013-10-12T13:13:13.123+02:00";
        final String timeSeries = "test";
        ResultActions result = mockMvc.perform(get("/{owner}/{series}/minutes", anOwner(), timeSeries).param("from", startTime));
        assertNormalResponse(result);
        verify(backendConfig.queryService()).query(
                TimeSeriesDefinition.builder().name(timeSeries).minutes().owner(anOwner()),
                queryFilter().range(parseTimestamp(startTime), null).build()
        );
    }

    @Test
    public void whenSendingRequestWithoutCategoryThenResponseContainsNoCategoryField() throws Exception {
        when(backendConfig.queryService().query(any(TimeSeriesDefinition.class), any(QueryFilter.class))).thenReturn(
                singletonList(aPointWithoutCategory())
        );
        mockMvc.perform(get("/{owner}/{series}/minutes", anOwner(), aSeries()))
                .andExpect(jsonPath("$[*].timestamp").exists())
                .andExpect(jsonPath("$[*].categories").doesNotExist());
    }

    private TimeSeriesPoint aPointWithoutCategory() {
        return TimeSeriesPoint.builder().timestamp(aTimestamp()).measurement(aMeasurementId(), aMeasurementValue()).build();
    }

    private String aMeasurementId() {
        return "m1";
    }

    private long aMeasurementValue() {
        return 123L;
    }

    private ZonedDateTime aTimestamp() {
        return ZonedDateTime.now();
    }

    private String anOwner() {
        return "anOwner";
    }

    private String aSeries() {
        return "aSeries";
    }

    private ZonedDateTime parseTimestamp(String timestamp) {
        return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private void assertNormalResponse(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
    }

}
