package de.westnordost.osmfeatures;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.westnordost.osmfeatures.MapEntry.*;
import static org.junit.Assert.*;

public class FeatureDictionaryTest
{
	private static final List<GeometryType> POINT = Collections.singletonList(GeometryType.POINT);

	private final Feature bakery = feature(
			"shop/bakery",
			mapOf(tag("shop","bakery")),
			"Bäckerei",
			listOf("Brot")
	);
	private final Feature ditsch = brandFeature(
			"shop/bakery/Ditsch",
			mapOf(tag("shop","bakery"), tag("name","Ditsch")),
			"Ditsch",
			listOf("DE"),
			mapOf(tag("wikipedia","de:Brezelb%C3%A4ckerei_Ditsch"))
	);

	private final Feature car_dealer = feature(
			"shop/car",
			mapOf(tag("shop","car")),
			"Autohändler",
			listOf("auto")
	);
	private final Feature second_hand_car_dealer = feature(
			"shop/car/second_hand",
			mapOf(tag("shop","car"), tag("second_hand", "only")),
			"Gebrauchtwagenhändler",
			listOf("auto")
	);

	private final Feature scheisshaus = new Feature(
			"amenity/scheißhaus",
			mapOf(tag("amenity","scheißhaus")),
			POINT,
			"Scheißhaus",
			listOf(),
			listOf(),
			false, // <- not searchable!
			0f, false, Collections.emptyMap());

	private final Feature bank = feature(
			"amenity/bank",
			mapOf(tag("amenity","bank")),
			"Bank",
			listOf()
	);
	private final Feature bench = feature(
			"amenity/bench",
			mapOf(tag("amenity","bench")),
			"Parkbank",
			listOf("Bank"),
			5.0f
	);
	private final Feature casino = feature(
			"amenity/casino",
			mapOf(tag("amenity","casino")),
			"Spielbank",
			listOf("Kasino")
	);
	private final Feature atm = feature(
			"amenity/atm",
			mapOf(tag("amenity","atm")),
			"Bankomat",
			listOf()
	);
	private final Feature stock_exchange = feature(
			"amenity/stock_exchange",
			mapOf(tag("amenity","stock_exchange")),
			"Börse",
			listOf("Banking")
	);
	private final Feature bank_of_america = brandFeature(
			"amenity/bank/Bank of America",
			mapOf(tag("amenity","bank"), tag("name","Bank of America")),
			"Bank of America",
			listOf(),
			mapOf()
	);
	private final Feature bank_of_liechtenstein = brandFeature(
			"amenity/bank/Bank of Liechtenstein",
			mapOf(tag("amenity","bank"), tag("name","Bank of Liechtenstein")),
			"Bank of Liechtenstein",
			listOf(),
			mapOf(),
			0.2f
	);
	private final Feature deutsche_bank = brandFeature(
			"amenity/bank/Deutsche Bank",
			mapOf(tag("amenity","bank"), tag("name","Deutsche Bank")),
			"Deutsche Bank",
			listOf(),
			mapOf()
	);
	private final Feature baenk = feature(
			"amenity/bänk",
			mapOf(tag("amenity","bänk")),
			"Bänk",
			listOf()
	);
	private final Feature bad_bank = feature(
			"amenity/bank/bad",
			mapOf(tag("amenity","bank"), tag("goodity","bad")),
			"Bad Bank",
			listOf()
	);

