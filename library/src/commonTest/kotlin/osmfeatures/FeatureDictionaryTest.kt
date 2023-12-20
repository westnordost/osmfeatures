package osmfeatures

import de.westnordost.osmfeatures.BaseFeature
import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.GeometryType
import de.westnordost.osmfeatures.IDLocalizedFeatureCollection
import de.westnordost.osmfeatures.Locale
import de.westnordost.osmfeatures.LocalizedFeature
import org.junit.Test
import de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder
import org.junit.Assert.*
import osmfeatures.MapEntry.Companion.tag
import osmfeatures.MapEntry.Companion.mapOf
import java.util.Collections

class FeatureDictionaryTest {
    private val bakery: Feature = feature( // unlocalized shop=bakery
        "shop/bakery",
        mapOf(tag("shop", "bakery")),
        POINT,
        listOf("Bäckerei"),
        listOf("Brot"),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        null
    )
    private val panetteria: Feature = feature( // localized shop=bakery
        "shop/bakery",
        mapOf(tag("shop", "bakery")),
        POINT,
        listOf("Panetteria"),
        listOf(),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        Locale.ITALIAN
    )
    private val ditsch: Feature = feature( // brand in DE for shop=bakery
        "shop/bakery/Ditsch",
        mapOf(tag("shop", "bakery"), tag("name", "Ditsch")),
        POINT,
        listOf("Ditsch"),
        listOf(),
        listOf("DE", "AT"),
        listOf("AT-9"),
        true,
        1.0f,
        mapOf(tag("wikipedia", "de:Brezelb%C3%A4ckerei_Ditsch"), tag("brand", "Ditsch")),
        true,
        null
    )
    private val ditschRussian: Feature = feature( // brand in RU for shop=bakery
        "shop/bakery/Дитсч",
        mapOf(tag("shop", "bakery"), tag("name", "Ditsch")),
        POINT,
        listOf("Дитсч"),
        listOf(),
        listOf("RU", "UA-43"),
        listOf(),
        true,
        1.0f,
        mapOf(tag("wikipedia", "de:Brezelb%C3%A4ckerei_Ditsch"), tag("brand", "Дитсч")),
        true,
        null
    )
    private val ditschInternational: Feature = feature( // brand everywhere for shop=bakery
        "shop/bakery/Ditsh",
        mapOf(tag("shop", "bakery"), tag("name", "Ditsch")),
        POINT,
        listOf("Ditsh"),
        listOf(),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(tag("wikipedia", "de:Brezelb%C3%A4ckerei_Ditsch")),
        true,
        null
    )
    private val liquor_store: Feature = feature( // English localized unspecific shop=alcohol
        "shop/alcohol",
        mapOf(tag("shop", "alcohol")),
        POINT,
        listOf("Off licence (Alcohol shop)"),
        listOf(),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        Locale.UK
    )
    private val car_dealer: Feature = feature( // German localized  unspecific shop=car
        "shop/car",
        mapOf(tag("shop", "car")),
        POINT,
        listOf("Autohändler"),
        listOf("auto"),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        Locale.GERMAN
    )
    private val second_hand_car_dealer: Feature = feature( // German localized shop=car with subtags
        "shop/car/second_hand",
        mapOf(tag("shop", "car"), tag("second_hand", "only")),
        POINT,
        listOf("Gebrauchtwagenhändler"),
        listOf("auto"),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        Locale.GERMAN
    )
    private val scheisshaus: Feature = feature( // unsearchable feature
        "amenity/scheißhaus",
        mapOf(tag("amenity", "scheißhaus")),
        POINT,
        listOf("Scheißhaus"),
        listOf(),
        listOf(),
        listOf(),
        false,  // <--- not searchable!
        1.0f,
        mapOf(),
        false,
        null
    )
    private val bank: Feature = feature( // unlocalized shop=bank (Bank)
        "amenity/bank",
        mapOf(tag("amenity", "bank")),
        POINT,
        listOf("Bank"),
        listOf(),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        null
    )
    private val bench: Feature = feature( // unlocalized amenity=bench (PARKbank)
        "amenity/bench",
        mapOf(tag("amenity", "bench")),
        POINT,
        listOf("Parkbank"),
        listOf("Bank"),
        listOf(),
        listOf(),
        true,
        5.0f,
        mapOf(),
        false,
        null
    )
    private val casino: Feature = feature( // unlocalized amenity=casino (SPIELbank)
        "amenity/casino",
        mapOf(tag("amenity", "casino")),
        POINT,
        listOf("Spielbank"),
        listOf("Kasino"),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        null
    )
    private val atm: Feature = feature( // unlocalized amenity=atm (BankOMAT)
        "amenity/atm",
        mapOf(tag("amenity", "atm")),
        POINT,
        listOf("Bankomat"),
        listOf(),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        null
    )
    private val stock_exchange: Feature =
        feature( // unlocalized amenity=stock_exchange (has "Banking" as term)
            "amenity/stock_exchange",
            mapOf(tag("amenity", "stock_exchange")),
            POINT,
            listOf("Börse"),
            listOf("Banking"),
            listOf(),
            listOf(),
            true,
            1.0f,
            mapOf(),
            false,
            null
        )
    private val bank_of_america: Feature = feature( // Brand of a amenity=bank (has "Bank" in name)
        "amenity/bank/Bank of America",
        mapOf(tag("amenity", "bank"), tag("name", "Bank of America")),
        POINT,
        listOf("Bank of America"),
        listOf(),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        true,
        null
    )
    private val bank_of_liechtenstein: Feature =
        feature( // Brand of a amenity=bank (has "Bank" in name), but low matchScore
            "amenity/bank/Bank of Liechtenstein",
            mapOf(tag("amenity", "bank"), tag("name", "Bank of Liechtenstein")),
            POINT,
            listOf("Bank of Liechtenstein"),
            listOf(),
            listOf(),
            listOf(),
            true,
            0.2f,
            mapOf(),
            true,
            null
        )
    private val deutsche_bank: Feature =
        feature( // Brand of a amenity=bank (does not start with "Bank" in name)
            "amenity/bank/Deutsche Bank",
            mapOf(tag("amenity", "bank"), tag("name", "Deutsche Bank")),
            POINT,
            listOf("Deutsche Bank"),
            listOf(),
            listOf(),
            listOf(),
            true,
            1.0f,
            mapOf(),
            true,
            null
        )
    private val baenk: Feature =
        feature( // amenity=bänk, to see if diacritics match non-strictly ("a" finds "ä")
            "amenity/bänk",
            mapOf(tag("amenity", "bänk")),
            POINT,
            listOf("Bänk"),
            listOf(),
            listOf(),
            listOf(),
            true,
            1.0f,
            mapOf(),
            false,
            null
        )
    private val bad_bank: Feature =
        feature( // amenity=bank with subtags that has "Bank" in name but it is not the first word
            "amenity/bank/bad",
            mapOf(tag("amenity", "bank"), tag("goodity", "bad")),
            POINT,
            listOf("Bad Bank"),
            listOf(),
            listOf(),
            listOf(),
            true,
            1.0f,
            mapOf(),
            false,
            null
        )
    private val thieves_guild: Feature = feature( // only has "bank" in an alias
        "amenity/thieves_guild",
        mapOf(tag("amenity", "thieves_guild")),
        POINT,
        listOf("Diebesgilde", "Bankräuberausbildungszentrum"),
        listOf(),
        listOf(),
        listOf(),
        true,
        1.0f,
        mapOf(),
        false,
        null
    )
    private val miniature_train_shop: Feature =
        feature( // feature whose name consists of several words
            "shop/miniature_train",
            mapOf(tag("shop", "miniature_train")),
            POINT,
            listOf("Miniature Train Shop"),
            listOf(),
            listOf(),
            listOf(),
            true,
            1.0f,
            mapOf(),
            false,
            null
        )

