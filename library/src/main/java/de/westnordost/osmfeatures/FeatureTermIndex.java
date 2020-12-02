package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Index that makes finding Features whose name/term/... starts with a given string very efficient.
 *
 *  Based on the StartsWithStringTree data structure, see that class. */
class FeatureTermIndex
{
    private final Map<String, List<Feature>> featureMap;
    private final StartsWithStringTree tree;

    public FeatureTermIndex(Iterable<Feature> features, Selector selector)
    {
        featureMap = new HashMap<>();
        for (Feature feature : features)
        {
            Collection<String> strings = selector.getStrings(feature);
            for (String string : strings)
            {
                if (!featureMap.containsKey(string)) featureMap.put(string, new ArrayList<>());
                featureMap.get(string).add(feature);
            }
        }
        tree = new StartsWithStringTree(featureMap.keySet());
    }

    public List<Feature> getAll(String startsWith)
    {
        Set<Feature> result = new HashSet<>();
        for (String string : tree.getAll(startsWith))
        {
            List<Feature> fs = featureMap.get(string);
            if (fs != null) result.addAll(fs);
        }
        return new ArrayList<>(result);
    }

    public interface Selector
    {
        List<String> getStrings(Feature feature);
    }
}
