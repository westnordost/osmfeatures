package de.westnordost.osmnames;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.westnordost.osmnames.MapEntry.*;
import static de.westnordost.osmnames.NamesDictionary.*;
import static org.junit.Assert.*;

public class NamesDictionaryTest
{
	private final Entry bakery = entry(
			listOf("Bäckerei", "Konditorei"),
			mapOf(tag("shop","bakery"))
	);
	private final Entry car_dealer = entry(
			listOf("Autohändler"),
			mapOf(tag("shop","car"))
	);
	private final Entry second_hand_car_dealer = entry(
			listOf("Gebrauchtwagenhändler"),
			mapOf(tag("shop","car"), tag("second_hand", "only"))
	);
	private final Entry bank = entry(
			listOf("Bank", "Kreditinstitut"),
			mapOf(tag("amenity","bank"))
	);
	private final Entry bench = entry(
			listOf("Parkbank", "Bank"),
			mapOf(tag("amenity","bench"))
	);
	private final Entry casino = entry(
			listOf("Spielbank"),
			mapOf(tag("amenity","casino"))
	);
	private final Entry atm = entry(
			listOf("Bankomat"),
			mapOf(tag("amenity","atm"))
	);
	private final Entry stock_exchange = entry(
			listOf("Börse"),
			mapOf(tag("amenity","stock_exchange")),
			listOf("Banking")
	);

	@Test public void find_no_entry_by_tags()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("shop", "supermarket");
		NamesDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.get(tags));
	}

	@Test public void find_entry_by_tags()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("shop", "bakery");
		NamesDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(bakery), dictionary.get(tags));
	}

	@Test public void do_not_find_entry_with_too_specific_tags()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("shop", "car");
		NamesDictionary dictionary = dictionary(car_dealer, second_hand_car_dealer);
		assertFalse(dictionary.get(tags).contains(second_hand_car_dealer));
	}

	@Test public void find_ordered_multiple_entries_by_tags()
	{
		Map<String,String> tags = new HashMap<>();
		tags.put("shop", "car");
		tags.put("second_hand", "only");
		NamesDictionary dictionary = dictionary(car_dealer, second_hand_car_dealer);
		assertEquals(
				listOf(second_hand_car_dealer, car_dealer),
				dictionary.get(tags));
	}

	@Test public void get_all_entries()
	{
		NamesDictionary dictionary = dictionary(bakery, bench);
		assertTrue(dictionary.getAll().containsAll(listOf(bakery, bench)));
	}

	@Test public void find_no_entry_by_name()
	{
		NamesDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.get("Supermarkt"));
	}

	@Test public void find_entry_by_term_anywhere()
	{
		NamesDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.find("dito", ANYWHERE);
		assertEquals(1, findings.size());
		assertEquals(bakery, findings.get(0).getEntry());
		assertEquals("Konditorei", findings.get(0).getWord());
		assertEquals(3, findings.get(0).getWordIndex());
	}

	@Test public void find_entry_by_term_at_start()
	{
		NamesDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.find("Bäck", STARTS_WITH);
		assertEquals(1, findings.size());
		assertEquals(bakery, findings.get(0).getEntry());
		assertEquals("Bäckerei", findings.get(0).getWord());
		assertEquals(0, findings.get(0).getWordIndex());
	}

	@Test public void do_not_find_entry_by_term_at_start()
	{
		NamesDictionary dictionary = dictionary(bakery);
		assertTrue(dictionary.find("äckerei", STARTS_WITH).isEmpty());
	}

	@Test public void find_entry_by_term_as_exact_match()
	{
		NamesDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.find("Bäckerei", EXACT_MATCH);
		assertEquals(1, findings.size());
		assertEquals(bakery, findings.get(0).getEntry());
		assertEquals("Bäckerei", findings.get(0).getWord());
		assertEquals(0, findings.get(0).getWordIndex());
	}

	@Test public void do_not_find_entry_by_term_as_exact_match()
	{
		NamesDictionary dictionary = dictionary(bakery);
		assertTrue(dictionary.find("äckerei", EXACT_MATCH).isEmpty());
		assertTrue(dictionary.find("Bäckere", EXACT_MATCH).isEmpty());
		assertTrue(dictionary.find("äcker", EXACT_MATCH).isEmpty());
	}

	@Test public void find_entry_diacritics_sensitive()
	{
		NamesDictionary dictionary = dictionary(bakery);
		assertTrue(dictionary.find("Backerei", 0).isEmpty());
		assertFalse(dictionary.find("Backerei", DIACRITICS_INSENSITIVE).isEmpty());
		assertFalse(dictionary.find("Báckèrei", DIACRITICS_INSENSITIVE).isEmpty());
	}

	@Test public void find_entry_case_sensitive()
	{
		NamesDictionary dictionary = dictionary(bakery);
		assertTrue(dictionary.find("bäckerei", 0).isEmpty());
		assertTrue(dictionary.find("bÄcKerEI", CASE_INSENSITIVE).isEmpty());
		assertFalse(dictionary.find("Bäckerei", CASE_INSENSITIVE).isEmpty());
	}

	@Test public void find_by_term_sorts_result_in_correct_order()
	{
		NamesDictionary dictionary = dictionary(casino, stock_exchange, bank, bench, atm);
		List<Match> findings = dictionary.find("bank", ANYWHERE | CASE_INSENSITIVE | DIACRITICS_INSENSITIVE);
		assertEquals(5, findings.size());
		assertEquals(bank, findings.get(0).getEntry());
		assertEquals(bench, findings.get(1).getEntry());
		assertEquals(atm, findings.get(2).getEntry());
		assertEquals(casino, findings.get(3).getEntry());
		assertEquals(stock_exchange, findings.get(4).getEntry());
	}

	private static NamesDictionary dictionary(Entry... entries)
	{
		return new NamesDictionary(listOf(entries));
	}

	private static Entry entry(List<String> names, Map<String, String> tags, List<String> keywords)
	{
		return new Entry(names, tags, keywords, 0);
	}

	private static Entry entry(List<String> names, Map<String, String> tags)
	{
		return new Entry(names, tags, null, 0);
	}

	@SafeVarargs private static <T> List<T> listOf(T... items) { return Arrays.asList(items); }
}
