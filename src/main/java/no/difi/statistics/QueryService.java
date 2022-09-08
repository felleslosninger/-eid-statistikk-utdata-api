package no.difi.statistics;

import no.difi.statistics.model.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public interface QueryService {

    List<TimeSeriesDefinition> availableTimeSeries();

    HashMap<IndexName, HashSet<String>> categories() throws IOException;

    TimeSeriesPoint last(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> query(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> query(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter, PercentileFilter filter);

    List<TimeSeriesPoint> lastHistogram(TimeSeriesDefinition seriesDefinition, MeasurementDistance targetDistance, QueryFilter queryFilter);

    TimeSeriesPoint sum(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> sumHistogram(TimeSeriesDefinition seriesDefinition, MeasurementDistance targetDistance, QueryFilter queryFilter);
}