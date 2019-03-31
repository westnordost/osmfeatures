package de.westnordost.osmnames;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Parse the dist/name-suggestions.json from https://github.com/osmlab/name-suggestion-index
 *  into entries grouped by ISO 3166-1 alpha 2 country codes. The key <tt>null</tt> in this map
 *  contains brand names for all countries. */
public class BrandNamesParser
{
	public Map<String, List<Entry>> parse(String json) { return parse(new JSONTokener(json)); }
	public Map<String, List<Entry>> parse(InputStream is) { return parse(new JSONTokener(is)); }
	public Map<String, List<Entry>> parse(Reader reader) { return parse(new JSONTokener(reader)); }

	private Map<String, List<Entry>> parse(JSONTokener tokener)
	{
		JSONObject object = new JSONObject(tokener);
		Map<String, List<Entry>> result = new HashMap<>();
		for (String key : object.keySet())
		{
			JSONObject category = object.getJSONObject(key); // i.e. shop, amenity, ...
			for (String value : category.keySet())
			{
				JSONObject place = category.getJSONObject(value); // i.e. supermarket, bank, ...
				for (String brand : place.keySet())
				{
					JSONObject item = place.getJSONObject(brand);

					Map<String,String> tags = parseStringMap(item.getJSONObject("tags"));
					int count = item.optInt("count", 0);
					Entry entry = new Entry(Collections.singletonList(brand), tags, null, count);

					JSONArray countryCodes = item.optJSONArray("countryCodes");
					if(countryCodes != null)
					{
						for (int i = 0; i < countryCodes.length(); i++)
						{
							String countryCode = countryCodes.getString(i).toUpperCase(Locale.US);
							putToMultiMap(result, countryCode, entry);
						}
					} else {
						putToMultiMap(result, null, entry);
					}
				}
			}
		}

		return result;
	}

	private static <T> void putToMultiMap(Map<String, List<T>> map, String key, T value)
	{
		if(!map.containsKey(key)) map.put(key, new ArrayList<>());
		map.get(key).add(value);
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
