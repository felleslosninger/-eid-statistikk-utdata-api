package no.difi.statistics;

import no.difi.statistics.model.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface QueryService {

    List<TimeSeriesDefinition> availableTimeSeries();

    Set<OwnerCategories> categories() throws IOException;

    TimeSeriesPoint last(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> query(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> query(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter, PercentileFilter filter);

    List<TimeSeriesPoint> lastHistogram(TimeSeriesDefinition seriesDefinition, MeasurementDistance targetDistance, QueryFilter queryFilter);

    TimeSeriesPoint sum(TimeSeriesDefinition seriesDefinition, QueryFilter queryFilter);

    List<TimeSeriesPoint> sumHistogram(TimeSeriesDefinition seriesDefinition, MeasurementDistance targetDistance, QueryFilter queryFilter);
}