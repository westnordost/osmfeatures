package de.westnordost.osmnames;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.westnordost.osmnames.MapEntry.mapOf;
import static de.westnordost.osmnames.MapEntry.tag;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class NamesDictionariesTest
{
	@Test public void parse_two_names()
	{
		List<Entry> names = loadNames("names.json");

		assertEquals(2, names.size());
		assertEquals(0, names.get(0).getCount());
		assertEquals(mapOf(tag("a","x"), tag("b","y")), names.get(0).getTags());
		assertEquals(listOf("A", "B"), names.get(0).getNames());
		assertEquals(listOf("1", "2"), names.get(0).getKeywords());
		assertEquals(0, names.get(1).getCount());
		assertEquals(mapOf(tag("c","z")), names.get(1).getTags());
		assertEquals(listOf("C"), names.get(1).getNames());
		assertEquals(listOf("3"), names.get(1).getKeywords());
	}

	@Test public void keywords_is_optional()
	{
		List<Entry> names = loadNames("names_keywords_is_optional.json");

		assertEquals(1, names.size());
		assertEquals(listOf(), names.get(0).getKeywords());
	}

	@Test public void parse_brand_names()
	{
		Map<String, List<Entry>> names = loadBrandNames("brand_names.json");

		assertEquals(1, names.size());
		assertTrue(names.containsKey(null));
		List<Entry> entries = names.get(null);
		assertEquals(4, entries.size());

		Collections.sort(entries, (a, b) -> a.getCount() - b.getCount());

		assertEquals(listOf("A"), entries.get(0).getNames());
		assertEquals(1, entries.get(0).getCount());
		assertEquals(mapOf(tag("a","1")), entries.get(0).getTags());

		assertEquals(listOf("B"), entries.get(1).getNames());
		assertEquals(2, entries.get(1).getCount());
		assertEquals(mapOf(tag("b","1")), entries.get(1).getTags());

		assertEquals(listOf("C"), entries.get(2).getNames());
		assertEquals(3, entries.get(2).getCount());
		assertEquals(mapOf(tag("c","1")), entries.get(2).getTags());

		assertEquals(listOf("D"), entries.get(3).getNames());
		assertEquals(4, entries.get(3).getCount());
		assertEquals(mapOf(tag("d","1")), entries.get(3).getTags());
	}

	@Test public void count_is_optional()
	{
		Map<String, List<Entry>> names = loadBrandNames("brand_names_count_is_optional.json");

		assertEquals(1, names.size());
		assertTrue(names.containsKey(null));
		List<Entry> entries = names.get(null);
		assertEquals(1, entries.size());

		assertEquals(0, entries.get(0).getCount());
	}

	@Test public void brand_names_are_sorted_by_country_codes()
	{
		Map<String, List<Entry>> names = loadBrandNames("brand_names_country_codes.json");

		assertEquals(3, names.size());
		assertTrue(names.containsKey(null));
		assertEquals(1, names.get(null).size());
		assertEquals(listOf("A"), names.get(null).get(0).getNames());
		assertTrue(names.containsKey("DE"));
		assertEquals(1, names.get("DE").size());
		assertEquals(listOf("B"), names.get("DE").get(0).getNames());
		assertTrue(names.containsKey("AT"));
		assertEquals(1, names.get("AT").size());
		assertEquals(listOf("B"), names.get("AT").get(0).getNames());
		assertSame(names.get("DE").get(0), names.get("AT").get(0));
	}

	private Map<String, List<Entry>> loadBrandNames(String fileName)
	{
		return NamesDictionaries.parseBrandNames(getClass().getClassLoader().getResourceAsStream(fileName));
	}

	private List<Entry> loadNames(String fileName)
	{
		return NamesDictionaries.parseNames(getClass().getClassLoader().getResourceAsStream(fileName));
	}

	@SafeVarargs private static <T> List<T> listOf(T... items) { return Arrays.asList(items); }
}
