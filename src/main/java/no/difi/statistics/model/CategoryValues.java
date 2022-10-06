package no.difi.statistics.model;

import java.util.Map;
import java.util.Set;

public class CategoryValues extends Timeseries {

    private Set<Map<String, String>> categoryValues;

    public CategoryValues(String owner, String name, String distance) {
        super(owner, name, distance);
    }

}
