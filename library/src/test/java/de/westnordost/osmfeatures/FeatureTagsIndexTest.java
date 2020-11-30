package de.westnordost.osmfeatures;

import org.junit.Test;

import static de.westnordost.osmfeatures.MapEntry.mapOf;
import static de.westnordost.osmfeatures.MapEntry.tag;
import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.assertTrue;

public class FeatureTagsIndexTest
{
    @Test public void copes_with_empty_collection()
    {
        FeatureTagsIndex index = index();
        assertTrue(index.getAll(mapOf(tag("a", "b"))).isEmpty());
    }

    @Test public void get_two_features_with_same_tags()
    {
        Feature f1 = feature(tag("a", "b"));
        Feature f2 = feature(tag("a", "b"));
        FeatureTagsIndex index = index(f1, f2);
        assertEqualsIgnoreOrder(
                listOf(f1, f2),
                index.getAll(mapOf(tag("a", "b"), tag("c", "d")))
        );
    }

    @Test public void get_two_features_with_different_tags()
    {
        Feature f1 = feature(tag("a", "b"));
        Feature f2 = feature(tag("c", "d"));
        FeatureTagsIndex index = index(f1, f2);
        assertEqualsIgnoreOrder(
                listOf(f1, f2),
                index.getAll(mapOf(tag("a", "b"), tag("c", "d")))
        );
    }

    private static FeatureTagsIndex index(Feature... features)
    {
        return new FeatureTagsIndex(listOf(features));
    }

    private static Feature feature(MapEntry... mapEntries)
    {
        return new BaseFeature(
                "id",
                mapOf(mapEntries),
                listOf(GeometryType.POINT),
                "name",
                null, null,
                listOf(),
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
