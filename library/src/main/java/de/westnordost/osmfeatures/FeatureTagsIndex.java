package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Index that makes finding Features whose tags are completely contained by a given set of tags
 *  very efficient.
 *
 *  Based on ContainedMapTree data structure, see that class. */
class FeatureTagsIndex
{
    private final Map<Map<String, String>, List<Feature>> featureMap;
    private final ContainedMapTree<String, String> tree;

    public FeatureTagsIndex(Iterable<Feature> features)
    {
        featureMap = new HashMap<>();
        for (Feature feature : features)
        {
            Map<String, String> map = feature.getTags();
            if (!featureMap.containsKey(map)) featureMap.put(map, new ArrayList<>());
            featureMap.get(map).add(feature);
        }
        tree = new ContainedMapTree<>(featureMap.keySet());
    }

    public List<Feature> getAll(Map<String, String> tags)
    {
        List<Feature> result = new ArrayList<>();
        for (Map<String, String> map : tree.getAll(tags))
        {
            List<Feature> fs = featureMap.get(map);
            if (fs != null) result.addAll(fs);
        }
        return result;
    }
}
