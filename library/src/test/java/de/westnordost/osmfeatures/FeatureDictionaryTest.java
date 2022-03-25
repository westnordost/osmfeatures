package de.westnordost.osmfeatures;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static de.westnordost.osmfeatures.MapEntry.*;
import static de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder;
import static de.westnordost.osmfeatures.TestUtils.listOf;
import static org.junit.Assert.*;

public class FeatureDictionaryTest
{
	private static final List<GeometryType> POINT = Collections.singletonList(GeometryType.POINT);

	private final Feature bakery = feature( // unlocalized shop=bakery
			"shop/bakery",
			mapOf(tag("shop","bakery")),
			"Bäckerei",
			listOf("Brot"),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);

	private final Feature panetteria = feature( // localized shop=bakery
			"shop/bakery",
			mapOf(tag("shop","bakery")),
			"Panetteria",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			Locale.ITALIAN
	);

	private final Feature ditsch = feature( // brand in DE for shop=bakery
			"shop/bakery/Ditsch",
			mapOf(tag("shop","bakery"), tag("name","Ditsch")),
			"Ditsch",
			listOf(),
			listOf("DE","AT"),
			listOf("AT-9"),
			true,
			1.0,
			mapOf(tag("wikipedia","de:Brezelb%C3%A4ckerei_Ditsch"), tag("brand", "Ditsch")),
			true,
			null
	);

	private final Feature ditschRussian = feature( // brand in RU for shop=bakery
			"shop/bakery/Дитсч",
			mapOf(tag("shop","bakery"), tag("name","Ditsch")),
			"Дитсч",
			listOf(),
			listOf("RU","UA-43"),
			listOf(),
			true,
			1.0,
			mapOf(tag("wikipedia","de:Brezelb%C3%A4ckerei_Ditsch"), tag("brand", "Дитсч")),
			true,
			null
	);

	private final Feature ditschInternational = feature( // brand everywhere for shop=bakery
			"shop/bakery/Ditsh",
			mapOf(tag("shop","bakery"), tag("name","Ditsch")),
			"Ditsh",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(tag("wikipedia","de:Brezelb%C3%A4ckerei_Ditsch")),
			true,
			null
	);

	private final Feature liquor_store = feature( // English localized unspecific shop=alcohol
			"shop/alcohol",
			mapOf(tag("shop","alcohol")),
			"Off licence (Alcohol shop)",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			true,
			Locale.UK
	);

	private final Feature car_dealer = feature( // German localized  unspecific shop=car
			"shop/car",
			mapOf(tag("shop","car")),
			"Autohändler",
			listOf("auto"),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			Locale.GERMAN
	);

	private final Feature second_hand_car_dealer = feature( // German localized shop=car with subtags
			"shop/car/second_hand",
			mapOf(tag("shop","car"), tag("second_hand", "only")),
			"Gebrauchtwagenhändler",
			listOf("auto"),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			Locale.GERMAN
	);

	private final Feature scheisshaus = feature( // unsearchable feature
			"amenity/scheißhaus",
			mapOf(tag("amenity","scheißhaus")),
			"Scheißhaus",
			listOf(),
			listOf(),
			listOf(),
			false, // <--- not searchable!
			1.0,
			mapOf(),
			false,
			null
	);

