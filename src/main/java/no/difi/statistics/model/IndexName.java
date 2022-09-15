package no.difi.statistics.model;

import java.util.Objects;
import java.util.Set;

public class IndexName {

    private String owner;
    private String name;
    private String distance;
    private Set<String> categories;

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public IndexName(String owner, String name, String distance) {
        this.owner = owner;
        this.name = name;
        this.distance = distance;
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

        IndexName other = (IndexName) obj;
        return Objects.equals(owner, other.owner) &&
                        Objects.equals(name, other.name) &&
                        Objects.equals(distance, other.distance);
    }

    @Override
    public String toString() {
        return this.owner + ", " + this.name + ", " + this.distance;
    }
}
