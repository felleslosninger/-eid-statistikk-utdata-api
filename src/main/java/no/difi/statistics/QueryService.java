package no.difi.statistics;

import no.difi.statistics.model.*;
import no.difi.statistics.model.QueryFilter;

import java.util.List;

public interface QueryService {

    List<TimeSeriesDefinition> availableTimeSeries();

    TimeSeriesPoint last(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> query(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> query(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter, PercentileFilter filter);

    List<TimeSeriesPoint> lastHistogram(TimeSeriesDefinition seriesDefinition, MeasurementDistance targetDistance, QueryFilter queryFilter);

    TimeSeriesPoint sum(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> sumHistogram(TimeSeriesDefinition seriesDefinition, MeasurementDistance targetDistance, QueryFilter queryFilter);

    List<TimeSeriesPoint> query(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);
}