	private final Feature bank = feature( // unlocalized shop=bank (Bank)
			"amenity/bank",
			mapOf(tag("amenity","bank")),
			"Bank",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);
	private final Feature bench = feature( // unlocalized amenity=bench (PARKbank)
			"amenity/bench",
			mapOf(tag("amenity","bench")),
			"Parkbank",
			listOf("Bank"),
			listOf(),
			listOf(),
			true,
			5.0,
			mapOf(),
			false,
			null
	);
	private final Feature casino = feature( // unlocalized amenity=casino (SPIELbank)
			"amenity/casino",
			mapOf(tag("amenity","casino")),
			"Spielbank",
			listOf("Kasino"),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);
	private final Feature atm = feature( // unlocalized amenity=atm (BankOMAT)
			"amenity/atm",
			mapOf(tag("amenity","atm")),
			"Bankomat",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);
	private final Feature stock_exchange = feature( // unlocalized amenity=stock_exchange (has "Banking" as term)
			"amenity/stock_exchange",
			mapOf(tag("amenity","stock_exchange")),
			"Börse",
			listOf("Banking"),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);
	private final Feature bank_of_america = feature( // Brand of a amenity=bank (has "Bank" in name)
			"amenity/bank/Bank of America",
			mapOf(tag("amenity","bank"), tag("name","Bank of America")),
			"Bank of America",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			true,
			null
	);
	private final Feature bank_of_liechtenstein = feature( // Brand of a amenity=bank (has "Bank" in name), but low matchScore
			"amenity/bank/Bank of Liechtenstein",
			mapOf(tag("amenity","bank"), tag("name","Bank of Liechtenstein")),
			"Bank of Liechtenstein",
			listOf(),
			listOf(),
			listOf(),
			true,
			0.2,
			mapOf(),
			true,
			null
	);
	private final Feature deutsche_bank = feature( // Brand of a amenity=bank (does not start with "Bank" in name)
			"amenity/bank/Deutsche Bank",
			mapOf(tag("amenity","bank"), tag("name","Deutsche Bank")),
			"Deutsche Bank",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			true,
			null
	);
	private final Feature baenk = feature( // amenity=bänk, to see if diacritics match non-strictly ("a" finds "ä")
			"amenity/bänk",
			mapOf(tag("amenity","bänk")),
			"Bänk",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);
	private final Feature bad_bank = feature( // amenity=bank with subtags that has "Bank" in name but it is not hte first word
			"amenity/bank/bad",
			mapOf(tag("amenity","bank"), tag("goodity","bad")),
			"Bad Bank",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);
	private final Feature miniature_train_shop = feature( // feature whose name consists of several words
			"shop/miniature_train",
			mapOf(tag("shop","miniature_train")),
			"Miniature Train Shop",
			listOf(),
			listOf(),
			listOf(),
			true,
			1.0,
			mapOf(),
			false,
			null
	);

	//region by tags

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

