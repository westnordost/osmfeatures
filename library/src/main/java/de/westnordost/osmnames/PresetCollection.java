package de.westnordost.osmnames;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class PresetCollection
{
	private static final String PRESETS_FILE = "presets.json";

	private final NamesDictionary.FileAccessAdapter fileAccess;

	// id -> preset
	private final Map<String, Preset> presets;
	// locale -> ( id -> preset )
	private final Map<Locale, Map<String, Preset>> localizedPresets = new HashMap<>();

	PresetCollection(NamesDictionary.FileAccessAdapter fileAccess) throws IOException
	{
		this.fileAccess = fileAccess;
		presets = loadPresets();
		// already load localization for default locale
		getAll(Locale.getDefault());
	}

	Collection<Preset> getAll() { return getAll(Locale.getDefault()); }

	Collection<Preset> getAll(Locale locale)
	{
		return Collections.unmodifiableCollection(
				getOrLoadLocalizedPresetsWithFallback(locale).values()
		);
	}

	Preset get(String id) { return get(id, Locale.getDefault()); }

	Preset get(String id, Locale locale)
	{
		return getOrLoadLocalizedPresetsWithFallback(locale).get(id);
	}

	private Map<String, Preset> loadPresets() throws IOException
	{
		try(InputStream is = fileAccess.open(PRESETS_FILE)) { return parsePresets(is); }
	}

	private static Map<String, Preset> parsePresets(InputStream is)
	{
		JSONObject object = new JSONObject(new JSONTokener(is));
		Map<String, Preset> result = new HashMap<>();
		JSONObject presetObjects = object.getJSONObject("presets");
		for (String id : presetObjects.keySet())
		{
			JSONObject p = presetObjects.getJSONObject(id);
			Map<String,String> tags = parseStringMap(p.getJSONObject("tags"));
			// drop presets with * in key or value of tags (for now), because they never describe
			// a concrete thing, but some category of things.
			// TODO maybe drop this limitation
			if(anyKeyOrValueContainsWildcard(tags)) continue;

			List<GeometryType> geometry = parseList(p.getJSONArray("geometry"),
					item -> GeometryType.valueOf(((String)item).toUpperCase(Locale.US)));
			String name = p.optString("name");
			List<String> terms = parseList(p.optJSONArray("terms"), item -> (String)item);
			List<String> countryCodes = parseList(p.optJSONArray("countryCodes"),
					item -> ((String)item).toUpperCase(Locale.US).intern());
			boolean searchable = p.optBoolean("searchable", true);
			float matchScore = p.optFloat("matchScore", 1.0f);
			boolean suggestion = p.optBoolean("suggestion", false);
			Map<String,String> addTags = parseStringMap(p.optJSONObject("addTags"));

			result.put(id.intern(), new Preset(
					id.intern(), tags, geometry, name, terms, countryCodes, searchable, matchScore,
					suggestion, addTags
			));
		}
		return result;
	}

	private static boolean anyKeyOrValueContainsWildcard(Map<String,String> map)
	{
		for (Map.Entry<String, String> e : map.entrySet())
		{
			if(e.getKey().contains("*") || e.getValue().contains("*")) return true;
		}
		return false;
	}

	private interface Transformer<T> { T apply(Object item); }
	private static <T> List<T> parseList(JSONArray array, Transformer<T> t)
	{
		if(array == null) return null;
		List<T> result = new ArrayList<>(array.length());
		for (int i = 0; i < array.length(); i++)
		{
			result.add(t.apply(array.get(i)));
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
			result.put(key.intern(), map.getString(key));
		}
		return result;
	}


	private Map<String, Preset> getOrLoadLocalizedPresetsWithFallback(Locale locale)
	{
		Map<String, Preset> entries = getOrLoadLocalizedPresets(locale);
		if(entries == null && locale.getCountry().length() > 0)
		{
			Locale languageOnly = new Locale(locale.getLanguage());
			entries = getOrLoadLocalizedPresets(languageOnly);
			if(entries == null)
			{
				entries = presets;
			}
		}
		assert entries != null;
		return entries;
	}

	private Map<String, Preset> getOrLoadLocalizedPresets(Locale locale)
	{
		synchronized (localizedPresets)
		{
			if (!localizedPresets.containsKey(locale))
			{
				localizedPresets.put(locale, loadLocalizedPresets(locale));
			}
			return localizedPresets.get(locale);
		}
	}

	private Map<String, Preset> loadLocalizedPresets(Locale locale)
	{
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		String filename = lang + (country.length() > 0 ? "-" + country : "") + ".json";
		try
		{
			if (!fileAccess.exists(filename)) return null;
			try (InputStream is = fileAccess.open(filename))
			{
				return parseLocalizedPresets(is);
			}
		}
		catch (IOException e) { throw new RuntimeException(e); }
	}

	private Map<String, Preset> parseLocalizedPresets(InputStream is)
	{
		JSONObject object = new JSONObject(new JSONTokener(is));
		Map<String, Preset> result = new HashMap<>();
		JSONObject presetsObject = object.getJSONObject("presets");
		for (String id : presetsObject.keySet())
		{
			id = id.intern();
			JSONObject localization = presetsObject.getJSONObject(id);

			String name = localization.getString("name");
			List<String> terms = new ArrayList<>(Arrays.asList(
					localization.optString("terms","").split("\\s*,\\s*"))
			);
			// remove duplicate name from terms
			terms.remove(name);

			result.put(id, new Preset(presets.get(id), name, terms));
		}
		return result;
	}
}
