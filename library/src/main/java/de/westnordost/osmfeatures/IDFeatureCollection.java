package de.westnordost.osmfeatures;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import static de.westnordost.osmfeatures.CollectionUtils.synchronizedGetOrCreate;

/** Feature collection sourcing from iD presets defined in JSON */
class IDFeatureCollection implements FeatureCollection
{
	private static final String FEATURES_FILE = "presets.json";

	public interface FileAccessAdapter
	{
		boolean exists(String name) throws IOException;
		InputStream open(String name) throws IOException;
	}

	private final FileAccessAdapter fileAccess;

	private final LinkedHashMap<String, BaseFeature> baseFeatures = new LinkedHashMap<>();
	private final LinkedHashMap<String, Feature> brandFeatures = new LinkedHashMap<>();

	private final Map<Locale, List<LocalizedFeature>> localizedFeaturesList = new HashMap<>();

	private final Map<List<Locale>, LinkedHashMap<String, Feature>> localizedFeatures = new HashMap<>();

	IDFeatureCollection(FileAccessAdapter fileAccess)
	{
		this.fileAccess = fileAccess;
		List<BaseFeature> features = loadFeatures();
		for (BaseFeature feature : features) {
			if (feature.isSuggestion())
				brandFeatures.put(feature.getId(), feature);
			else
				baseFeatures.put(feature.getId(), feature);
		}
	}

	@Override public Collection<Feature> getAllSuggestions()
	{
		return brandFeatures.values();
	}

	@Override public Collection<Feature> getAllLocalized(List<Locale> locale)
	{
		return getOrLoadLocalizedFeatures(locale).values();
	}

	@Override public Feature get(String id, List<Locale> locale)
	{
		Feature result = getOrLoadLocalizedFeatures(locale).get(id);
		return (result != null) ? result : brandFeatures.get(id);
	}

	private List<BaseFeature> loadFeatures()
	{
		try(InputStream is = fileAccess.open(FEATURES_FILE))
		{
			return new IDPresetsJsonParser().parse(is);
		}
		catch (IOException | JSONException e)
		{
			throw new RuntimeException(e);
		}
	}

	private LinkedHashMap<String, Feature> getOrLoadLocalizedFeatures(List<Locale> locales)
	{
		return synchronizedGetOrCreate(localizedFeatures, locales, this::loadLocalizedFeatures);
	}

	private LinkedHashMap<String, Feature> loadLocalizedFeatures(List<Locale> locales)
	{
		LinkedHashMap<String, Feature> result = new LinkedHashMap<>();
		ListIterator<Locale> it = locales.listIterator(locales.size());
		while(it.hasPrevious())
		{
			Locale locale = it.previous();
			if (locale != null)
			{
				Locale languageLocale = new Locale(locale.getLanguage());
				putAllFeatures(result, getOrLoadLocalizedFeaturesList(languageLocale));
				if (!locale.getCountry().isEmpty())
				{
					putAllFeatures(result, getOrLoadLocalizedFeaturesList(locale));
				}
			} else {
				putAllFeatures(result, baseFeatures.values());
			}
		}

		return result;
	}

	private List<LocalizedFeature> getOrLoadLocalizedFeaturesList(Locale locale)
	{
		return synchronizedGetOrCreate(localizedFeaturesList, locale, this::loadLocalizedFeaturesList);
	}

	private List<LocalizedFeature> loadLocalizedFeaturesList(Locale locale)
	{
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		String filename = lang + (country.length() > 0 ? "-" + country : "") + ".json";
		try
		{
			if (!fileAccess.exists(filename)) return Collections.emptyList();
			try (InputStream is = fileAccess.open(filename))
			{
				return new IDPresetsTranslationJsonParser().parse(is, locale, baseFeatures);
			}
		}
		catch (IOException | JSONException e) { throw new RuntimeException(e); }
	}

	private static void putAllFeatures(Map<String, Feature> map, Iterable<? extends Feature> features)
	{
		for (Feature feature : features)
		{
			map.put(feature.getId(), feature);
		}
	}
}
