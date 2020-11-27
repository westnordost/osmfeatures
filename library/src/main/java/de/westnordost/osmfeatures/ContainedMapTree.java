package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Index that makes finding which maps are completely contained by a given map very efficient.
 *  It sorts the maps into a tree structure with configurable depth.
 *
 *  It is threadsafe because it is immutable.
 *
 *  For example for the string maps...
 *  <pre>
 *  [
 *    #1 (amenity -> bicycle_parking),
 *    #2 (amenity -> bicycle_parking, bicycle_parking -> shed),
 *    #3 (amenity -> bicycle_parking, bicycle_parking -> lockers),
 *    #4 (amenity -> taxi),
 *    #5 (shop -> supermarket),
 *  ]
 *  </pre>
 *  ...the tree internally looks like this:
 *  <pre>
 *  amenity ->
 *    bicycle_parking ->
 *      #1
 *      bicycle_parking ->
 *        shed ->
 *          #2
 *        lockers ->
 *          #3
 *    taxi ->
 *      #4
 *  shop ->
 *    supermarket ->
 *      #5
 *  ...
 *  </pre>
 *  */
class ContainedMapTree<K,V>
{
    private final Node<K,V> root;

    ContainedMapTree(Collection<Map<K,V>> maps)
    {
        this(maps, 4, 4);
    }

    /** Create this index with the given maps.
     *
     *  The generated tree will have a max depth of maxDepth and another depth is not added to the
     *  tree if there are less than minContainerSize maps in one tree node.
     */
    ContainedMapTree(Collection<Map<K,V>> maps, int maxDepth, int minContainerSize)
    {
        if (maxDepth < 0) maxDepth = 0;
        root = buildTree(maps, Collections.emptyList(), maxDepth, minContainerSize);
    }

    /** Get all maps whose entries are completely contained by the given map */
    List<Map<K,V>> getAll(Map<K,V> map)
    {
        return root.getAll(map);
    }

    private static <K,V> Node<K,V> buildTree(Collection<Map<K,V>> maps, Collection<K> previousKeys, int maxDepth, int minContainerSize)
    {
        if (previousKeys.size() == maxDepth || maps.size() < minContainerSize)
            return new Node<>(null, maps);

        HashMap<K, Map<V, Node<K,V>>> result = new HashMap<>();
        Set<Map<K,V>> unsortedMaps = new HashSet<>(maps);

        Map<K, List<Map<K,V>>> mapsByKey = getMapsByKey(maps, previousKeys);

        /* the map should be categorized by frequent keys first and least frequent keys last. */
        List<Map.Entry<K, List<Map<K,V>>>> sortedByCountDesc = new ArrayList<>(mapsByKey.entrySet());
        Collections.sort(sortedByCountDesc, (a, b) -> b.getValue().size() - a.getValue().size());

        for (Map.Entry<K, List<Map<K,V>>> keyToMaps : sortedByCountDesc)
        {
            K key = keyToMaps.getKey();
            List<Map<K,V>> mapsForKey = keyToMaps.getValue();

            // a map already sorted in a certain node should not be sorted into another too
            mapsForKey.retainAll(unsortedMaps);
            if (mapsForKey.isEmpty()) continue;

            Map<V, List<Map<K,V>>> featuresByValue = getMapsByKeyValue(key, mapsForKey);

            Map<V, Node<K,V>> valueNodes = new HashMap<>();
            for (Map.Entry<V, List<Map<K,V>>> valueToFeatures : featuresByValue.entrySet())
            {
                V value = valueToFeatures.getKey();
                List<Map<K,V>> featuresForValue = valueToFeatures.getValue();
                List<K> previousKeysNow = new ArrayList<>(previousKeys);
                previousKeysNow.add(key);
                valueNodes.put(value, buildTree(featuresForValue, previousKeysNow, maxDepth, minContainerSize));
            }

            result.put(key, valueNodes);

            unsortedMaps.removeAll(mapsForKey);
        }

        return new Node<>(result, new ArrayList<>(unsortedMaps));
    }

    /** returns the given features grouped by the map entry value of the given key. */
    private static <K,V> Map<V, List<Map<K,V>>> getMapsByKeyValue(K key, Collection<Map<K,V>> maps)
    {
        HashMap<V, List<Map<K,V>>> result = new HashMap<>();
        for (Map<K,V> map : maps)
        {
            V value = map.get(key);
            if (!result.containsKey(value)) result.put(value, new ArrayList<>());
            result.get(value).add(map);
        }
        return result;
    }

    /** returns the given maps grouped by each of their keys (except the given ones). */
    private static <K,V> Map<K, List<Map<K,V>>> getMapsByKey(Collection<Map<K,V>> maps, Collection<K> excludeKeys)
    {
        HashMap<K, List<Map<K,V>>> result = new HashMap<>();
        for (Map<K,V> map : maps)
        {
            for (K key : map.keySet())
            {
                if (excludeKeys.contains(key)) continue;
                if (!result.containsKey(key)) result.put(key, new ArrayList<>());
                result.get(key).add(map);
            }
        }
        return result;
    }

    private static class Node<K,V>
    {
        /** key -> (value -> Node) */
        final Map<K, Map<V, Node<K,V>>> children;
        final Collection<Map<K,V>> maps;

        private Node(Map<K, Map<V, Node<K,V>>> children, Collection<Map<K,V>> maps)
        {
            this.children = children;
            this.maps = maps;
        }

        /** Get all maps whose entries are all contained by given map */
        private List<Map<K,V>> getAll(Map<K,V> map)
        {
            List<Map<K,V>> result = new ArrayList<>();
            if (children != null)
            {
                for (Map.Entry<K, Map<V, Node<K,V>>> keyToValues : children.entrySet())
                {
                    K key = keyToValues.getKey();
                    if (map.containsKey(key))
                    {
                        for (Map.Entry<V, Node<K,V>> valueToNode : keyToValues.getValue().entrySet())
                        {
                            V value = valueToNode.getKey();
                            if (value.equals(map.get(key)))
                            {
                                result.addAll(valueToNode.getValue().getAll(map));
                            }
                        }
                    }
                }
            }
            if (maps != null)
            {
                for (Map<K,V> m : maps)
                {
                    if (CollectionUtils.mapContainsAllEntries(map, m.entrySet()))
                    {
                        result.add(m);
                    }
                }
            }
            return result;
        }
    }
}
