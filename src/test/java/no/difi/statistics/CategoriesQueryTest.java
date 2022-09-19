package no.difi.statistics;

import no.difi.statistics.elasticsearch.commands.CategoriesQuery;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CategoriesQueryTest {

    @BeforeAll
    static void setup() {}

    @BeforeEach
    void init() {}

    @Test
    public void testAddition() {
        assertEquals(2, 1 + 1);
    }

    @Test
    public void testMinuteDistance() {
        CategoriesQuery categoriesQuery = new CategoriesQuery();
        String distanceToken = "minute2022";
        String distance = categoriesQuery.determineDistance(distanceToken);
        assertEquals("minutes", distance);
    }

    @Test
    public void testHourDistance() {
        CategoriesQuery categoriesQuery = new CategoriesQuery();
        String distanceToken = "hour2022";
        String distance = categoriesQuery.determineDistance(distanceToken);
        assertEquals("hours", distance);
    }
}
