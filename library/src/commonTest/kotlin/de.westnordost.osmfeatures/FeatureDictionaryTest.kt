package de.westnordost.osmfeatures

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FeatureDictionaryTest {


    private val bakery = feature( // unlocalized shop=bakery
        id = "shop/bakery",
        tags = mapOf("shop" to "bakery"),
        names = listOf("Bäckerei"),
        terms = listOf("Brot")
    )
    private val panetteria = feature( // localized shop=bakery
        id = "shop/bakery",
        tags = mapOf("shop" to "bakery"),
        names = listOf("Panetteria"),
        locale = ITALIAN
    )
    private val ditsch = feature( // brand in DE for shop=bakery
        id = "shop/bakery/Ditsch",
        tags = mapOf("shop" to "bakery", "name" to "Ditsch"),
        names = listOf("Ditsch"),
        countryCodes = listOf("DE", "AT"),
        excludeCountryCodes = listOf("AT-9"),
        addTags = mapOf("wikipedia" to "de:Brezelb%C3%A4ckerei_Ditsch", "brand" to "Ditsch"),
        isSuggestion = true
    )
    private val ditschRussian = feature( // brand in RU for shop=bakery
        id = "shop/bakery/Дитсч",
        tags = mapOf("shop" to "bakery", "name" to "Ditsch"),
        names = listOf("Дитсч"),
        countryCodes = listOf("RU", "UA-43"),
        addTags = mapOf("wikipedia" to "de:Brezelb%C3%A4ckerei_Ditsch", "brand" to "Дитсч"),
        isSuggestion = true
    )
    private val ditschInternational = feature( // brand everywhere for shop=bakery
        id = "shop/bakery/Ditsh",
        tags = mapOf("shop" to "bakery", "name" to "Ditsch"),
        names  = listOf("Ditsh"),
        addTags = mapOf("wikipedia" to "de:Brezelb%C3%A4ckerei_Ditsch"),
        isSuggestion = true
    )
    private val liquor_store = feature( // English localized unspecific shop=alcohol
        id = "shop/alcohol",
        tags = mapOf("shop" to "alcohol"),
        names = listOf("Off licence (Alcohol shop)"),
        locale = UK
    )
    private val car_dealer = feature( // German localized  unspecific shop=car
        id = "shop/car",
        tags = mapOf("shop" to "car"),
        names = listOf("Autohändler"),
        terms = listOf("auto"),
        locale = GERMAN
    )
    private val second_hand_car_dealer = feature( // German localized shop=car with subtags
        id = "shop/car/second_hand",
        tags = mapOf("shop" to "car", "second_hand" to "only"),
        names = listOf("Gebrauchtwagenhändler"),
        terms = listOf("auto"),
        locale = GERMAN
    )
    private val scheisshaus = feature( // unsearchable feature
        id = "amenity/scheißhaus",
        tags = mapOf("amenity" to "scheißhaus"),
        names = listOf("Scheißhaus"),
        searchable = false
    )
    private val bank = feature( // unlocalized shop=bank (Bank)
        id = "amenity/bank",
        tags = mapOf("amenity" to "bank"),
        names = listOf("Bank")
    )
    private val bench = feature( // unlocalized amenity=bench (PARKbank)
        id = "amenity/bench",
        tags = mapOf("amenity" to "bench"),
        names = listOf("Parkbank"),
        terms = listOf("Bank"),
        matchScore = 5.0f
    )
    private val casino = feature( // unlocalized amenity=casino (SPIELbank)
        id = "amenity/casino",
        tags = mapOf("amenity" to "casino"),
        names = listOf("Spielbank"),
        terms = listOf("Kasino")
    )
    private val atm = feature( // unlocalized amenity=atm (BankOMAT)
        id = "amenity/atm",
        tags = mapOf("amenity" to "atm"),
        names = listOf("Bankomat")
    )
    private val stock_exchange = feature( // unlocalized amenity=stock_exchange (has "Banking" as term)
        id = "amenity/stock_exchange",
        tags = mapOf("amenity" to "stock_exchange"),
        names = listOf("Börse"),
        terms = listOf("Banking"),
    )
    private val bank_of_america = feature( // Brand of a amenity=bank (has "Bank" in name)
        id = "amenity/bank/Bank of America",
        tags = mapOf("amenity" to "bank", "name" to "Bank of America"),
        names = listOf("Bank of America"),
        isSuggestion = true
    )
    private val bank_of_liechtenstein = feature( // Brand of a amenity=bank (has "Bank" in name), but low matchScore
        id = "amenity/bank/Bank of Liechtenstein",
        tags = mapOf("amenity" to "bank", "name" to "Bank of Liechtenstein"),
        names = listOf("Bank of Liechtenstein"),
        matchScore = 0.2f,
        isSuggestion = true,
    )
    private val deutsche_bank = feature( // Brand of a amenity=bank (does not start with "Bank" in name)
        id = "amenity/bank/Deutsche Bank",
        tags = mapOf("amenity" to "bank", "name" to "Deutsche Bank"),
        names = listOf("Deutsche Bank"),
        isSuggestion = true
    )
    private val baenk = feature( // amenity=bänk, to see if diacritics match non-strictly ("a" finds "ä")
        id = "amenity/bänk",
        tags = mapOf("amenity" to "bänk"),
        names = listOf("Bänk"),
    )
    private val bad_bank = feature( // amenity=bank with subtags that has "Bank" in name but it is not the first word
        id = "amenity/bank/bad",
        tags = mapOf("amenity" to "bank", "goodity" to "bad"),
        names = listOf("Bad Bank")
    )
    private val thieves_guild = feature( // only has "bank" in an alias
        id = "amenity/thieves_guild",
        tags = mapOf("amenity" to "thieves_guild"),
        names = listOf("Diebesgilde", "Bankräuberausbildungszentrum"),
    )
    private val miniature_train_shop = feature( // feature whose name consists of several words
        id = "shop/miniature_train",
        tags = mapOf("shop" to "miniature_train"),
        names = listOf("Miniature Train Shop"),
    )

    //region by tags
    
    @Test
    fun find_no_entry_by_tags() {
        assertEquals(
            emptyList(),
            dictionary(bakery).byTags(mapOf("shop" to "supermarket")).find()
        )
    }

    @Test
    fun find_no_entry_because_wrong_geometry() {
        assertEquals(
            emptyList(),
            dictionary(bakery)
                .byTags(mapOf("shop" to "bakery"))
                .forGeometry(GeometryType.RELATION)
                .find()
        )
    }

    @Test
    fun find_no_entry_because_wrong_locale() {
        assertEquals(
            emptyList(),
            dictionary(bakery)
                .byTags(mapOf("shop" to "bakery"))
                .forLocale(ITALIAN)
                .find()
        )
    }

    @Test
    fun find_entry_because_fallback_locale() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery)
                .byTags(mapOf("shop" to "bakery"))
                .forLocale(ITALIAN, null)
                .find()
        )
    }

    @Test
    fun find_entry_by_tags() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).byTags(mapOf("shop" to "bakery")).find()
        )
    }

    @Test
    fun find_non_searchable_entry_by_tags() {
        assertEquals(
            listOf(scheisshaus),
            dictionary(scheisshaus).byTags(mapOf("amenity" to "scheißhaus")).find()
        )
    }

    @Test
    fun find_entry_by_tags_correct_geometry() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery)
                .byTags(mapOf("shop" to "bakery"))
                .forGeometry(GeometryType.POINT)
                .find()
        )
    }

    @Test
    fun find_brand_entry_by_tags() {
        assertEquals(
            listOf(ditsch),
            dictionary(bakery, ditsch)
                .byTags(mapOf("shop" to "bakery", "name" to "Ditsch"))
                .inCountry("DE")
                .find()
        )
    }

    @Test
    fun find_only_entries_with_given_locale() {
        val tags = mapOf("shop" to "bakery")
        val dictionary = dictionary(bakery, panetteria)

        assertEquals(
            listOf(panetteria),
            dictionary.byTags(tags).forLocale(ITALIAN).find()
        )
        assertEquals(
            emptyList(),
            dictionary.byTags(tags).forLocale(ENGLISH).find()
        )
        assertEquals(
            listOf(bakery),
            dictionary.byTags(tags).forLocale(null).find()
        )
    }

    @Test
    fun find_only_brands_finds_no_normal_entries() {
        assertEquals(
            0,
            dictionary(bakery)
                .byTags(mapOf("shop" to "bakery", "name" to "Ditsch"))
                .isSuggestion(true)
                .find().size
        )
    }

    @Test
    fun find_no_brands_finds_only_normal_entries() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery, ditsch)
                .byTags(mapOf("shop" to "bakery", "name" to "Ditsch"))
                .isSuggestion(false)
                .find()
        )
    }

    @Test
    fun find_multiple_brands_sorts_by_locale() {
        assertEquals(
            ditschInternational,
            dictionary(ditschRussian, ditschInternational, ditsch)
                .byTags(mapOf("shop" to "bakery", "name" to "Ditsch"))
                .forLocale(null)
                .find()
                .first()
        )
    }

    @Test
    fun find_multiple_entries_by_tags() {
        assertEquals(
            2,
            dictionary(bakery, bank)
                .byTags(mapOf("shop" to "bakery", "amenity" to "bank"))
                .find()
                .size
        )
    }

    @Test
    fun do_not_find_entry_with_too_specific_tags() {
        assertEquals(
            listOf(car_dealer),
            dictionary(car_dealer, second_hand_car_dealer)
                .byTags(mapOf("shop" to "car"))
                .forLocale(GERMAN, null)
                .find()
        )
    }

    @Test
    fun find_entry_with_specific_tags() {
        assertEquals(
            listOf(second_hand_car_dealer),
            dictionary(car_dealer, second_hand_car_dealer)
                .byTags(mapOf("shop" to "car", "second_hand" to "only"))
                .forLocale(GERMAN, null)
                .find()
        )
    }

    //endregion
    
    //region by name
    
    @Test
    fun find_no_entry_by_name() {
        assertEquals(emptyList(), dictionary(bakery).byTerm("Supermarkt").find())
    }

    @Test
    fun find_no_entry_by_name_because_wrong_geometry() {
        assertEquals(
            emptyList(),
            dictionary(bakery).byTerm("Bäckerei").forGeometry(GeometryType.LINE).find()
        )
    }

    @Test
    fun find_no_entry_by_name_because_wrong_country() {
        val dictionary = dictionary(ditsch, ditschRussian)
        assertEquals(
            emptyList(),
            dictionary.byTerm("Ditsch").find()
        )
        assertEquals(
            emptyList(),
            dictionary.byTerm("Ditsch").inCountry("FR").find()
        ) // not in France
        assertEquals(
            emptyList(),
            dictionary.byTerm("Ditsch").inCountry("AT-9").find()
        ) // in all of AT but not Vienna
        assertEquals(
            emptyList(),
            dictionary.byTerm("Дитсч").inCountry("UA").find()
        ) // only on the Krim
    }

    @Test
    fun find_no_non_searchable_entry_by_name() {
        assertEquals(emptyList(), dictionary(scheisshaus).byTerm("Scheißhaus").find())
    }

    @Test
    fun find_entry_by_name() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery)
                .byTerm("Bäckerei")
                .forLocale(null)
                .find())
    }

    @Test
    fun find_entry_by_name_with_correct_geometry() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery)
                .byTerm("Bäckerei")
                .forLocale(null)
                .forGeometry(GeometryType.POINT)
                .find()
        )
    }

    @Test
    fun find_entry_by_name_with_correct_country() {
        val dictionary = dictionary(ditsch, ditschRussian, bakery)
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("DE").find())
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("DE-TH").find())
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("AT").find())
        assertEquals(listOf(ditsch), dictionary.byTerm("Ditsch").inCountry("AT-5").find())
        assertEquals(listOf(ditschRussian), dictionary.byTerm("Дитсч").inCountry("UA-43").find())
        assertEquals(listOf(ditschRussian), dictionary.byTerm("Дитсч").inCountry("RU-KHA").find())
    }

    @Test
    fun find_entry_by_name_case_insensitive() {
        assertEquals(listOf(bakery), dictionary(bakery).byTerm("BÄCkErEI").forLocale(null).find())
    }

    @Test
    fun find_entry_by_name_diacritics_insensitive() {
        assertEquals(listOf(bakery), dictionary(bakery).byTerm("Backérèi").forLocale(null).find())
    }

    @Test
    fun find_entry_by_term() {
        assertEquals(listOf(bakery), dictionary(bakery).byTerm("bro").forLocale(null).find())
    }

    @Test
    fun find_entry_by_term_brackets() {
        val dictionary = dictionary(liquor_store)
        assertEquals(
            listOf(liquor_store),
            dictionary.byTerm("Alcohol").forLocale(UK).find()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.byTerm("Off licence (Alcohol Shop)").forLocale(UK).find()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.byTerm("Off Licence").forLocale(UK).find()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.byTerm("Off Licence (Alco").forLocale(UK).find()
        )
    }

    @Test
    fun find_entry_by_term_case_insensitive() {
        assertEquals(listOf(bakery), dictionary(bakery).byTerm("BRO").forLocale(null).find())
    }

    @Test
    fun find_entry_by_term_diacritics_insensitive() {
        assertEquals(listOf(bakery), dictionary(bakery).byTerm("bró").forLocale(null).find())
    }

    @Test
    fun find_multiple_entries_by_term() {
        assertEquals(
            setOf(second_hand_car_dealer, car_dealer),
            dictionary(second_hand_car_dealer, car_dealer)
                .byTerm("auto")
                .forLocale(GERMAN)
                .find()
                .toSet()
        )
    }

    @Test
    fun find_multiple_entries_by_name_but_respect_limit() {
        assertEquals(
            1,
            dictionary(second_hand_car_dealer, car_dealer)
                .byTerm("auto")
                .forLocale(GERMAN)
                .limit(1)
                .find()
                .size
        )
    }

    @Test
    fun find_only_brands_by_name_finds_no_normal_entries() {
        assertEquals(
            0,
            dictionary(bakery)
                .byTerm("Bäckerei")
                .forLocale(null)
                .isSuggestion(true)
                .find()
                .size
        )
    }

    @Test
    fun find_no_brands_by_name_finds_only_normal_entries() {
        assertEquals(
            listOf(bank),
            dictionary(bank, bank_of_america)
                .byTerm("Bank")
                .forLocale(null)
                .isSuggestion(false)
                .find()
        )
    }

    @Test
    fun find_no_entry_by_term_because_wrong_locale() {
        assertEquals(
            emptyList(),
            dictionary(bakery).byTerm("Bäck").forLocale(ITALIAN).find()
        )
    }

    @Test
    fun find_entry_by_term_because_fallback_locale() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).byTerm("Bäck").forLocale(ITALIAN, null).find()
        )
    }

    @Test
    fun find_multi_word_brand_feature() {
        val dictionary = dictionary(deutsche_bank)
        assertEquals(listOf(deutsche_bank), dictionary.byTerm("Deutsche Ba").find())
        assertEquals(listOf(deutsche_bank), dictionary.byTerm("Deut").find())
        // by-word only for non-brand features
        assertTrue(dictionary.byTerm("Ban").find().isEmpty())
    }

    @Test
    fun find_multi_word_feature() {
        val dictionary = dictionary(miniature_train_shop)
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("mini").forLocale(null).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("train").forLocale(null).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("shop").forLocale(null).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("Miniature Trai").forLocale(null).find()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.byTerm("Miniature Train Shop").forLocale(null).find()
        )
        assertTrue(dictionary.byTerm("Train Sho").forLocale(null).find().isEmpty())
    }

    @Test
    fun find_entry_by_tag_value() {
        assertEquals(
            listOf(panetteria),
            dictionary(panetteria).byTerm("bakery").forLocale(ITALIAN).find()
        )
    }

    //endregion

    //region by id

    @Test
    fun find_no_entry_by_id() {
        assertNull(dictionary(bakery).byId("amenity/hospital").get())
    }

    @Test
    fun find_no_entry_by_id_because_unlocalized_results_are_excluded() {
        assertNull(dictionary(bakery).byId("shop/bakery").forLocale(ITALIAN).get())
    }

    @Test
    fun find_entry_by_id() {
        val dictionary = dictionary(bakery)
        assertEquals(bakery, dictionary.byId("shop/bakery").get())
        assertEquals(bakery, dictionary.byId("shop/bakery").forLocale(CHINESE, null).get())
    }

    @Test
    fun find_localized_entry_by_id() {
        val dictionary = dictionary(panetteria)
        assertEquals(
            panetteria,
            dictionary.byId("shop/bakery").forLocale(ITALIAN).get()
        )
        assertEquals(
            panetteria,
            dictionary.byId("shop/bakery").forLocale(ITALIAN, null).get()
        )
    }

    @Test
    fun find_no_entry_by_id_because_wrong_country() {
        val dictionary = dictionary(ditsch)
        assertNull(dictionary.byId("shop/bakery/Ditsch").get())
        assertNull(dictionary.byId("shop/bakery/Ditsch").inCountry("IT").get())
        assertNull(dictionary.byId("shop/bakery/Ditsch").inCountry("AT-9").get())
    }

    @Test
    fun find_entry_by_id_in_country() {
        val dictionary = dictionary(ditsch)
        assertEquals(ditsch, dictionary.byId("shop/bakery/Ditsch").inCountry("AT").get())
        assertEquals(ditsch, dictionary.byId("shop/bakery/Ditsch").inCountry("DE").get())
    }

    //endregion

    @Test
    fun find_by_term_sorts_result_in_correct_order() {
        val dictionary = dictionary(
            casino, baenk, bad_bank, stock_exchange, bank_of_liechtenstein, bank, bench, atm,
            bank_of_america, deutsche_bank, thieves_guild
        )
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
            ),
            dictionary.byTerm("Bank").forLocale(null).find()
        )
    }

    @Test
    fun issue19() {
        val lush = feature(
            id = "shop/cosmetics/lush-a08666",
            tags = mapOf("brand:wikidata" to "Q1585448", "shop" to "cosmetics"),
            geometries = listOf(GeometryType.POINT, GeometryType.AREA),
            names = listOf("Lush"),
            terms = listOf("lush"),
            matchScore = 2.0f,
            addTags = mapOf(
                "brand" to "Lush",
                "brand:wikidata" to "Q1585448",
                "name" to "Lush",
                "shop" to "cosmetics"
            ),
            isSuggestion = true,
        )

        val dictionary = dictionary(lush)

        val byTags = dictionary
            .byTags(mapOf("brand:wikidata" to "Q1585448", "shop" to "cosmetics"))
            .forLocale(GERMAN, null)
            .inCountry("DE")
            .find()
        assertEquals(1, byTags.size)
        assertEquals(lush, byTags[0])

        val byTerm = dictionary
            .byTerm("Lush")
            .forLocale(GERMAN, null)
            .inCountry("DE")
            .find()
        assertEquals(1, byTerm.size)
        assertEquals(lush, byTerm[0])

        val byId = dictionary
            .byId("shop/cosmetics/lush-a08666")
            .forLocale(GERMAN, null)
            .inCountry("DE")
            .get()
        assertEquals(lush, byId)
    }
}

private val ENGLISH = Locale("en")
private val UK = Locale("en","UK")
private val ITALIAN = Locale("it")
private val GERMAN = Locale("de")
private val CHINESE = Locale("zh")

private fun dictionary(vararg entries: Feature) = FeatureDictionary(
    TestLocalizedFeatureCollection(entries.filterNot { it.isSuggestion }),
    TestPerCountryFeatureCollection(entries.filter { it.isSuggestion })
)

private fun feature(
    id: String,
    tags: Map<String, String>,
    geometries: List<GeometryType> = listOf(GeometryType.POINT),
    names: List<String>,
    terms: List<String> = listOf(),
    countryCodes: List<String> = listOf(),
    excludeCountryCodes: List<String> = listOf(),
    searchable: Boolean = true,
    matchScore: Float = 1.0f,
    addTags: Map<String, String> = mapOf(),
    isSuggestion: Boolean = false,
    locale: Locale? = null
): Feature {
    val f = BaseFeature(
        id, tags, geometries, null, null, names, terms, countryCodes,
        excludeCountryCodes, searchable, matchScore, isSuggestion, addTags, mapOf()
    )
    return if (locale != null) LocalizedFeature(f, locale, f.names, f.terms) else f
}