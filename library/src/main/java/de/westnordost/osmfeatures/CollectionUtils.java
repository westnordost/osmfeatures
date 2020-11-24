package de.westnordost.osmfeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class CollectionUtils {

    /** Whether all entries of the given map are also in the given entries */
    static <K,V> boolean mapContainedInEntries(Map<K,V> map, Iterable<Map.Entry<K,V>> entries)
    {
        int found = 0;
        int size = map.size();
        for (Map.Entry<K, V> entry : entries)
        {
            if(mapContainsEntry(map, entry)) found++;
            if(found == size) return true;
        }
        return false;
    }

    /** Number of entries contained in the given map */
    static <K,V> int numberOfContainedEntriesInMap(Map<K,V> map, Iterable<Map.Entry<K,V>> entries)
    {
        int found = 0;
        for (Map.Entry<K, V> entry : entries)
        {
            if(mapContainsEntry(map, entry)) found++;
        }
        return found;
    }

    /** Whether the given map contains the given entry */
    static <K,V> boolean mapContainsEntry(Map<K,V> map, Map.Entry<K,V> entry)
    {
        V mapValue = map.get(entry.getKey());
        V value = entry.getValue();
        return mapValue == value || value != null && value.equals(mapValue);
    }

    interface Predicate<T> { boolean fn(T v); }

    /** Return a list of all elements in the given collection that fulfill the given predicate */
    static <T> List<T> filter(Iterable<T> collection, Predicate<T> predicate)
    {
        List<T> result = new ArrayList<>();
        for (T v : collection) if(predicate.fn(v)) result.add(v);
        return result;
    }
}