    //region by tags
    @Test
    fun find_no_entry_by_tags() {
        val tags: Map<String, String> = mapOf(tag("shop", "supermarket"))
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertEquals(emptyList<Feature>(), dictionary.byTags(tags).find())
    }

    @Test
    fun find_no_entry_because_wrong_geometry() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"))
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertEquals(emptyList<Feature>(), dictionary.byTags(tags).forGeometry(GeometryType.RELATION).find())
    }

    @Test
    fun find_no_entry_because_wrong_locale() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"))
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertEquals(emptyList<Feature>(), dictionary.byTags(tags).forLocale(Locale.ITALIAN).find())
    }

    @Test
    fun find_entry_because_fallback_locale() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"))
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTags(tags).forLocale(Locale.ITALIAN, null).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_entry_by_tags() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"))
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTags(tags).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_non_searchable_entry_by_tags() {
        val tags: Map<String, String> = mapOf(tag("amenity", "scheißhaus"))
        val dictionary: FeatureDictionary = dictionary(scheisshaus)
        val matches: List<Feature> = dictionary.byTags(tags).find()
        assertEquals(listOf(scheisshaus), matches)
    }

    @Test
    fun find_entry_by_tags_correct_geometry() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"))
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTags(tags).forGeometry(GeometryType.POINT).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_brand_entry_by_tags() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"))
        val dictionary: FeatureDictionary = dictionary(bakery, ditsch)
        val matches: List<Feature> = dictionary.byTags(tags).inCountry("DE").find()
        assertEquals(listOf(ditsch), matches)
    }

    @Test
    fun find_only_entries_with_given_locale() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"))
        val dictionary: FeatureDictionary = dictionary(bakery, panetteria)
        assertEquals(listOf(panetteria), dictionary.byTags(tags).forLocale(Locale.ITALIAN).find())
        assertEquals(emptyList<Feature>(), dictionary.byTags(tags).forLocale(Locale.ENGLISH).find())
        assertEquals(listOf(bakery), dictionary.byTags(tags).forLocale(null as Locale?).find())
    }

    @Test
    fun find_only_brands_finds_no_normal_entries() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"))
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTags(tags).isSuggestion(true).find()
        assertEquals(0, matches.size)
    }

    @Test
    fun find_no_brands_finds_only_normal_entries() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"))
        val dictionary: FeatureDictionary = dictionary(bakery, ditsch)
        val matches: List<Feature> = dictionary.byTags(tags).isSuggestion(false).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_multiple_brands_sorts_by_locale() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"), tag("name", "Ditsch"))
        val dictionary: FeatureDictionary = dictionary(ditschRussian, ditschInternational, ditsch)
        val matches: List<Feature> = dictionary.byTags(tags).forLocale(null as Locale?).find()
        assertEquals(ditschInternational, matches[0])
    }

    @Test
    fun find_multiple_entries_by_tags() {
        val tags: Map<String, String> = mapOf(tag("shop", "bakery"), tag("amenity", "bank"))
        val dictionary: FeatureDictionary = dictionary(bakery, bank)
        val matches: List<Feature> = dictionary.byTags(tags).find()
        assertEquals(2, matches.size)
    }

    @Test
    fun do_not_find_entry_with_too_specific_tags() {
        val tags: Map<String, String> = mapOf(tag("shop", "car"))
        val dictionary: FeatureDictionary = dictionary(car_dealer, second_hand_car_dealer)
        val matches: List<Feature> = dictionary.byTags(tags).forLocale(Locale.GERMAN, null).find()
        assertEquals(listOf(car_dealer), matches)
    }

    @Test
    fun find_entry_with_specific_tags() {
        val tags: Map<String, String> = mapOf(tag("shop", "car"), tag("second_hand", "only"))
        val dictionary: FeatureDictionary = dictionary(car_dealer, second_hand_car_dealer)
        val matches: List<Feature> = dictionary.byTags(tags).forLocale(Locale.GERMAN, null).find()
        assertEquals(listOf(second_hand_car_dealer), matches)
    }

    //endregion
    //region by name
    @Test
    fun find_no_entry_by_name() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertEquals(emptyList<Feature>(), dictionary.byTerm("Supermarkt").find())
    }

    @Test
    fun find_no_entry_by_name_because_wrong_geometry() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertEquals(emptyList<Feature>(), dictionary.byTerm("Bäckerei").forGeometry(GeometryType.LINE).find())
    }

    @Test
    fun find_no_entry_by_name_because_wrong_country() {
        val dictionary: FeatureDictionary = dictionary(ditsch, ditschRussian)
        assertEquals(emptyList<Feature>(), dictionary.byTerm("Ditsch").find())
        assertEquals(emptyList<Feature>(), dictionary.byTerm("Ditsch").inCountry("FR").find()) // not in France
        assertEquals(
            emptyList<Feature>(),
            dictionary.byTerm("Ditsch").inCountry("AT-9").find()
        ) // in all of AT but not Vienna
        assertEquals(
            emptyList<Feature>(),
            dictionary.byTerm("Дитсч").inCountry("UA").find()
        ) // only on the Krim
    }

    @Test
    fun find_no_non_searchable_entry_by_name() {
        val dictionary: FeatureDictionary = dictionary(scheisshaus)
        assertEquals(emptyList<Feature>(), dictionary.byTerm("Scheißhaus").find())
    }

    @Test
    fun find_entry_by_name() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTerm("Bäckerei").forLocale(null as Locale?).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_entry_by_name_with_correct_geometry() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> =
            dictionary.byTerm("Bäckerei").forLocale(null as Locale?).forGeometry(GeometryType.POINT)
                .find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_entry_by_name_with_correct_country() {
        val dictionary: FeatureDictionary = dictionary(ditsch, ditschRussian, bakery)
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("DE").find())
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("DE-TH").find())
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("AT").find())
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("AT-5").find())
        assertEquals(listOf(ditschRussian), dictionary.byTerm("Дитсч").inCountry("UA-43").find())
        assertEquals(listOf(ditschRussian), dictionary.byTerm("Дитсч").inCountry("RU-KHA").find())
    }

    @Test
    fun find_entry_by_name_case_insensitive() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTerm("BÄCkErEI").forLocale(null as Locale?).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_entry_by_name_diacritics_insensitive() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTerm("Backérèi").forLocale(null as Locale?).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_entry_by_term() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTerm("bro").forLocale(null as Locale?).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_entry_by_term_brackets() {
        val dictionary: FeatureDictionary = dictionary(liquor_store)
        assertEquals(listOf(liquor_store), dictionary.byTerm("Alcohol").forLocale(Locale.UK).find())
        assertEquals(
            listOf(liquor_store),
            dictionary.byTerm("Off licence (Alcohol Shop)").forLocale(Locale.UK).find()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.byTerm("Off Licence").forLocale(Locale.UK).find()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.byTerm("Off Licence (Alco").forLocale(Locale.UK).find()
        )
    }

    @Test
    fun find_entry_by_term_case_insensitive() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTerm("BRO").forLocale(null as Locale?).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_entry_by_term_diacritics_insensitive() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> = dictionary.byTerm("bró").forLocale(null as Locale?).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_multiple_entries_by_term() {
        val dictionary: FeatureDictionary = dictionary(second_hand_car_dealer, car_dealer)
        val matches: List<Feature> = dictionary.byTerm("auto").forLocale(Locale.GERMAN).find()
        assertEqualsIgnoreOrder(listOf(second_hand_car_dealer, car_dealer), matches)
    }

    @Test
    fun find_multiple_entries_by_name_but_respect_limit() {
        val dictionary: FeatureDictionary = dictionary(second_hand_car_dealer, car_dealer)
        val matches: List<Feature> =
            dictionary.byTerm("auto").forLocale(Locale.GERMAN).limit(1).find()
        assertEquals(1, matches.size)
    }

    @Test
    fun find_only_brands_by_name_finds_no_normal_entries() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> =
            dictionary.byTerm("Bäckerei").forLocale(null as Locale?).isSuggestion(true).find()
        assertEquals(0, matches.size)
    }

    @Test
    fun find_no_brands_by_name_finds_only_normal_entries() {
        val dictionary: FeatureDictionary = dictionary(bank, bank_of_america)
        val matches: List<Feature> =
            dictionary.byTerm("Bank").forLocale(null as Locale?).isSuggestion(false).find()
        assertEquals(listOf(bank), matches)
    }

    @Test
    fun find_no_entry_by_term_because_wrong_locale() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertEquals(emptyList<Feature>(), dictionary.byTerm("Bäck").forLocale(Locale.ITALIAN).find())
    }

    @Test
    fun find_entry_by_term_because_fallback_locale() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        val matches: List<Feature> =
            dictionary.byTerm("Bäck").forLocale(Locale.ITALIAN, null).find()
        assertEquals(listOf(bakery), matches)
    }

    @Test
    fun find_multi_word_brand_feature() {
        val dictionary: FeatureDictionary = dictionary(deutsche_bank)
        assertEquals(listOf(deutsche_bank), dictionary.byTerm("Deutsche Ba").find())
        assertEquals(listOf(deutsche_bank), dictionary.byTerm("Deut").find())
        // by-word only for non-brand features
        assertTrue(dictionary.byTerm("Ban").find().isEmpty())
    }

    @Test
    fun find_multi_word_feature() {
        val dictionary: FeatureDictionary = dictionary(miniature_train_shop)
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("mini").forLocale(null as Locale?).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("train").forLocale(null as Locale?).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("shop").forLocale(null as Locale?).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("Miniature Trai").forLocale(null as Locale?).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("Miniature Train Shop").forLocale(null as Locale?).find()
        )
        assertTrue(dictionary.byTerm("Train Sho").forLocale(null as Locale?).find().isEmpty())
    }

    @Test
    fun find_entry_by_tag_value() {
        val dictionary: FeatureDictionary = dictionary(panetteria)
        val matches: List<Feature> = dictionary.byTerm("bakery").forLocale(Locale.ITALIAN).find()
        assertEquals(listOf(panetteria), matches)
    }

    //endregion
    //region by id
    @Test
    fun find_no_entry_by_id() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertNull(dictionary.byId("amenity/hospital").get())
    }

    @Test
    fun find_no_entry_by_id_because_unlocalized_results_are_excluded() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertNull(dictionary.byId("shop/bakery").forLocale(Locale.ITALIAN).get())
    }

    @Test
    fun find_entry_by_id() {
        val dictionary: FeatureDictionary = dictionary(bakery)
        assertEquals(bakery, dictionary.byId("shop/bakery").get())
        assertEquals(bakery, dictionary.byId("shop/bakery").forLocale(Locale.CHINESE, null).get())
    }

    @Test
    fun find_localized_entry_by_id() {
        val dictionary: FeatureDictionary = dictionary(panetteria)
        assertEquals(panetteria, dictionary.byId("shop/bakery").forLocale(Locale.ITALIAN).get())
        assertEquals(
            panetteria,
            dictionary.byId("shop/bakery").forLocale(Locale.ITALIAN, null).get()
        )
    }

    @Test
    fun find_no_entry_by_id_because_wrong_country() {
        val dictionary: FeatureDictionary = dictionary(ditsch)
        assertNull(dictionary.byId("shop/bakery/Ditsch").get())
        assertNull(dictionary.byId("shop/bakery/Ditsch").inCountry("IT").get())
        assertNull(dictionary.byId("shop/bakery/Ditsch").inCountry("AT-9").get())
    }

    @Test
    fun find_entry_by_id_in_country() {
        val dictionary: FeatureDictionary = dictionary(ditsch)
        assertEquals(ditsch, dictionary.byId("shop/bakery/Ditsch").inCountry("AT").get())
        assertEquals(ditsch, dictionary.byId("shop/bakery/Ditsch").inCountry("DE").get())
    }

    //endregion
    @Test
    fun find_by_term_sorts_result_in_correct_order() {
        val dictionary: FeatureDictionary = dictionary(
            casino, baenk, bad_bank, stock_exchange, bank_of_liechtenstein, bank, bench, atm,
            bank_of_america, deutsche_bank, thieves_guild
        )
        val matches: List<Feature> = dictionary.byTerm("Bank").forLocale(null as Locale?).find()
        assertEquals(
            listOf(
                bank,  // exact name matches
                baenk,  // exact name matches (diacritics and case insensitive)
                atm,  // starts-with name matches
                thieves_guild,  // starts-with alias matches
                bad_bank,  // starts-with-word name matches
                bank_of_america,  // starts-with brand name matches
                bank_of_liechtenstein,  // starts-with brand name matches - lower matchScore
                bench,  // found word in terms - higher matchScore
                stock_exchange // found word in terms - lower matchScore
                // casino,       // not included: "Spielbank" does not start with "bank"
                // deutsche_bank // not included: "Deutsche Bank" does not start with "bank" and is a brand
            ), matches
        )
    }

    @Test
    fun some_tests_with_real_data() {
        val featureCollection: LocalizedFeatureCollection =
            IDLocalizedFeatureCollection(LivePresetDataAccessAdapter())
        featureCollection.getAll(listOf(Locale.ENGLISH))
        val dictionary = FeatureDictionary(featureCollection, null)
        val matches: List<Feature> = dictionary
            .byTags(mapOf(tag("amenity", "studio")))
            .forLocale(Locale.ENGLISH)
            .find()
        assertEquals(1, matches.size)
        assertEquals("Studio", matches[0].name)
        val matches2: List<Feature> = dictionary
            .byTags(mapOf(tag("amenity", "studio"), tag("studio", "audio")))
            .forLocale(Locale.ENGLISH)
            .find()
        assertEquals(1, matches2.size)
        assertEquals("Recording Studio", matches2[0].name)
        val matches3: List<Feature> = dictionary
            .byTerm("Chinese Res")
            .forLocale(Locale.ENGLISH)
            .find()
        assertEquals(1, matches3.size)
        assertEquals("Chinese Restaurant", matches3[0].name)
    }

    @Test
    fun issue19() {
        val lush: Feature = feature(
            "shop/cosmetics/lush-a08666",
            mapOf(tag("brand:wikidata", "Q1585448"), tag("shop", "cosmetics")),
            listOf(GeometryType.POINT, GeometryType.AREA),
            listOf("Lush"),
            listOf("lush"),
            listOf(),
            listOf(),
            true,
            2.0f,
            mapOf(
                tag("brand", "Lush"),
                tag("brand:wikidata", "Q1585448"),
                tag("name", "Lush"),
                tag("shop", "cosmetics")
            ),
            true,
            null
        )
        val dictionary: FeatureDictionary = dictionary(lush)
        val byTags: List<Feature> = dictionary
            .byTags(mapOf(tag("brand:wikidata", "Q1585448"), tag("shop", "cosmetics")))
            .forLocale(Locale.GERMAN, null)
            .inCountry("DE")
            .find()
        assertEquals(1, byTags.size)
        assertEquals(lush, byTags[0])
        val byTerm: List<Feature> = dictionary
            .byTerm("Lush")
            .forLocale(Locale.GERMAN, null)
            .inCountry("DE")
            .find()
        assertEquals(1, byTerm.size)
        assertEquals(lush, byTerm[0])
        val byId: Feature? = dictionary
            .byId("shop/cosmetics/lush-a08666")
            .forLocale(Locale.GERMAN, null)
            .inCountry("DE")
            .get()
        assertEquals(lush, byId)
    }

    internal class SuggestionFeature(
        id: String,
        tags: Map<String, String>,
        geometry: List<GeometryType>,
        icon: String?,
        imageURL: String?,
        names: List<String>,
        terms: List<String>,
        includeCountryCodes: List<String>,
        excludeCountryCodes: List<String>,
        searchable: Boolean,
        matchScore: Float,
        addTags: Map<String, String>,
        removeTags: Map<String, String>
    ) : BaseFeature(
        id,
        tags,
        geometry,
        icon,
        imageURL,
        names,
        terms,
        includeCountryCodes,
        excludeCountryCodes,
        searchable,
        matchScore,
        true,
        addTags,
        removeTags
    )

    companion object {
        private val POINT: List<GeometryType> = Collections.singletonList(GeometryType.POINT)
        private fun dictionary(vararg entries: Feature): FeatureDictionary {
            return FeatureDictionary(
                TestLocalizedFeatureCollection(entries.filterNot { it is SuggestionFeature }),
                TestPerCountryFeatureCollection(entries.filterIsInstance<SuggestionFeature>())
            )
        }

        private fun feature(
            id: String,
            tags: Map<String, String>,
            geometries: List<GeometryType>,
            names: List<String>,
            terms: List<String>,
            countryCodes: List<String>,
            excludeCountryCodes: List<String>,
            searchable: Boolean,
            matchScore: Float,
            addTags: Map<String, String>,
            isSuggestion: Boolean,
            locale: Locale?
        ): Feature {
            return if (isSuggestion) {
                SuggestionFeature(
                    id, tags, geometries, null, null, names, terms, countryCodes,
                    excludeCountryCodes, searchable, matchScore, addTags, mapOf()
                )
            } else {
                val f = BaseFeature(
                    id, tags, geometries, null, null, names, terms, countryCodes,
                    excludeCountryCodes, searchable, matchScore, false, addTags, mapOf()
                )
                if (locale != null) {
                    LocalizedFeature(f, locale, f.names, f.terms.orEmpty())
                } else {
                    f
                }
            }
        }
    }
}