	@Test public void find_no_entry_because_wrong_locale()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.byTags(tags).forLocale(Locale.ITALIAN).find());
	}

	@Test public void find_entry_because_fallback_locale()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTags(tags).forLocale(Locale.ITALIAN, null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_entry_by_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTags(tags).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_non_searchable_entry_by_tags()
	{
		Map<String,String> tags = mapOf(tag("amenity", "scheißhaus"));
		FeatureDictionary dictionary = dictionary(scheisshaus);
		List<Feature> matches = dictionary.byTags(tags).find();
		assertEquals(listOf(scheisshaus), matches);
	}

	@Test public void find_entry_by_tags_correct_geometry()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTags(tags).forGeometry(GeometryType.POINT).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_brand_entry_by_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"));
		FeatureDictionary dictionary = dictionary(bakery, ditsch);
		List<Feature> matches = dictionary.byTags(tags).inCountry("DE").find();
		assertEquals(listOf(ditsch), matches);
	}

	@Test public void find_only_entries_with_given_locale()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"));
		FeatureDictionary dictionary = dictionary(bakery, panetteria);
		assertEquals(listOf(panetteria), dictionary.byTags(tags).forLocale(Locale.ITALIAN).find());
		assertEquals(listOf(), dictionary.byTags(tags).forLocale(Locale.ENGLISH).find());
		assertEquals(listOf(bakery), dictionary.byTags(tags).forLocale((Locale) null).find());
	}

	@Test public void find_only_brands_finds_no_normal_entries()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"));
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTags(tags).isSuggestion(true).find();
		assertEquals(0, matches.size());
	}

	@Test public void find_no_brands_finds_only_normal_entries()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"));
		FeatureDictionary dictionary = dictionary(bakery, ditsch);
		List<Feature> matches = dictionary.byTags(tags).isSuggestion(false).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_multiple_brands_sorts_by_locale()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"));
		FeatureDictionary dictionary = dictionary(ditschRussian, ditschInternational, ditsch);
		List<Feature> matches = dictionary.byTags(tags).forLocale((Locale) null).find();
		assertEquals(ditschInternational, matches.get(0));
	}

	@Test public void find_multiple_entries_by_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "bakery"), tag("amenity", "bank"));
		FeatureDictionary dictionary = dictionary(bakery, bank);
		List<Feature> matches = dictionary.byTags(tags).find();
		assertEquals(2, matches.size());
	}

	@Test public void do_not_find_entry_with_too_specific_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "car"));
		FeatureDictionary dictionary = dictionary(car_dealer, second_hand_car_dealer);
		List<Feature> matches = dictionary.byTags(tags).forLocale(Locale.GERMAN, null).find();
		assertEquals(listOf(car_dealer), matches);
	}

	@Test public void find_entry_with_specific_tags()
	{
		Map<String,String> tags = mapOf(tag("shop", "car"), tag("second_hand", "only"));
		FeatureDictionary dictionary = dictionary(car_dealer, second_hand_car_dealer);
		List<Feature> matches = dictionary.byTags(tags).forLocale(Locale.GERMAN, null).find();
		assertEquals(listOf(second_hand_car_dealer), matches);
	}

	//endregion

	//region by name

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
		FeatureDictionary dictionary = dictionary(ditsch, ditschRussian);
		assertEquals(listOf(), dictionary.byTerm("Ditsch").find());
		assertEquals(listOf(), dictionary.byTerm("Ditsch").inCountry("FR").find()); // not in France
		assertEquals(listOf(), dictionary.byTerm("Ditsch").inCountry("AT-9").find()); // in all of AT but not Vienna
		assertEquals(listOf(), dictionary.byTerm("Дитсч").inCountry("UA").find()); // only on the Krim
	}

	@Test public void find_no_non_searchable_entry_by_name()
	{
		FeatureDictionary dictionary = dictionary(scheisshaus);
		assertEquals(listOf(), dictionary.byTerm("Scheißhaus").find());
	}

	@Test public void find_entry_by_name()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("Bäckerei").forLocale((Locale) null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_entry_by_name_with_correct_geometry()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("Bäckerei").forLocale((Locale) null).forGeometry(GeometryType.POINT).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_entry_by_name_with_correct_country()
	{
		FeatureDictionary dictionary = dictionary(ditsch, ditschRussian, bakery);
		assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("DE").find());
		assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("DE-TH").find());
		assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("AT").find());
		assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("AT-5").find());
		assertEquals(listOf(ditschRussian), dictionary.byTerm("Дитсч").inCountry("UA-43").find());
		assertEquals(listOf(ditschRussian), dictionary.byTerm("Дитсч").inCountry("RU-KHA").find());
	}

	@Test public void find_entry_by_name_case_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("BÄCkErEI").forLocale((Locale) null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_entry_by_name_diacritics_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("Backérèi").forLocale((Locale) null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_entry_by_term()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("bro").forLocale((Locale) null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_entry_by_term_brackets()
	{
		FeatureDictionary dictionary = dictionary(liquor_store);
		List<Feature> matches = dictionary.byTerm("Alcohol").forLocale(Locale.UK).find();
		assertEquals(listOf(liquor_store), matches);
	}

	@Test public void find_entry_by_term_case_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("BRO").forLocale((Locale) null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_entry_by_term_diacritics_insensitive()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("bró").forLocale((Locale) null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_multiple_entries_by_term()
	{
		FeatureDictionary dictionary = dictionary(second_hand_car_dealer, car_dealer);
		List<Feature> matches = dictionary.byTerm("auto").forLocale(Locale.GERMAN).find();
		assertEqualsIgnoreOrder(listOf(second_hand_car_dealer, car_dealer), matches);
	}

	@Test public void find_multiple_entries_by_name_but_respect_limit()
	{
		FeatureDictionary dictionary = dictionary(second_hand_car_dealer, car_dealer);
		List<Feature> matches = dictionary.byTerm("auto").forLocale(Locale.GERMAN).limit(1).find();
		assertEquals(1, matches.size());
	}

	@Test public void find_only_brands_by_name_finds_no_normal_entries()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("Bäckerei").forLocale((Locale) null).isSuggestion(true).find();
		assertEquals(0, matches.size());
	}

	@Test public void find_no_brands_by_name_finds_only_normal_entries()
	{
		FeatureDictionary dictionary = dictionary(bank, bank_of_america);
		List<Feature> matches = dictionary.byTerm("Bank").forLocale((Locale) null).isSuggestion(false).find();
		assertEquals(listOf(bank), matches);
	}

	@Test public void find_no_entry_by_term_because_wrong_locale()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		assertEquals(listOf(), dictionary.byTerm("Bäck").forLocale(Locale.ITALIAN).find());
	}

	@Test public void find_entry_by_term_because_fallback_locale()
	{
		FeatureDictionary dictionary = dictionary(bakery);
		List<Feature> matches = dictionary.byTerm("Bäck").forLocale(Locale.ITALIAN, null).find();
		assertEquals(listOf(bakery), matches);
	}

	@Test public void find_multi_word_brand_feature()
	{
		FeatureDictionary dictionary = dictionary(deutsche_bank);
		assertEquals(listOf(deutsche_bank), dictionary.byTerm("Deutsche Ba").find());
		assertEquals(listOf(deutsche_bank), dictionary.byTerm("Deut").find());
		// by-word only for non-brand features
		assertTrue(dictionary.byTerm("Ban").find().isEmpty());
	}

	@Test public void find_multi_word_feature()
	{
		FeatureDictionary dictionary = dictionary(miniature_train_shop);
		assertEquals(listOf(miniature_train_shop), dictionary.byTerm("mini").forLocale((Locale) null).find());
		assertEquals(listOf(miniature_train_shop), dictionary.byTerm("train").forLocale((Locale) null).find());
		assertEquals(listOf(miniature_train_shop), dictionary.byTerm("shop").forLocale((Locale) null).find());
		assertEquals(listOf(miniature_train_shop), dictionary.byTerm("Miniature Trai").forLocale((Locale) null).find());
		assertTrue(dictionary.byTerm("Train Sho").forLocale((Locale) null).find().isEmpty());
	}

	//endregion

	@Test public void find_by_term_sorts_result_in_correct_order()
	{
		FeatureDictionary dictionary = dictionary(
				casino, baenk, bad_bank, stock_exchange, bank_of_liechtenstein, bank, bench, atm,
				bank_of_america, deutsche_bank);
		List<Feature> matches = dictionary.byTerm("Bank").forLocale((Locale) null).find();
		assertEquals(listOf(
				bank,       // exact name matches
				baenk,      // exact name matches (diacritics and case insensitive)
				atm,        // starts-with name matches
				bad_bank,   // starts-with-word name matches
				bank_of_america,        // starts-with brand name matches
				bank_of_liechtenstein,  // starts-with brand name matches - lower matchScore
				bench,           // found word in terms - higher matchScore
				stock_exchange   // found word in terms - lower matchScore
				// casino,       // not included: "Spielbank" does not start with "bank"
				// deutsche_bank // not included: "Deutsche Bank" does not start with "bank"
		), matches);
	}

	@Test public void some_tests_with_real_data()
	{
		LocalizedFeatureCollection featureCollection = new IDLocalizedFeatureCollection(new LivePresetDataAccessAdapter());
		featureCollection.getAll(listOf(Locale.ENGLISH));
		FeatureDictionary dictionary = new FeatureDictionary(featureCollection, null);

		List<Feature> matches = dictionary
				.byTags(mapOf(tag("amenity", "studio")))
				.forLocale(Locale.ENGLISH)
				.find();
		assertEquals(1, matches.size());
		assertEquals("Studio", matches.get(0).getName());

		List<Feature> matches2 = dictionary
				.byTags(mapOf(tag("amenity", "studio"), tag("studio", "audio")))
				.forLocale(Locale.ENGLISH)
				.find();
		assertEquals(1, matches2.size());
		assertEquals("Recording Studio", matches2.get(0).getName());

		List<Feature> matches3 = dictionary
				.byTerm("Chinese Res")
				.forLocale(Locale.ENGLISH)
				.find();
		assertEquals(1, matches3.size());
		assertEquals("Chinese Restaurant", matches3.get(0).getName());
	}

	private static FeatureDictionary dictionary(Feature... entries)
	{
		List<Feature> features = new ArrayList<>();
		List<Feature> brandFeatures = new ArrayList<>();
		for (Feature entry : entries) {
			if (entry instanceof SuggestionFeature) {
				brandFeatures.add(entry);
			} else {
				features.add(entry);
			}
		}
		return new FeatureDictionary(
				new TestLocalizedFeatureCollection(features),
				new TestPerCountryFeatureCollection(brandFeatures)
		);
	}

	private static Feature feature(
			String id, Map<String, String> tags, String name, List<String> terms,
			List<String> countryCodes, List<String> excludeCountryCodes, boolean searchable,
			double matchScore, Map<String, String> addTags, boolean isSuggestion, Locale locale
	) {
		if (isSuggestion) {
			return new SuggestionFeature(
					id, tags, POINT, name, null, null, terms, countryCodes,
					excludeCountryCodes, searchable, matchScore, addTags, mapOf()
			);
		} else {
			BaseFeature f = new BaseFeature(
					id, tags, POINT, name, null, null, terms, countryCodes,
					excludeCountryCodes, searchable, matchScore, addTags, mapOf()
			);
			if (locale != null) {
				return new LocalizedFeature(f, locale, f.getName(), f.getTerms());
			} else {
				return f;
			}
		}
	}

	static class SuggestionFeature extends BaseFeature {
		public SuggestionFeature(String id, Map<String, String> tags, List<GeometryType> geometry, String name, String icon, String imageURL, List<String> terms, List<String> includeCountryCodes, List<String> excludeCountryCodes, boolean searchable, double matchScore, Map<String, String> addTags, Map<String, String> removeTags) {
			super(id, tags, geometry, name, icon, imageURL, terms, includeCountryCodes, excludeCountryCodes, searchable, matchScore, addTags, removeTags);
		}
	}
}
