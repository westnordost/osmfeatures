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
	private final Map<String, Preset> allPresets;
	private final Map<String, Preset> suggestionPresets;
	// locale -> ( id -> preset )
	private final Map<Locale, Map<String, Preset>> localizedPresets = new HashMap<>();

	PresetCollection(NamesDictionary.FileAccessAdapter fileAccess)
	{
		this.fileAccess = fileAccess;
		allPresets = loadPresets();
		suggestionPresets = new HashMap<>();
		for (Preset preset : allPresets.values())
		{
			if(preset.suggestion) suggestionPresets.put(preset.id, preset);
		}
		// already load localization for default locale
		getOrLoadLocalizedPresets(Locale.getDefault());
	}

	Collection<Preset> getAll() { return getAll(Locale.getDefault()); }

	Collection<Preset> getAll(Locale locale)
	{
		return Collections.unmodifiableCollection(getOrLoadLocalizedPresets(locale).values());
	}

	Preset get(String id) { return get(id, Locale.getDefault()); }

	Preset get(String id, Locale locale)
	{
		return getOrLoadLocalizedPresets(locale).get(id);
	}

	private Map<String, Preset> loadPresets()
	{
		try(InputStream is = fileAccess.open(PRESETS_FILE)) { return parsePresets(is); }
		catch (IOException e) { throw new RuntimeException(e); }
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
			boolean suggestion = p.optBoolean("suggestion", false);
			String name;
			if(suggestion) name = p.getString("name");
			else           name = p.optString("name", null);
			List<String> terms = parseList(p.optJSONArray("terms"), item -> (String)item);
			List<String> countryCodes = parseList(p.optJSONArray("countryCodes"),
					item -> ((String)item).toUpperCase(Locale.US).intern());
			boolean searchable = p.optBoolean("searchable", true);
			float matchScore = p.optFloat("matchScore", 1.0f);
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

	private Map<String, Preset> getOrLoadLocalizedPresets(Locale locale)
	{
		synchronized (localizedPresets)
		{
			if (!localizedPresets.containsKey(locale))
			{
				Locale localeLanguage = new Locale(locale.getLanguage());
				if (!localizedPresets.containsKey(localeLanguage))
				{
					localizedPresets.put(localeLanguage, loadLocalizedPresets(localeLanguage));
				}
				// merging language presets with country presets (country presets overwrite language presets)
				if(!locale.getCountry().isEmpty())
				{
					Map<String, Preset> languagePresets = localizedPresets.get(localeLanguage);
					Map<String, Preset> countryPresets = loadLocalizedPresets(locale);
// TODO should return null if empty? -> and then fallback to suggestion presets on getAll()?
					Map<String, Preset> localePresets = new HashMap<>();
					if(languagePresets != null) localePresets.putAll(languagePresets);
					if(countryPresets != null) localePresets.putAll(countryPresets);

					localizedPresets.put(locale, localePresets);
				}
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
		Map<String, Preset> result = new HashMap<>(suggestionPresets);
		JSONObject presetsObject = object.getJSONObject("presets");
		for (String id : presetsObject.keySet())
		{
			id = id.intern();
			JSONObject localization = presetsObject.getJSONObject(id);

			String name = localization.getString("name");
			String termsStr = localization.optString("terms", null);
			List<String> terms = null;
			if(termsStr != null)
			{
				terms = new ArrayList<>(Arrays.asList(termsStr.split("\\s*,\\s*")));
				// remove duplicate name from terms
				terms.remove(name);
			}

			Preset base = allPresets.get(id);
			// no base preset found! Skip this
			if(base == null) continue;

			result.put(id, new Preset(base, name, terms));
		}
		return result;
	}
}
