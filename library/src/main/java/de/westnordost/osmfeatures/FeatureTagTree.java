package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.westnordost.osmfeatures.CollectionUtils.mapContainsAllEntries;

/** A tree to efficiently find Features that match a given set of tags. It basically acts like
 *  an index.
 *
 *  The tree internally looks f.e. like this:
 *  <pre>
 *  amenity ->
 *    bicycle_parking ->
 *      [Feature object: Generic Bicycle parking]
 *      bicycle_parking ->
 *        shed ->
 *          [Feature object: Bicycle shed]
 *        lockers ->
 *          [Feature object: Bicycle lockers]
 *    taxi ->
 *      [Feature object: Taxi stand]
 *  shop ->
 *    supermarket ->
 *      [Feature object: Supermarket]
 *  ...
 *  </pre>
 *  */
class FeatureTagTree {

    private final Node root;

    FeatureTagTree(Collection<Feature> features)
    {
        root = buildTree(features, Collections.emptyList());
    }

    List<Feature> getAll(Map<String, String> tags)
    {
        return root.getAll(tags);
    }

    private static Node buildTree(Collection<Feature> features, Collection<String> previousKeys)
    {
        HashMap<String, Map<String, Node>> result = new HashMap<>();
        Set<Feature> unsortedFeatures = new HashSet<>(features);

        Map<String, List<Feature>> featuresByKey = getFeaturesByKey(features, previousKeys);

        /* the map should be categorized by frequent tags first and least frequent tags last. For
        *  example, the resulting tree should look like in the documentation comment for the class
        *  and not like
        * <pre>
        *  bicycle_parking ->
        *    shed ->
        *      amenity ->
        *        bicycle_parking ->
        *          [Feature object: Bicycle shed]
        *    lockers ->
        *      amenity ->
        *        bicycle_parking ->
        *          [Feature object: Bicycle lockers]
        * </pre>
        * */
        ArrayList<Map.Entry<String, List<Feature>>> sortedByCountDesc = new ArrayList<>(featuresByKey.entrySet());
        Collections.sort(sortedByCountDesc, (a, b) -> b.getValue().size() - a.getValue().size());

        for (Map.Entry<String, List<Feature>> keyToFeatures : sortedByCountDesc)
        {
            String key = keyToFeatures.getKey();
            List<Feature> featuresForKey = keyToFeatures.getValue();

            // a feature already sorted in a certain node should not be sorted into another, too
            featuresForKey.retainAll(unsortedFeatures);
            if (featuresForKey.isEmpty()) continue;

            Map<String, List<Feature>> featuresByValue = getFeaturesByValue(key, featuresForKey);

            Map<String, Node> valueNodes = new HashMap<>();
            for (Map.Entry<String, List<Feature>> valueToFeatures : featuresByValue.entrySet()) {
                String value = valueToFeatures.getKey();
                List<Feature> featuresForValue = valueToFeatures.getValue();
                List<String> previousKeysNow = new ArrayList<>(previousKeys);
                previousKeysNow.add(key);
                valueNodes.put(value, buildTree(featuresForValue, previousKeysNow));
            }

            result.put(key, valueNodes);

            unsortedFeatures.removeAll(featuresForKey);
        }

        // if there is only one (or no) child, let this be the leaf node
        if (result.isEmpty() || result.size() == 1 && result.values().iterator().next().size() == 1)
            return new Node(null, features);
        else
            return new Node(result, new ArrayList<>(unsortedFeatures));
    }

    /** returns the given features grouped by the tag value of the given key */
    private static Map<String, List<Feature>> getFeaturesByValue(String key, Collection<Feature> features)
    {
        HashMap<String, List<Feature>> result = new HashMap<>();
        for (Feature feature : features)
        {
            String value = feature.getTags().get(key);
            if (!result.containsKey(value)) result.put(value, new ArrayList<>());
            result.get(value).add(feature);
        }
        return result;
    }

    /** returns the given features grouped by each of their keys (except the given ones) */
    private static Map<String, List<Feature>> getFeaturesByKey(Collection<Feature> features, Collection<String> excludeKeys)
    {
        HashMap<String, List<Feature>> result = new HashMap<>();
        for (Feature feature : features)
        {
            for (String key : feature.getTags().keySet())
            {
                if (excludeKeys.contains(key)) continue;
                if (!result.containsKey(key)) result.put(key, new ArrayList<>());
                result.get(key).add(feature);
            }
        }
        return result;
    }

    private static class Node
    {
        /** key -> value -> Node */
        final Map<String, Map<String, Node>> children;
        final Collection<Feature> features;

        private Node(Map<String, Map<String, Node>> children, Collection<Feature> features)
        {
            this.children = children;
            this.features = features;
        }

        private List<Feature> getAll(Map<String, String> tags)
        {
            List<Feature> result = new ArrayList<>();
            if (children != null) {
                for (Map.Entry<String, Map<String, Node>> keyToValues : children.entrySet())
                {
                    String key = keyToValues.getKey();
                    if (tags.containsKey(key))
                    {
                        for (Map.Entry<String, Node> valueToNode : keyToValues.getValue().entrySet())
                        {
                            String value = valueToNode.getKey();
                            if (value.equals(tags.get(key)))
                            {
                                result.addAll(valueToNode.getValue().getAll(tags));
                            }
                        }
                    }
                }
            }
            if (features != null)
            {
                for (Feature feature : features)
                {
                    if (mapContainsAllEntries(tags, feature.getTags().entrySet()))
                    {
                        result.add(feature);
                    }
                }
            }
            return result;
        }
    }
}
