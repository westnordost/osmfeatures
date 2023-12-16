package de.westnordost.osmfeatures;

import okio.FileHandle;
import okio.FileSystem;
import okio.Path;
import okio.Source;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.*;

public class IDLocalizedFeatureCollectionTest
{
	@Test public void features_not_found_produces_runtime_exception()
	{
		try
		{
			new IDLocalizedFeatureCollection(new FileAccessAdapter()
			{
				@Override public boolean exists(String name) { return false; }
				@Override public Source open(String name) throws IOException { throw new FileNotFoundException(); }
			});
			fail();
		} catch (RuntimeException ignored) { }
	}

	@Test public void load_features_and_two_localizations()
	{
		IDLocalizedFeatureCollection c = new IDLocalizedFeatureCollection(new FileAccessAdapter()
		{
			@Override public boolean exists(String name)
			{
				return Arrays.asList("presets.json", "en.json", "de.json").contains(name);
			}

			@Override public Source open(String name) throws IOException
			{
				if (name.equals("presets.json")) return getSource("some_presets_min.json");
				if (name.equals("en.json")) return getSource("localizations_en.json");
				if (name.equals("de.json")) return getSource("localizations_de.json");
				throw new IOException("File not found");
			}
		});

		// getting non-localized features
		List<Locale> notLocalized = listOf((Locale) null);
		Collection<Feature> notLocalizedFeatures = c.getAll(notLocalized);
		assertEqualsIgnoreOrder(listOf("test", "test", "test"), getNames(notLocalizedFeatures));

		assertEquals("test", c.get("some/id", notLocalized).getName());
		assertEquals("test", c.get("another/id", notLocalized).getName());
		assertEquals("test", c.get("yet/another/id", notLocalized).getName());

		// getting English features
		List<Locale> english = listOf(Locale.ENGLISH);
		Collection<Feature> englishFeatures = c.getAll(english);
		assertEqualsIgnoreOrder(listOf("Bakery"), getNames(englishFeatures));

		assertEquals("Bakery", c.get("some/id", english).getName());
		assertNull(c.get("another/id", english));
		assertNull(c.get("yet/another/id", english));

		// getting Germany features
		// this also tests if the fallback from de-DE to de works if de-DE.json does not exist
		List<Locale> germany = listOf(Locale.GERMANY);
		Collection<Feature> germanyFeatures = c.getAll(germany);
		assertEqualsIgnoreOrder(listOf("Bäckerei", "Gullideckel"), getNames(germanyFeatures));

		assertEquals("Bäckerei", c.get("some/id", germany).getName());
		assertEquals("Gullideckel", c.get("another/id", germany).getName());
		assertNull(c.get("yet/another/id", germany));

		// getting features through fallback chain
		List<Locale> locales = listOf(Locale.ENGLISH, Locale.GERMANY, null);
		Collection<Feature> fallbackFeatures = c.getAll(locales);
		assertEqualsIgnoreOrder(listOf("Bakery", "Gullideckel", "test"), getNames(fallbackFeatures));

		assertEquals("Bakery", c.get("some/id", locales).getName());
		assertEquals("Gullideckel", c.get("another/id", locales).getName());
		assertEquals("test", c.get("yet/another/id", locales).getName());

		assertEquals(Locale.ENGLISH, c.get("some/id", locales).getLocale());
		assertEquals(Locale.GERMAN, c.get("another/id", locales).getLocale());
        assertNull(c.get("yet/another/id", locales).getLocale());
	}

	@Test public void load_features_and_merge_localizations()
	{
		IDLocalizedFeatureCollection c = new IDLocalizedFeatureCollection(new FileAccessAdapter()
		{
			@Override public boolean exists(String name)
			{
				return Arrays.asList("presets.json", "de-AT.json", "de.json", "de-Cyrl.json", "de-Cyrl-AT.json").contains(name);
			}

			@Override public Source open(String name) throws IOException
			{
				if (name.equals("presets.json")) return getSource("some_presets_min.json");
				if (name.equals("de-AT.json")) return getSource("localizations_de-AT.json");
				if (name.equals("de.json")) return getSource("localizations_de.json");
				if (name.equals("de-Cyrl-AT.json")) return getSource("localizations_de-Cyrl-AT.json");
				if (name.equals("de-Cyrl.json")) return getSource("localizations_de-Cyrl.json");
				throw new IOException("File not found");
			}
		});

		// standard case - no merging
		List<Locale> german = listOf(Locale.GERMAN);
		Collection<Feature> germanFeatures = c.getAll(german);
		assertEqualsIgnoreOrder(listOf("Bäckerei", "Gullideckel"), getNames(germanFeatures));

		assertEquals("Bäckerei", c.get("some/id", german).getName());
		assertEquals("Gullideckel", c.get("another/id", german).getName());
		assertNull(c.get("yet/another/id", german));

		// merging de-AT and de
		List<Locale> austria = listOf(new Locale("de", "AT"));
		Collection<Feature> austrianFeatures = c.getAll(austria);
		assertEqualsIgnoreOrder(listOf("Backhusl", "Gullideckel", "Brückle"), getNames(austrianFeatures));

		assertEquals("Backhusl", c.get("some/id", austria).getName());
		assertEquals("Gullideckel", c.get("another/id", austria).getName());
		assertEquals("Brückle", c.get("yet/another/id", austria).getName());

		// merging scripts
		List<Locale> cryllic = listOf(new Locale.Builder().setLanguage("de").setScript("Cyrl").build());
		assertEquals("бацкхаус", c.get("some/id", cryllic).getName());

		List<Locale> cryllicAustria = listOf(new Locale.Builder().setLanguage("de").setRegion("AT").setScript("Cyrl").build());
		assertEquals("бацкхусл", c.get("some/id", cryllicAustria).getName());
	}

	private Source getSource(String file) throws IOException {
		return FileSystem.SYSTEM.source(Path.get(getClass().getClassLoader().getResource(file).getFile()));
	}

	private static Collection<String> getNames(Collection<Feature> features)
	{
		List<String> result = new ArrayList<>();
		for (Feature feature : features) {
			result.add(feature.getName());
		}
		return result;
	}
}
