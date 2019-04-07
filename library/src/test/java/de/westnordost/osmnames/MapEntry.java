package de.westnordost.osmnames;

import java.util.HashMap;
import java.util.Map;

class MapEntry
{
	private String key, value;
	private MapEntry(String key, String value)
	{
		this.key = key;
		this.value = value;
	}

	static MapEntry tag(String key, String value) { return new MapEntry(key, value); }

	static Map<String, String> mapOf(MapEntry... items)
	{
		Map<String, String> result = new HashMap<>();
		for (MapEntry item : items) result.put(item.key, item.value);
		return result;
	}
}
