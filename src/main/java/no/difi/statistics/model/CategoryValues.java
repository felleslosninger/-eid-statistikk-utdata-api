package no.difi.statistics.model;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CategoryValues {

    private final String owner;
    private final String name;
    private final String distance;
    private final Set<Map<String, Object>> categories;

    public Set<Map<String, Object>> getCategories() {
        return categories;
    }

    public CategoryValues(String owner, String name, String distance) {
        this.owner = owner;
        this.name = name;
        this.distance = distance;
        this.categories = new HashSet<>();
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDistance() {
        return distance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, distance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        CategoryValues other = (CategoryValues) obj;
        return Objects.equals(owner, other.owner) &&
                Objects.equals(name, other.name) &&
                Objects.equals(distance, other.distance);
    }

    @Override
    public String toString() {
        return this.owner + ", " + this.name + ", " + this.distance;
    }

}
