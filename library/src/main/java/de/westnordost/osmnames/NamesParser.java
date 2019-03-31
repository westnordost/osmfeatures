package de.westnordost.osmnames;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NamesParser
{
	public List<Entry> parse(String json) { return parse(new JSONTokener(json)); }
	public List<Entry> parse(InputStream is) { return parse(new JSONTokener(is)); }
	public List<Entry> parse(Reader reader) { return parse(new JSONTokener(reader)); }

	private List<Entry> parse(JSONTokener tokener)
	{
		JSONArray array = new JSONArray(tokener);
		List<Entry> result = new ArrayList<>(array.length());
		for (int i = 0; i < array.length(); i++)
		{
			JSONObject item = array.getJSONObject(i);
			List<String> names = parseStringArray(item.getJSONArray("names"));
			List<String> keywords = parseStringArray(item.optJSONArray("keywords"));
			Map<String,String> tags = parseStringMap(item.getJSONObject("tags"));
			result.add(new Entry(names, tags, keywords, 0));
		}
		return result;
	}

	private static List<String> parseStringArray(JSONArray array)
	{
		if(array == null) return null;
		List<String> result = new ArrayList<>(array.length());
		for (int i = 0; i < array.length(); i++)
		{
			result.add(array.getString(i));
		}
		return result;
	}

	private static Map<String, String> parseStringMap(JSONObject map)
	{
		if(map == null) return null;
		Set<String> keys = map.keySet();
		Map<String, String> result = new HashMap<>(keys.size());
		for (String key : keys)
		{
			result.put(key, map.getString(key));
		}
		return result;
	}
}
