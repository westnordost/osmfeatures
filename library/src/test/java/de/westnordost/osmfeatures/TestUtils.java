package de.westnordost.osmfeatures;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class TestUtils {
    public static <T> void assertEqualsIgnoreOrder(Collection<T> a, Collection<T> b)
    {
        assertTrue(a.size() == b.size() && a.containsAll(b));
    }

    @SafeVarargs public static <T> List<T> listOf(T... items) { return Arrays.asList(items); }
}
