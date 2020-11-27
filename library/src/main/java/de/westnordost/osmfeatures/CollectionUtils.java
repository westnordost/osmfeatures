package de.westnordost.osmfeatures;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

class CollectionUtils {

    public interface CreateFn<K,V> { V create(K value); }

    public static <K,V> V synchronizedGetOrCreate(Map<K,V> map, K key, CreateFn<K,V> createFn)
    {
        if (!map.containsKey(key))
        {
            synchronized (map)
            {
                if (!map.containsKey(key))
                {
                    map.put(key, createFn.create(key));
                }
            }
        }
        return map.get(key);
    }

    /** Whether the given map contains all the given entries */
    public static <K,V> boolean mapContainsAllEntries(Map<K,V> map, Iterable<Map.Entry<K,V>> entries)
    {
        for (Map.Entry<K, V> entry : entries)
        {
            if(!mapContainsEntry(map, entry)) return false;
        }
        return true;
    }

    /** Number of entries contained in the given map */
    public static <K,V> int numberOfContainedEntriesInMap(Map<K,V> map, Iterable<Map.Entry<K,V>> entries)
    {
        int found = 0;
        for (Map.Entry<K, V> entry : entries)
        {
            if(mapContainsEntry(map, entry)) found++;
        }
        return found;
    }

    /** Whether the given map contains the given entry */
    public static <K,V> boolean mapContainsEntry(Map<K,V> map, Map.Entry<K,V> entry)
    {
        V mapValue = map.get(entry.getKey());
        V value = entry.getValue();
        return mapValue == value || value != null && value.equals(mapValue);
    }

    public interface Predicate<T> { boolean fn(T v); }

    /** Backport of Collection.removeIf */
    public static <T> void removeIf(Collection<T> list, Predicate<T> predicate)
    {
        Iterator<T> it = list.iterator();
        while(it.hasNext()) if (predicate.fn(it.next())) it.remove();
    }
}
