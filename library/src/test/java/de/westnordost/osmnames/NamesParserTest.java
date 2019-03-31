package de.westnordost.osmnames;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static de.westnordost.osmnames.MapEntry.*;
import static org.junit.Assert.*;

public class NamesParserTest
{
	@Test public void parse_two_names()
	{
		List<Entry> names = load("names.json");

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
		List<Entry> names = load("names_keywords_is_optional.json");

		assertEquals(1, names.size());
		assertEquals(listOf(), names.get(0).getKeywords());
	}

	private List<Entry> load(String fileName)
	{
		return new NamesParser().parse(getClass().getClassLoader().getResourceAsStream(fileName));
	}

	@SafeVarargs private static <T> List<T> listOf(T... items) { return Arrays.asList(items); }
}
