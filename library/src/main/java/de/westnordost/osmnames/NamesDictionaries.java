package de.westnordost.osmnames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NamesDictionaries
{
	private final NamesParser namesParser;
	private final BrandNamesParser brandNamesParser;
	private final FileAccessAdapter files;

	private final Map<Locale, List<Entry>> names = new HashMap<>();
	private Map<String, List<Entry>> brandNames;

	public NamesDictionaries(NamesParser namesParser, BrandNamesParser brandNamesParser, FileAccessAdapter files)
	{
		this.namesParser = namesParser;
		this.brandNamesParser = brandNamesParser;
		this.files = files;
	}

	public static NamesDictionaries create(String path)
	{
		return new NamesDictionaries(new NamesParser(), new BrandNamesParser(), new FileSystemAccess(new File(path)));
	}

	public NamesDictionary get(Locale locale) throws IOException
	{
		return new NamesDictionary(getNamesWithFallbackToLanguageOnly(locale));
	}

	private List<Entry> getBrandNames(String countryCode) throws IOException
	{
		if(brandNames == null) brandNames = loadBrandNames();

		List<Entry> result = new ArrayList<>();
		List<Entry> anyBrandNames = brandNames.get(null);
		if(anyBrandNames != null) result.addAll(anyBrandNames);
		if(countryCode != null)
		{
			List<Entry> countryBrandNames = brandNames.get(countryCode.toUpperCase(Locale.US));
			if(countryBrandNames != null) result.addAll(countryBrandNames);
		}
		return result;
	}

	private Map<String, List<Entry>> loadBrandNames() throws IOException
	{
		try(InputStream is = files.open("name-suggestions.json"))
		{
			return brandNamesParser.parse(is);
		}
	}

	private List<Entry> getNamesWithFallbackToLanguageOnly(Locale locale) throws IOException
	{
		List<Entry> entries = getNames(locale);
		if(entries == null && locale.getCountry().length() > 0)
		{
			Locale languageOnly = new Locale(locale.getLanguage());
			return getNames(languageOnly);
		}
		return entries;
	}

	private List<Entry> getNames(Locale locale) throws IOException
	{
		if(!names.containsKey(locale))
		{
			names.put(locale, loadNames(locale));
		}
		return names.get(locale);
	}

	private List<Entry> loadNames(Locale locale) throws IOException
	{
		String lang = locale.getLanguage();
		String country = locale.getCountry();
		String filename = lang + (country.length() > 0 ? "-" + country : "") + ".json";
		if(!files.exists(filename)) return null;
		try (InputStream is = files.open(filename))
		{
			return namesParser.parse(is);
		}
	}
}
