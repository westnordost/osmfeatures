package de.westnordost.osmnames;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

class IDPresetCollection implements PresetCollection
{
	private static final String PRESETS_FILE = "presets.json";

	public interface FileAccessAdapter
	{
		boolean exists(String name) throws IOException;
		InputStream open(String name) throws IOException;
	}

	private final FileAccessAdapter fileAccess;

	// id -> preset
	private final Map<String, Preset> allPresets;
	// locale -> ( id -> preset )
	private final Map<Locale, Map<String, Preset>> localizedPresets = new HashMap<>();

	IDPresetCollection(FileAccessAdapter fileAccess)
	{
		this.fileAccess = fileAccess;
		allPresets = loadPresets();
		// already load localization for default locale
		getOrLoadLocalizedPresets(Locale.getDefault());
	}

	@Override public Collection<Preset> getAll(Locale locale)
	{
		if(locale == null) return Collections.unmodifiableCollection(allPresets.values());
		return Collections.unmodifiableCollection(getOrLoadLocalizedPresetsWithFallbackToDefault(locale).values());
	}

	@Override public Preset get(String id, Locale locale)
	{
		if(locale == null) return allPresets.get(id);
		return getOrLoadLocalizedPresetsWithFallbackToDefault(locale).get(id);
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
			String name = p.getString("name");
			List<String> terms = parseCommaSeparatedList(p.optString("terms"), name);
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
		if(array == null) return Collections.emptyList();
		List<T> result = new ArrayList<>(array.length());
		for (int i = 0; i < array.length(); i++)
		{
			T item = t.apply(array.get(i));
			if(item != null) result.add(item);
		}
		return Collections.unmodifiableList(result);
	}

	private static List<String> parseCommaSeparatedList(String str, String remove)
	{
		if(str == null || str.isEmpty()) return Collections.emptyList();
		String[] array = str.split("\\s*,\\s*");
		List<String> result = new ArrayList<>(array.length);
		Collections.addAll(result, array);
		result.remove(remove);
		return Collections.unmodifiableList(result);
	}

	private static Map<String, String> parseStringMap(JSONObject map)
	{
		if(map == null) return Collections.emptyMap();
		Set<String> keys = map.keySet();
		Map<String, String> result = new HashMap<>(keys.size());
		for (String key : keys)
		{
			result.put(key.intern(), map.getString(key));
		}
		return Collections.unmodifiableMap(result);
	}

	private Map<String, Preset> getOrLoadLocalizedPresetsWithFallbackToDefault(Locale locale)
	{
		Map<String, Preset> result = getOrLoadLocalizedPresets(locale);
		if(result == null)
		{
			Locale localeLanguage = new Locale(locale.getLanguage());
			result = getOrLoadLocalizedPresets(localeLanguage);
		}
		if(result == null) result = allPresets;
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
					localizedPresets.put(localeLanguage, mergePresets(
							loadLocalizedPresets(localeLanguage),
							allPresets
					));
				}
				// merging language presets with country presets (country presets overwrite language presets)
				if(!locale.getCountry().isEmpty())
				{
					Map<String, Preset> basePresets = localizedPresets.get(localeLanguage);
					if(basePresets == null) basePresets = allPresets;

					localizedPresets.put(locale, mergePresets(
						loadLocalizedPresets(locale),
						basePresets
					));
				}
			}
			return localizedPresets.get(locale);
		}
	}

	private static Map<String, Preset> mergePresets(Map<String, Preset> presets, Map<String, Preset> basePresets)
	{
		if(presets != null && basePresets != null)
		{
			for (String id : basePresets.keySet())
			{
				if (!presets.containsKey(id)) presets.put(id, basePresets.get(id));
			}
		}
		return presets;
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
		Map<String, Preset> result = new HashMap<>(allPresets.size());
		JSONObject presetsObject = object.getJSONObject("presets");
		for (String id : presetsObject.keySet())
		{
			id = id.intern();
			Preset basePreset = allPresets.get(id);
			if(basePreset == null) continue;

			JSONObject localization = presetsObject.getJSONObject(id);
			String name = localization.optString("name");
			if(name == null || name.isEmpty()) continue;
			List<String> terms = parseCommaSeparatedList(localization.optString("terms"), name);
			result.put(id, new Preset(basePreset, name, terms));
		}
		return result;
	}
}
