package de.westnordost.osmfeatures;

import org.junit.Test;

import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertTrue;

public class FeatureTermIndexTest
{
    @Test public void copes_with_empty_collection()
    {
        FeatureTermIndex index = index();
        assertTrue(index.getAll("a").isEmpty());
    }


    @Test public void get_one_features_with_same_term()
    {
        Feature f1 = feature("a", "b");
        Feature f2 = feature("c");
        FeatureTermIndex index = index(f1, f2);
        assertEqualsIgnoreOrder(
                listOf(f1),
                index.getAll("b")
        );
    }

    @Test public void get_two_features_with_same_term()
    {
        Feature f1 = feature("a", "b");
        Feature f2 = feature("a", "c");
        FeatureTermIndex index = index(f1, f2);
        assertEqualsIgnoreOrder(
                listOf(f1, f2),
                index.getAll("a")
        );
    }

    @Test public void get_two_features_with_different_terms()
    {
        Feature f1 = feature("anything");
        Feature f2 = feature("anybody");
        FeatureTermIndex index = index(f1, f2);
        assertEqualsIgnoreOrder(
                listOf(f1, f2),
                index.getAll("any")
        );
        assertEqualsIgnoreOrder(
                listOf(f1),
                index.getAll("anyt")
        );
    }

    private static FeatureTermIndex index(Feature... features)
    {
        return new FeatureTermIndex(listOf(features), Feature::getTerms);
    }

    private static Feature feature(String... terms)
    {
        return new BaseFeature(
                "id",
                mapOf(),
                listOf(GeometryType.POINT),
                "name",
                null,
                null,
                listOf(terms),
                listOf(),
                listOf(),
                true,
                1.0,
                false,
                mapOf(),
                mapOf()
        );
    }
}
