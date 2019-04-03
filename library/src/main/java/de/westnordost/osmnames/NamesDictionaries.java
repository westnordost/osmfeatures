package de.westnordost.osmnames;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class NamesDictionaries
{
	public interface FileAccessAdapter
	{
		boolean exists(String name) throws IOException;
		InputStream open(String name) throws IOException;
	}
	private final FileAccessAdapter fileAccess;

	private final Object
			namesMutex = new Object(),
			brandNamesMutex = new Object();

	private final Map<Locale, List<Entry>> names = new HashMap<>();
	private Map<String, List<Entry>> allBrandNames;

	public NamesDictionaries(FileAccessAdapter fileAccess)
	{
		this.fileAccess = fileAccess;
	}

	public static NamesDictionaries create(String path)
	{
		return new NamesDictionaries(new FileSystemAccess(new File(path)));
	}

	public NamesDictionary get(Locale locale) throws IOException
	{
		return new NamesDictionary(getNamesWithFallbackToLanguageOnly(locale));
	}

	/* Localized names */

	private List<Entry> getNamesWithFallbackToLanguageOnly(Locale locale) throws IOException
	{
		List<Entry> entries = getOrLoadNames(locale);
		if(entries == null && locale.getCountry().length() > 0)
		{
			Locale languageOnly = new Locale(locale.getLanguage());
			return getOrLoadNames(languageOnly);
		}
		return entries;
	}

	private List<Entry> getOrLoadNames(Locale locale) throws IOException
	{
		synchronized (namesMutex)
		{
			if (!names.containsKey(locale))
			{
				names.put(locale, loadNames(locale));
			}
			return names.get(locale);
		}
	}

	private List<Entry> loadNames(Locale locale) throws IOException
	{
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		String filename = lang + (country.length() > 0 ? "-" + country : "") + ".json";
		if(!fileAccess.exists(filename)) return null;
		try (InputStream is = fileAccess.open(filename))
		{
			return parseNames(is);
		}
	}

	static List<Entry> parseNames(InputStream is)
	{
		JSONArray array = new JSONArray(new JSONTokener(is));
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

	/* Brand names */

	private List<Entry> getBrandNames(String countryCode) throws IOException
	{
		Map<String, List<Entry>> names = getOrLoadAllBrandNames();
		List<Entry> result = new ArrayList<>();
		List<Entry> anyBrandNames = names.get(null);
		if(anyBrandNames != null) result.addAll(anyBrandNames);
		if(countryCode != null)
		{
			List<Entry> countryBrandNames = names.get(countryCode.toUpperCase(Locale.US));
			if(countryBrandNames != null) result.addAll(countryBrandNames);
		}
		return result;
	}

	private Map<String, List<Entry>> getOrLoadAllBrandNames() throws IOException
	{
		synchronized (brandNamesMutex)
		{
			if (allBrandNames == null) allBrandNames = loadAllBrandNames();
			return allBrandNames;
		}
	}

	private Map<String, List<Entry>> loadAllBrandNames() throws IOException
	{
		try(InputStream is = fileAccess.open("name-suggestions.json"))
		{
			return parseBrandNames(is);
		}
	}

	/** Parse the dist/name-suggestions.json from https://github.com/osmlab/name-suggestion-index
	 *  into entries grouped by ISO 3166-1 alpha 2 country codes. The key <tt>null</tt> in this map
	 *  contains brand names for all countries. */
	static Map<String, List<Entry>> parseBrandNames(InputStream is)
	{
		JSONObject object = new JSONObject(new JSONTokener(is));
		Map<String, List<Entry>> result = new HashMap<>();
		for (String key : object.keySet())
		{
			JSONObject category = object.getJSONObject(key); // i.e. shop, amenity, ...
			for (String value : category.keySet())
			{
				JSONObject place = category.getJSONObject(value); // i.e. supermarket, bank, ...
				for (String brand : place.keySet())
				{
					brand = brand.intern();
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

	private static class FileSystemAccess implements FileAccessAdapter
	{
		private final File basePath;

		FileSystemAccess(File basePath)
		{
			if(!basePath.isDirectory()) throw new IllegalArgumentException("basePath must be a directory");
			this.basePath = basePath;
		}

		@Override public boolean exists(String name) { return new File(basePath, name).exists(); }
		@Override public InputStream open(String name) throws IOException
		{
			return new FileInputStream(new File(basePath, name));
		}
	}
}