	@Test public void find_no_entry_by_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "supermarket"));
		FeatureDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.byTags(tags).find());
	}

	@Test public void find_no_entry_because_wrong_geometry()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.byTags(tags).forGeometry(GeometryType.RELATION).find());
	}

	@Test public void find_entry_by_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> matches = dictionary.byTags(tags).find();
		assertEquals(1, matches.size());
		assertEquals(bakery.name, matches.get(0).name);
		assertEquals(bakery.tags, matches.get(0).tags);
		assertNull(matches.get(0).parentName);
	}

	@Test public void find_non_searchable_entry_by_tags()
	{
		Map<String,String> tags = mapOf(tag("amenity", "scheißhaus"));
		FeatureDictionary dictionary = dictionary(scheisshaus);
		List<Match> matches = dictionary.byTags(tags).find();
		assertEquals(1, matches.size());
	}

	@Test public void find_entry_by_tags_correct_geometry()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> matches = dictionary.byTags(tags).forGeometry(GeometryType.POINT).find();
		assertEquals(1, matches.size());
		assertEquals(bakery.name, matches.get(0).name);
		assertEquals(bakery.tags, matches.get(0).tags);
		assertNull(matches.get(0).parentName);
	}

	@Test public void find_brand_entry_by_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"));
		FeatureDictionary dictionary = dictionary(bakery, ditsch);
		List<Match> matches = dictionary.byTags(tags).find();
		assertEquals(1, matches.size());
		assertEquals(ditsch.name, matches.get(0).name);
		Map<String,String> expectedTags = new HashMap<>(ditsch.tags);
		expectedTags.putAll(ditsch.addTags);
		assertEquals(expectedTags, matches.get(0).tags);
		assertEquals("Bäckerei", matches.get(0).parentName);
	}

	@Test public void find_only_brands_finds_no_normal_entries()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> matches = dictionary.byTags(tags).isSuggestion(true).find();
		assertEquals(0, matches.size());
	}

	@Test public void find_no_brands_finds_only_normal_entries()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> matches = dictionary.byTags(tags).isSuggestion(false).find();
		assertEquals(1, matches.size());
		assertEquals(bakery.name, matches.get(0).name);
		assertEquals(bakery.tags, matches.get(0).tags);
		assertNull(matches.get(0).parentName);
	}

	@Test public void find_multiple_entries_by_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("amenity", "bank"));
		FeatureDictionary dictionary = dictionary(bakery, bank);
		List<Match> matches = dictionary.byTags(tags).find();
		assertEquals(2, matches.size());
	}

	@Test public void do_not_find_entry_with_too_specific_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "car"));
		FeatureDictionary dictionary = dictionary(car_dealer, second_hand_car_dealer);
		List<Match> matches = dictionary.byTags(tags).find();
		assertEquals(1, matches.size());
		assertEquals(car_dealer.name, matches.get(0).name);
	}

	@Test public void find_entry_with_specific_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "car"), tag("second_hand", "only"));
		FeatureDictionary dictionary = dictionary(car_dealer, second_hand_car_dealer);
		List<Match> matches = dictionary.byTags(tags).find();
		assertEquals(1, matches.size());
		assertEquals(second_hand_car_dealer.name, matches.get(0).name);
	}

	@Test public void find_no_entry_by_name()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.byTerm("Supermarkt").find());
	}

	@Test public void find_no_entry_by_name_because_wrong_geometry()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.byTerm("Bäckerei").forGeometry(GeometryType.LINE).find());
	}

	@Test public void find_no_entry_by_name_because_wrong_country()
	{
		FeatureDictionary dictionary = dictionary(ditsch);
		assertEquals(listOf(), dictionary.byTerm("Ditsch").find());
		assertEquals(listOf(), dictionary.byTerm("Ditsch").inCountry("AT").find());
	}

	@Test public void find_no_non_searchable_entry_by_name()
	{
		FeatureDictionary dictionary = dictionary(scheisshaus);
		assertEquals(listOf(), dictionary.byTerm("Scheißhaus").find());
	}

	@Test public void find_entry_by_name()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.byTerm("Bäckerei").find();
		assertEquals(1, findings.size());
		assertEquals(bakery.name, findings.get(0).name);
	}

	@Test public void find_entry_by_name_with_correct_geometry()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.byTerm("Bäckerei").forGeometry(GeometryType.POINT).find();
		assertEquals(1, findings.size());
		assertEquals(bakery.name, findings.get(0).name);
	}

	@Test public void find_entry_by_name_with_correct_country()
	{
		FeatureDictionary dictionary = dictionary(ditsch, bakery);
		List<Match> findings = dictionary.byTerm("Ditsch").inCountry("DE").find();
		assertEquals(1, findings.size());
		assertEquals(ditsch.name, findings.get(0).name);
		assertEquals(bakery.name, findings.get(0).parentName);
	}

	@Test public void find_entry_by_name_case_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.byTerm("BÄCkErEI").find();
		assertEquals(1, findings.size());
		assertEquals(bakery.name, findings.get(0).name);
	}

	@Test public void find_entry_by_name_diacritics_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.byTerm("Backérèi").find();
		assertEquals(1, findings.size());
		assertEquals(bakery.name, findings.get(0).name);
	}

	@Test public void find_entry_by_term()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.byTerm("bro").find();
		assertEquals(1, findings.size());
		assertEquals(bakery.name, findings.get(0).name);
	}

	@Test public void find_entry_by_term_case_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.byTerm("BRO").find();
		assertEquals(1, findings.size());
		assertEquals(bakery.name, findings.get(0).name);
	}

	@Test public void find_entry_by_term_diacritics_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Match> findings = dictionary.byTerm("bró").find();
		assertEquals(1, findings.size());
		assertEquals(bakery.name, findings.get(0).name);
	}

	@Test public void find_multiple_entries_by_name()
	{
		FeatureDictionary dictionary = dictionary(second_hand_car_dealer, car_dealer);
		List<Match> matches = dictionary.byTerm("auto").find();
		assertEquals(2, matches.size());
	}

	@Test public void find_multiple_entries_by_name_but_respect_limit()
	{
		FeatureDictionary dictionary = dictionary(second_hand_car_dealer, car_dealer);
		List<Match> matches = dictionary.byTerm("auto").limit(1).find();
		assertEquals(1, matches.size());
	}

	@Test public void find_by_term_sorts_result_in_correct_order()
	{
		FeatureDictionary dictionary = dictionary(
				casino, baenk, bad_bank, stock_exchange, bank_of_liechtenstein, bank, bench, atm,
				bank_of_america, deutsche_bank);
		List<Match> findings = dictionary.byTerm("Bank").find();
		assertEquals(8, findings.size());
		assertEquals(bank.name, findings.get(0).name); // exact name matches
		assertEquals(baenk.name, findings.get(1).name); // exact name matches (diacritics and case insensitive)
		assertEquals(atm.name, findings.get(2).name); // starts-with name matches
		assertEquals(bad_bank.name, findings.get(3).name); // starts-with-word name matches
		assertEquals(bank_of_america.name, findings.get(4).name); // starts-with brand name matches
		assertEquals(bank_of_liechtenstein.name, findings.get(5).name); // starts-with brand name matches - lower matchScore
		assertEquals(bench.name, findings.get(6).name); // found word in terms - higher matchScore
		assertEquals(stock_exchange.name, findings.get(7).name); // found word in terms - lower matchScore

		//assertEquals(casino.name, findings.get(X).name); // not included: "Spielbank" does not start with "bank"
		//assertEquals(deutsche_bank.name, findings.get(X).name); // not included: "Deutsche Bank" does not start with "bank"
	}

	private static FeatureDictionary dictionary(Feature... entries)
	{
		return new FeatureDictionary(new TestFeatureCollection(entries));
	}

	private static Feature brandFeature(String id, Map<String, String> tags, String name,
										List<String> countryCodes, Map<String, String> addTags, float matchScore)
	{
		return new Feature(id, tags, POINT, name, Collections.emptyList(), countryCodes, true, matchScore,
				true, addTags);
	}

	private static Feature brandFeature(String id, Map<String, String> tags, String name,
										List<String> countryCodes, Map<String, String> addTags)
	{
		return new Feature(id, tags, POINT, name, Collections.emptyList(), countryCodes, true, 1.0f,
				true, addTags);
	}

	private static Feature feature(String id, Map<String, String> tags, String name,
								   List<String> terms, float matchScore)
	{
		return new Feature(id, tags, POINT, name, terms,
				Collections.emptyList(), true, matchScore, false, Collections.emptyMap()
		);
	}

	private static Feature feature(String id, Map<String, String> tags, String name, List<String> terms)
	{
		return new Feature(id, tags, POINT, name, terms,
				Collections.emptyList(), true, 1.0f, false, Collections.emptyMap()
		);
	}

	@SafeVarargs private static <T> List<T> listOf(T... items) { return Arrays.asList(items); }
}
