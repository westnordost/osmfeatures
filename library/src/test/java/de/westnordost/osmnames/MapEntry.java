package de.westnordost.osmnames;

import java.util.HashMap;
import java.util.Map;

public class MapEntry
{
	public String key, value;
	public MapEntry(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	public static MapEntry tag(String key, String value) { return new MapEntry(key, value); }

	public static Map<String, String> mapOf(MapEntry... items)
	{
		Map<String, String> result = new HashMap<>();
		for (MapEntry item : items) result.put(item.key, item.value);
		return result;
	}
}
