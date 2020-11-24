package de.westnordost.osmfeatures;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class iDFeatureCollectionTest
{
	@Test public void features_not_found_produces_runtime_exception()
	{
		try
		{
			new iDFeatureCollection(new iDFeatureCollection.FileAccessAdapter()
			{
				@Override public boolean exists(String name) { return false; }
				@Override public InputStream open(String name) throws IOException { throw new FileNotFoundException(); }
			});
			fail();
		} catch (RuntimeException ignored) { }
	}

	@Test public void load_features_only()
	{
		iDFeatureCollection features = create("one_preset_full.json", null);

		assertEquals(1, features.getAll(null).size());
		Feature feature = features.get("some/id", null);
		assertEquals("some/id", feature.id);
		assertEquals(MapEntry.mapOf(MapEntry.tag("a","b"), MapEntry.tag("c","d")), feature.tags);
		assertEquals(listOf(GeometryType.POINT, GeometryType.VERTEX, GeometryType.LINE, GeometryType.AREA, GeometryType.RELATION), feature.geometry);

		assertEquals(listOf("DE", "GB"), feature.countryCodes);
		assertEquals("foo", feature.name);
		assertTrue(feature.suggestion);
		assertEquals(listOf("1","2"), feature.terms);
		assertEquals(0.5f, feature.matchScore, 0.001f);
		assertFalse(feature.searchable);
		assertEquals(MapEntry.mapOf(MapEntry.tag("e","f")), feature.addTags);
	}

	@Test public void load_features_only_defaults()
	{
		iDFeatureCollection features = create("one_preset_min.json", null);

		assertEquals(1, features.getAll(null).size());
		Feature feature = features.get("some/id", null);

		assertEquals("some/id", feature.id);
		assertEquals(MapEntry.mapOf(MapEntry.tag("a","b"), MapEntry.tag("c","d")), feature.tags);
		assertEquals(listOf(GeometryType.POINT), feature.geometry);

		assertTrue(feature.countryCodes.isEmpty());
		assertEquals("test",feature.name);
		assertFalse(feature.suggestion);
		assertTrue(feature.terms.isEmpty());
		assertEquals(1.0f, feature.matchScore, 0.001f);
		assertTrue(feature.searchable);
		assertTrue(feature.addTags.isEmpty());
	}

	@Test public void load_features_no_wildcards()
	{
		iDFeatureCollection features = create("one_preset_wildcard.json", null);
		assertTrue(features.getAll(null).isEmpty());
	}

	@Test public void load_features_and_localization()
	{
		iDFeatureCollection features = create("one_preset_min.json", "localizations.json");

		assertEquals(1, features.getAll(Locale.US).size());
		Feature feature = features.get("some/id", Locale.US);

		assertEquals("some/id", feature.id);
		assertEquals(MapEntry.mapOf(MapEntry.tag("a","b"), MapEntry.tag("c","d")), feature.tags);
		assertEquals(listOf(GeometryType.POINT), feature.geometry);
		assertEquals("bar", feature.name);
		assertEquals(listOf("a", "b"), feature.terms);
	}

	@Test public void load_features_and_localization_defaults()
	{
		iDFeatureCollection features = create("one_preset_min.json", "localizations_min.json");

		assertEquals(1, features.getAll(Locale.US).size());
		Feature feature = features.get("some/id", Locale.US);

		assertEquals("some/id", feature.id);
		assertEquals(MapEntry.mapOf(MapEntry.tag("a","b"), MapEntry.tag("c","d")), feature.tags);
		assertEquals(listOf(GeometryType.POINT), feature.geometry);
		assertEquals("bar", feature.name);
		assertTrue(feature.terms.isEmpty());
	}

	@Test public void load_features_and_two_localizations()
	{
		iDFeatureCollection features = new iDFeatureCollection(new iDFeatureCollection.FileAccessAdapter()
		{
			@Override public boolean exists(String name)
			{
				return Arrays.asList("presets.json", "en.json", "de.json").contains(name);
			}

			@Override public InputStream open(String name) throws IOException
			{
				if (name.equals("presets.json")) return getStream("some_presets_min.json");
				if (name.equals("en.json")) return getStream("localizations_en.json");
				if (name.equals("de.json")) return getStream("localizations_de.json");
				throw new IOException("File not found");
			}
		});

		assertEquals(3, features.getAll(Locale.ENGLISH).size());
		assertEquals("Bakery", features.get("some/id", Locale.ENGLISH).name);
		assertEquals("test", features.get("another/id", Locale.ENGLISH).name);
		assertEquals("test", features.get("yet/another/id", Locale.ENGLISH).name);

		// this also tests if the fallback from de-DE to de works if de-DE.json does not exist
		assertEquals(3, features.getAll(Locale.GERMANY).size());
		assertEquals("Bäckerei", features.get("some/id", Locale.GERMANY).name);
		assertEquals("Gullideckel", features.get("another/id", Locale.GERMANY).name);
		assertEquals("test", features.get("yet/another/id", Locale.GERMANY).name);

	}

	@Test public void load_features_and_merge_localizations()
	{
		iDFeatureCollection features = new iDFeatureCollection(new iDFeatureCollection.FileAccessAdapter()
		{
			@Override public boolean exists(String name)
			{
				return Arrays.asList("presets.json", "de-AT.json", "de.json").contains(name);
			}

			@Override public InputStream open(String name) throws IOException
			{
				if (name.equals("presets.json")) return getStream("some_presets_min.json");
				if (name.equals("de-AT.json")) return getStream("localizations_de-AT.json");
				if (name.equals("de.json")) return getStream("localizations_de.json");
				throw new IOException("File not found");
			}
		});

		// this also tests if the fallback from de-DE to de works if de-DE.json does not exist
		assertEquals(3, features.getAll(Locale.GERMAN).size());
		assertEquals("Bäckerei", features.get("some/id", Locale.GERMAN).name);
		assertEquals("Gullideckel", features.get("another/id", Locale.GERMAN).name);
		assertEquals("test", features.get("yet/another/id", Locale.GERMAN).name);

		Locale AUSTRIA = new Locale("de", "AT");
		assertEquals(3, features.getAll(new Locale("de", "AT")).size());
		assertEquals("Backhusl", features.get("some/id", AUSTRIA).name);
		assertEquals("Gullideckel", features.get("another/id", AUSTRIA).name);
		assertEquals("Brückle", features.get("yet/another/id", AUSTRIA).name);
	}

	@Test public void parse_some_real_data()
	{
		FeatureCollection featureCollection = new iDFeatureCollection(new iDFeatureCollection.FileAccessAdapter()
		{
			@Override public boolean exists(String name) { return name.equals("presets.json") || name.equals("de.json"); }
			@Override public InputStream open(String name) throws IOException
			{
				if(name.equals("presets.json"))
				{
					return new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.min.json").openStream();
				} else {
					URL url = new URL("https://raw.githubusercontent.com/openstreetmap/iD/develop/dist/locales/"+name);
					String localeString = name.split("\\.")[0];
					try(InputStream is = url.openStream())
					{
						JSONObject localizationJson = new JSONObject(new JSONTokener(is));
						JSONObject featuresJson = localizationJson.getJSONObject(localeString).getJSONObject("presets").getJSONObject("presets");
						String localizationJsonString = "{\"presets\": " + featuresJson.toString() +"}";
						return new ByteArrayInputStream(localizationJsonString.getBytes(StandardCharsets.UTF_8));
					}
				}
			}
		});
		// should not crash etc
		assertTrue(featureCollection.getAll(Locale.GERMANY).size() > 1000);
	}

	private iDFeatureCollection create(String featureFile, String localizationsFile)
	{
		return new iDFeatureCollection(new iDFeatureCollection.FileAccessAdapter()
		{
			@Override public boolean exists(String name)
			{
				return name.equals("presets.json") || localizationsFile != null;
			}
			@Override public InputStream open(String name) throws IOException
			{
				if(name.equals("presets.json")) return getStream(featureFile);
				if(localizationsFile != null) return getStream(localizationsFile);
				throw new FileNotFoundException();
			}
		});
	}

	@SafeVarargs private static <T> List<T> listOf(T... items) { return Arrays.asList(items); }

	private InputStream getStream(String file)
	{
		return getClass().getClassLoader().getResourceAsStream(file);
	}
}
