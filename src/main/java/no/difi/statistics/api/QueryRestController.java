package no.difi.statistics.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import no.difi.statistics.QueryService;
import no.difi.statistics.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static no.difi.statistics.model.QueryFilter.queryFilter;

@Tag(name = "Statistics-query", description = "Hent ut data frå statistikk-databasen")
@RestController
public class QueryRestController {

    private final QueryService service;

    public QueryRestController(QueryService service) {
        this.service = service;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler
    @ResponseStatus
    public String handle(Exception e) {
        logger.error("Query failed", e);
        return e.getMessage();
    }

    @Hidden
    @GetMapping("/")
    public RedirectView index() {
        return new RedirectView("swagger-ui.html");
    }

    @Operation(summary = "Hent ut liste over tilgjengelege tidsseriar")
    @GetMapping("/meta")
    public List<TimeSeriesDefinition> available() {
        return service.availableTimeSeries();
    }

    @Operation(summary = "Hent ut liste over tilgjengelege kategorier")
    @GetMapping("/categories")
    public Set<OwnerCategories> categories() throws IOException {
        return service.categories();
    }

    @Operation(summary = "Hent data frå ein tidsserie")
    @GetMapping("/{owner}/{seriesName}/{distance}")
    public List<TimeSeriesPoint> query(
            @Parameter(name = "owner", example = "991825827", required = true, description = "eigar av tidsserien i form av eit organisasjonsnummer")
            @PathVariable String owner,
            @Parameter(name = "seriesName", example = "idporten-innlogging", required = true, description = "namn på tidsserie")
            @PathVariable String seriesName,
            @Parameter(name = "distance", required = true, description = "tidsserien sin måleavstand")
            @PathVariable MeasurementDistance distance,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String perCategory
    ) {
        TimeSeriesDefinition seriesDefinition = TimeSeriesDefinition.builder().name(seriesName).distance(distance).owner(owner);
        return service.query(seriesDefinition, queryFilter().range(from, to).categories(categories).perCategory(perCategory).build());
    }

    @Operation(summary = "Hent nyaste datapunkt frå ein tidsserie")
    @GetMapping("/{owner}/{seriesName}/{distance}/last")
    public TimeSeriesPoint last(
            @Parameter(name = "owner", example = "991825827", required = true, description = "eigar av tidsserien i form av eit organisasjonsnummer")
            @PathVariable String owner,
            @Parameter(name = "seriesName", example = "idporten-innlogging", required = true, description = "namn på tidsserie")
            @PathVariable String seriesName,
            @Parameter(name = "distance", required = true, description = "tidsserien sin måleavstand")
            @PathVariable MeasurementDistance distance,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(required = false) String categories
    ) {
        TimeSeriesDefinition seriesDefinition = TimeSeriesDefinition.builder().name(seriesName).distance(distance).owner(owner);
        return service.last(seriesDefinition, queryFilter().range(from, to).categories(categories).build());
    }

    @Operation(summary = "Hent nyaste datapunkt frå ein tidsserie")
    @GetMapping("{owner}/{seriesName}/{distance}/last/{targetDistance}")
    public List<TimeSeriesPoint> lastHistogram(
            @Parameter(name = "owner", example = "991825827", required = true, description = "eigar av tidsserien i form av eit organisasjonsnummer")
            @PathVariable String owner,
            @Parameter(name = "seriesName", example = "idporten-innlogging", required = true, description = "namn på tidsserie")
            @PathVariable String seriesName,
            @Parameter(name = "distance", required = true, description = "tidsserien sin måleavstand")
            @PathVariable MeasurementDistance distance,
            @PathVariable MeasurementDistance targetDistance,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(required = false) String categories
    ) {
        validateMeasurementDistance(distance, targetDistance);
        TimeSeriesDefinition seriesDefinition = TimeSeriesDefinition.builder().name(seriesName).distance(distance).owner(owner);
        return service.lastHistogram(seriesDefinition, targetDistance, queryFilter().range(from, to).categories(categories).build());
    }

    @Operation(summary = "Hent eit datapunkt med sum av målingar",
        description = "Returnerer eitt datapunkt")
    @GetMapping("{owner}/{seriesName}/{distance}/sum")
    public TimeSeriesPoint sum(
            @Parameter(name = "owner", example = "991825827", required = true, description = "eigar av tidsserien i form av eit organisasjonsnummer")
            @PathVariable String owner,
            @Parameter(name = "seriesName", example = "idporten-innlogging", required = true, description = "namn på tidsserie")
            @PathVariable String seriesName,
            @Parameter(name = "distance", required = true, description = "tidsserien sin måleavstand")
            @PathVariable MeasurementDistance distance,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(required = false) String categories
    ) {
        TimeSeriesDefinition seriesDefinition = TimeSeriesDefinition.builder().name(seriesName).distance(distance).owner(owner);
        return service.sum(seriesDefinition, queryFilter().range(from, to).categories(categories).build());
    }

    @Operation(summary = "Hent datapunkter med summar av målingar, omforma til ny måleavstand",
        description = "Ein tidsserie med måleavstand på timar kan for eksempel summerast opp på dag, månad eller årsnivå.")
    @GetMapping("{owner}/{seriesName}/{distance}/sum/{targetDistance}")
    public List<TimeSeriesPoint> sumHistogram(
            @Parameter(name = "owner", example = "991825827", required = true, description = "eigar av tidsserien i form av eit organisasjonsnummer")
            @PathVariable String owner,
            @Parameter(name = "seriesName", example = "idporten-innlogging", required = true, description = "namn på tidsserie")
            @PathVariable String seriesName,
            @Parameter(name = "distance", required = true, description = "tidsserien sin måleavstand")
            @PathVariable MeasurementDistance distance,
            @PathVariable MeasurementDistance targetDistance,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(required = false) String categories
    ) {
        validateMeasurementDistance(distance, targetDistance);
        TimeSeriesDefinition seriesDefinition = TimeSeriesDefinition.builder().name(seriesName).distance(distance).owner(owner);
        return service.sumHistogram(seriesDefinition, targetDistance, queryFilter().range(from, to).categories(categories).build());
    }

    @GetMapping(path = "{owner}/{seriesName}/{distance}/percentile", params = {"percentile", "measurementId", "operator"})
    @Operation(summary = "", description = "<b>Experimental feature -- use at your own risk. Categorized series are not supported.</b>")
    public List<TimeSeriesPoint> relationalToPercentile(
            @Parameter(name = "owner", example = "991825827", required = true, description = "eigar av tidsserien i form av eit organisasjonsnummer")
            @PathVariable String owner,
            @Parameter(name = "seriesName", example = "idporten-innlogging", required = true, description = "namn på tidsserie")
            @PathVariable String seriesName,
            @Parameter(name = "distance", required = true, description = "tidsserien sin måleavstand")
            @PathVariable MeasurementDistance distance,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime to,
            @RequestParam(required = false) String categories,
            @RequestParam int percentile,
            @RequestParam String measurementId,
            @RequestParam RelationalOperator operator
    ) {
        TimeSeriesDefinition seriesDefinition = TimeSeriesDefinition.builder().name(seriesName).distance(distance).owner(owner);
        return service.query(seriesDefinition, queryFilter().range(from, to).build(), new PercentileFilter(percentile, measurementId, operator));
    }

    private void validateMeasurementDistance(MeasurementDistance distance, MeasurementDistance targetDistance) {
        if (distance.ordinal() >= targetDistance.ordinal())
            throw new IllegalArgumentException(format("Distance %s is greater than or equal to target distance %s", distance, targetDistance));
    }

}
