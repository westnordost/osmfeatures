package de.westnordost.osmfeatures;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import static de.westnordost.osmfeatures.CollectionUtils.synchronizedGetOrCreate;

/** Localized feature collection sourcing from iD presets defined in JSON.
 *
 *  The base path is defined via the given FileAccessAdapter. In the base path, it is expected that
 *  there is a presets.json which includes all the features. The translations are expected to be
 *  located in the same directory named like e.g. de.json, pt-BR.json etc. */
class IDLocalizedFeatureCollection implements LocalizedFeatureCollection
{
	private static final String FEATURES_FILE = "presets.json";

	private final FileAccessAdapter fileAccess;

	private final LinkedHashMap<String, BaseFeature> featuresById;

	private final Map<Locale, List<LocalizedFeature>> localizedFeaturesList = new HashMap<>();

	private final Map<List<Locale>, LinkedHashMap<String, Feature>> localizedFeatures = new HashMap<>();

	IDLocalizedFeatureCollection(FileAccessAdapter fileAccess)
	{
		this.fileAccess = fileAccess;
		List<BaseFeature> features = loadFeatures();
		featuresById = new LinkedHashMap<>(features.size());
		for (BaseFeature feature : features) {
			this.featuresById.put(feature.getId(), feature);
		}
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


	@Override public Collection<Feature> getAll(List<Locale> locales)
	{
		return getOrLoadLocalizedFeatures(locales).values();
	}

	@Override public Feature get(String id, List<Locale> locales)
	{
		return getOrLoadLocalizedFeatures(locales).get(id);
	}

	private LinkedHashMap<String, Feature> getOrLoadLocalizedFeatures(List<Locale> locales)
	{
		return synchronizedGetOrCreate(localizedFeatures, locales, this::loadLocalizedFeatures);
	}

	private LinkedHashMap<String, Feature> loadLocalizedFeatures(List<Locale> locales)
	{
		LinkedHashMap<String, Feature> result = new LinkedHashMap<>(featuresById.size());
		ListIterator<Locale> it = locales.listIterator(locales.size());
		while (it.hasPrevious())
		{
			Locale locale = it.previous();
			if (locale != null)
			{
				for (Locale localeComponent : getLocaleComponents(locale))
				{
					putAllFeatures(result, getOrLoadLocalizedFeaturesList(localeComponent));
				}
			} else {
				putAllFeatures(result, featuresById.values());
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
		String filename = getLocalizationFilename(locale);
		try
		{
			if (!fileAccess.exists(filename)) return Collections.emptyList();
			try (InputStream is = fileAccess.open(filename))
			{
				return new IDPresetsTranslationJsonParser().parse(is, locale, featuresById);
			}
		}
		catch (IOException | JSONException e) { throw new RuntimeException(e); }
	}

	private static String getLocalizationFilename(Locale locale)
	{
		/* we only want language+country+script of the locale, not anything else. So we construct
		   it anew here */
		return new Locale.Builder()
				.setLanguage(locale.getLanguage())
				.setRegion(locale.getCountry())
				.setScript(locale.getScript())
				.build()
				.toLanguageTag() + ".json";
	}

	private static List<Locale> getLocaleComponents(Locale locale)
	{
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		String script = locale.getScript();
		List<Locale> result = new ArrayList<>(4);

		result.add(new Locale(lang));

		if (!country.isEmpty())
			result.add(new Locale.Builder().setLanguage(lang).setRegion(country).build());

		if (!script.isEmpty())
			result.add(new Locale.Builder().setLanguage(lang).setScript(script).build());

		if (!country.isEmpty() && !script.isEmpty())
			result.add(new Locale.Builder().setLanguage(lang).setRegion(country).setScript(script).build());

		return result;
	}

	private static void putAllFeatures(Map<String, Feature> map, Iterable<? extends Feature> features)
	{
		for (Feature feature : features)
		{
			map.put(feature.getId(), feature);
		}
	}
}
