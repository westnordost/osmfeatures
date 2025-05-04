package de.westnordost.osmfeatures

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

import de.westnordost.osmfeatures.GeometryType.*

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
        language = "it"
    )
    private val ditsch = feature( // brand in DE for shop=bakery
        id = "shop/bakery/Ditsch",
        tags = mapOf("shop" to "bakery", "name" to "Ditsch"),
        names = listOf("Ditsch"),
        includeCountryCodes = listOf("DE", "AT"),
        excludeCountryCodes = listOf("AT-9"),
        addTags = mapOf("wikipedia" to "de:Brezelb%C3%A4ckerei_Ditsch", "brand" to "Ditsch"),
        isSuggestion = true
    )
    private val ditschRussian = feature( // brand in RU for shop=bakery
        id = "shop/bakery/Дитсч",
        tags = mapOf("shop" to "bakery", "name" to "Ditsch"),
        names = listOf("Дитсч"),
        includeCountryCodes = listOf("RU", "UA-43"),
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
        language = "en-GB"
    )
    private val car_dealer = feature( // German localized  unspecific shop=car
        id = "shop/car",
        tags = mapOf("shop" to "car"),
        names = listOf("Autohändler"),
        terms = listOf("auto"),
        language = "de"
    )
    private val second_hand_car_dealer = feature( // German localized shop=car with subtags
        id = "shop/car/second_hand",
        tags = mapOf("shop" to "car", "second_hand" to "only"),
        names = listOf("Gebrauchtwagenhändler"),
        terms = listOf("auto"),
        language = "de"
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
    private val ms_food = feature( // Combined letter + punctuation words in the middle of the brand
        id = "shop/convenience/Some M&S Food",
        tags = mapOf("shop" to "convenience", "name" to "Some M&S Food"),
        names = listOf("Some M&S Food"),
        isSuggestion = true
    )
    private val seven_eleven = feature( // Brand with non-space word separator
        id = "shop/convenience/7-Eleven",
        tags = mapOf("shop" to "convenience", "name" to "7-Eleven"),
        names = listOf("7-Eleven"),
        isSuggestion = true
    )
    private val seven_eleven_jp = feature( // Brand with non-space word separator, localized to Japan
        id = "shop/convenience/7-Eleven-JP",
        tags = mapOf("shop" to "convenience", "name" to "セブン-イレブン", "name:en" to "7-Eleven"),
        names = listOf("セブン-イレブン"),
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
    private val postbox = feature(
        id = "amenity/post_box",
        tags = mapOf("amenity" to "post_box"),
        names = listOf("Post box"),
        excludeCountryCodes = listOf("US"),
    )
    private val postboxUS = feature(
        id = "amenity/post_box/post_box-US",
        tags = mapOf("amenity" to "post_box"),
        names = listOf("Post box in US"),
        includeCountryCodes = listOf("US"),
    )
    private val amenity = feature(
        id = "amenity",
        keys = setOf("amenity"),
        names = listOf("Some amenity"),
    )

    //region by tags
    
    @Test
    fun find_no_entry_by_tags() {
        assertEquals(
            emptyList(),
            dictionary(bakery).getByTags(mapOf("shop" to "supermarket"))
        )
    }

    @Test
    fun find_no_entry_because_wrong_geometry() {
        assertEquals(
            emptyList(),
            dictionary(bakery).getByTags(mapOf("shop" to "bakery"), geometry = RELATION)
        )
    }

    @Test
    fun find_no_entry_because_wrong_language() {
        assertEquals(
            emptyList(),
            dictionary(bakery).getByTags(mapOf("shop" to "bakery"), languages = listOf("it"))
        )
    }

    @Test
    fun find_entry_because_fallback_language() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTags(mapOf("shop" to "bakery"), languages = listOf("it", null))
        )
    }

    @Test
    fun find_entry_by_tags() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTags(mapOf("shop" to "bakery"))
        )
    }

    @Test
    fun find_non_searchable_entry_by_tags() {
        assertEquals(
            listOf(scheisshaus),
            dictionary(scheisshaus).getByTags(mapOf("amenity" to "scheißhaus"))
        )
    }

    @Test
    fun find_entry_by_tags_correct_geometry() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTags(mapOf("shop" to "bakery"), geometry = POINT)
        )
    }

    @Test
    fun find_brand_entry_by_tags() {
        assertEquals(
            listOf(ditsch),
            dictionary(bakery, ditsch)
                .getByTags(mapOf("shop" to "bakery", "name" to "Ditsch"), country = "DE")
        )
    }

    @Test
    fun find_only_entries_with_given_language() {
        val tags = mapOf("shop" to "bakery")
        val dictionary = dictionary(bakery, panetteria)

        assertEquals(
            listOf(panetteria),
            dictionary.getByTags(tags, languages = listOf("it"))
        )
        assertEquals(
            emptyList(),
            dictionary.getByTags(tags, languages = listOf("en"))
        )
        assertEquals(
            listOf(bakery),
            dictionary.getByTags(tags, languages = listOf(null))
        )
    }

    @Test
    fun find_only_brands_finds_no_normal_entries() {
        assertEquals(
            0,
            dictionary(bakery)
                .getByTags(mapOf("shop" to "bakery", "name" to "Ditsch"), isSuggestion = true)
                .size
        )
    }

    @Test
    fun find_no_brands_finds_only_normal_entries() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery, ditsch)
                .getByTags(mapOf("shop" to "bakery", "name" to "Ditsch"), isSuggestion = false)
        )
    }

    @Test
    fun find_multiple_brands_sorts_by_language() {
        assertEquals(
            ditschInternational,
            dictionary(ditschRussian, ditschInternational, ditsch)
                .getByTags(mapOf("shop" to "bakery", "name" to "Ditsch"), languages = listOf(null))
                .first()
        )
    }

    @Test
    fun find_multiple_entries_by_tags() {
        assertEquals(
            2,
            dictionary(bakery, bank).getByTags(mapOf("shop" to "bakery", "amenity" to "bank")).size
        )
    }

    @Test
    fun do_not_find_entry_with_too_specific_tags() {
        assertEquals(
            listOf(car_dealer),
            dictionary(car_dealer, second_hand_car_dealer)
                .getByTags(mapOf("shop" to "car"), languages = listOf("de", null))
        )
    }

    @Test
    fun find_entry_with_specific_tags() {
        assertEquals(
            listOf(second_hand_car_dealer),
            dictionary(car_dealer, second_hand_car_dealer).getByTags(
                tags = mapOf("shop" to "car", "second_hand" to "only"),
                languages = listOf("de", null)
            )
        )
    }

    @Test
    fun find_country_specific_feature_by_tags() {
        val dictionary = dictionary(postbox, postboxUS)

        assertEquals(
            listOf(),
            dictionary.getByTags(mapOf("amenity" to "post_box"), country = null)
        )

        assertEquals(
            listOf(postbox),
            dictionary.getByTags(mapOf("amenity" to "post_box"), country = "DE")
        )

        assertEquals(
            listOf(postboxUS),
            dictionary.getByTags(mapOf("amenity" to "post_box"), country = "US")
        )
    }

    @Test
    fun find_feature_with_wildcard() {
        val dictionary = dictionary(bank, amenity)

        assertEquals(
            listOf(bank),
            dictionary.getByTags(mapOf("amenity" to "bank"))
        )
        assertEquals(
            listOf(amenity),
            dictionary.getByTags(mapOf("amenity" to "blubber"))
        )
    }

    //endregion
    
    //region by name
    
    @Test
    fun find_no_entry_by_name() {
        assertEquals(
            emptyList(),
            dictionary(bakery).getByTerm("Supermarkt").toList()
        )
    }

    @Test
    fun find_no_entry_by_name_because_wrong_geometry() {
        assertEquals(
            emptyList(),
            dictionary(bakery).getByTerm("Bäckerei", geometry = LINE).toList()
        )
    }

    @Test
    fun find_no_entry_by_name_because_wrong_country() {
        val dictionary = dictionary(ditsch, ditschRussian)
        assertEquals(
            emptyList(),
            dictionary.getByTerm("Ditsch").toList()
        )
        assertEquals(
            emptyList(),
            dictionary.getByTerm("Ditsch", country = "FR").toList()
        ) // not in France
        assertEquals(
            emptyList(),
            dictionary.getByTerm("Ditsch", country = "AT-9").toList()
        ) // in all of AT but not Vienna
        assertEquals(
            emptyList(),
            dictionary.getByTerm("Дитсч", country = "UA").toList()
        ) // only on the Krim
    }

    @Test
    fun find_no_non_searchable_entry_by_name() {
        assertEquals(
            emptyList(),
            dictionary(scheisshaus).getByTerm("Scheißhaus").toList()
        )
    }

    @Test
    fun find_entry_by_name() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTerm("Bäckerei", languages = listOf(null)).toList()
        )
    }

    @Test
    fun find_entry_by_name_with_correct_geometry() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery)
                .getByTerm("Bäckerei", languages = listOf(null), geometry = POINT).toList()
        )
    }

    @Test
    fun find_entry_by_name_with_correct_country() {
        val dictionary = dictionary(ditsch, ditschRussian, bakery)
        assertEquals(listOf(ditsch), dictionary.getByTerm("Ditsch", country = "DE").toList())
        assertEquals(listOf(ditsch), dictionary.getByTerm("Ditsch", country = "DE-TH").toList())
        assertEquals(listOf(ditsch), dictionary.getByTerm("Ditsch", country = "AT").toList())
        assertEquals(listOf(ditsch), dictionary.getByTerm("Ditsch", country = "AT-5").toList())
        assertEquals(listOf(ditschRussian), dictionary.getByTerm("Дитсч", country = "UA-43").toList())
        assertEquals(listOf(ditschRussian), dictionary.getByTerm("Дитсч", country = "RU-KHA").toList())
    }

    @Test
    fun find_entry_by_name_case_insensitive() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTerm("BÄCkErEI", languages = listOf(null)).toList()
        )
    }

    @Test
    fun find_entry_by_name_diacritics_insensitive() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTerm("Backérèi", languages = listOf(null)).toList()
        )
    }

    @Test
    fun find_entry_by_term() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTerm("bro", languages = listOf(null)).toList()
        )
    }

    @Test
    fun find_entry_by_term_brackets() {
        val dictionary = dictionary(liquor_store)
        assertEquals(
            listOf(liquor_store),
            dictionary.getByTerm("Alcohol", languages = listOf("en-GB")).toList()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.getByTerm("Off licence (Alcohol Shop)", languages = listOf("en-GB")).toList()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.getByTerm("Off Licence", languages = listOf("en-GB")).toList()
        )
        assertEquals(
            listOf(liquor_store),
            dictionary.getByTerm("Off Licence (Alco", languages = listOf("en-GB")).toList()
        )
    }

    @Test
    fun find_entry_by_term_case_insensitive() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTerm("BRO", languages = listOf(null)).toList()
        )
    }

    @Test
    fun find_entry_by_term_diacritics_insensitive() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTerm("bró", languages = listOf(null)).toList()
        )
    }

    @Test
    fun find_multiple_entries_by_term() {
        assertEquals(
            setOf(second_hand_car_dealer, car_dealer),
            dictionary(second_hand_car_dealer, car_dealer)
                .getByTerm("auto", languages = listOf("de"))
                .toSet()
        )
    }

    @Test
    fun find_only_brands_by_name_finds_no_normal_entries() {
        assertEquals(
            0,
            dictionary(bakery)
                .getByTerm("Bäckerei", languages = listOf(null), isSuggestion = true)
                .count()
        )
    }

    @Test
    fun find_no_brands_by_name_finds_only_normal_entries() {
        assertEquals(
            listOf(bank),
            dictionary(bank, bank_of_america)
                .getByTerm("Bank", languages = listOf(null), isSuggestion = false)
                .toList()
        )
    }

    @Test
    fun find_no_entry_by_term_because_wrong_language() {
        assertEquals(
            emptyList(),
            dictionary(bakery).getByTerm("Bäck", languages = listOf("it")).toList()
        )
    }

    @Test
    fun find_entry_by_term_because_fallback_language() {
        assertEquals(
            listOf(bakery),
            dictionary(bakery).getByTerm("Bäck", languages = listOf("it", null)).toList()
        )
    }

    @Test
    fun find_multi_word_brand_feature() {
        val dictionary = dictionary(deutsche_bank, seven_eleven, seven_eleven_jp, ms_food)
        assertEquals(listOf(deutsche_bank), dictionary.getByTerm("Deutsche Ba").toList())
        assertEquals(listOf(deutsche_bank), dictionary.getByTerm("Deut").toList())
        assertEquals(listOf(deutsche_bank), dictionary.getByTerm("Bank").toList())

        // if we use other languages in search than just the english name, as discussed in
        // https://github.com/westnordost/osmfeatures/issues/29, we might want to adjust the tests
        // below to properly reflect what would be happening
        assertEquals(listOf(seven_eleven), dictionary.getByTerm("7").toList())
        assertEquals(listOf(seven_eleven), dictionary.getByTerm("Eleven").toList())

        assertEquals(listOf(seven_eleven_jp), dictionary.getByTerm("セ").toList())
        assertEquals(listOf(seven_eleven_jp), dictionary.getByTerm("イレブ").toList())

        assertEquals(listOf(ms_food), dictionary.getByTerm("M").toList())
        assertEquals(listOf(ms_food), dictionary.getByTerm("M&S").toList())

        // don't search sub-strings, only starts of words
        assertEquals(0, dictionary.getByTerm("sche").count())
        assertEquals(0, dictionary.getByTerm("hop").count())
    }

    @Test
    fun find_multi_word_feature() {
        val dictionary = dictionary(miniature_train_shop)
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.getByTerm("mini", languages = listOf(null)).toList()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.getByTerm("train", languages = listOf(null)).toList()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.getByTerm("shop", languages = listOf(null)).toList()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.getByTerm("Miniature Trai", languages = listOf(null)).toList()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.getByTerm("Miniature Train Shop", languages = listOf(null)).toList()
        )
        assertEquals(
            listOf(miniature_train_shop),
            dictionary.getByTerm("Train Sho", languages = listOf(null)).toList()
        )
    }

    @Test
    fun find_entry_by_tag_value() {
        assertEquals(
            listOf(panetteria),
            dictionary(panetteria).getByTerm("bakery", languages = listOf("it")).toList()
        )
    }

    @Test
    fun find_country_specific_feature_by_term() {
        val dictionary = dictionary(postbox, postboxUS)

        assertEquals(
            listOf(),
            dictionary.getByTerm("Post", country = null).toList()
        )

        assertEquals(
            listOf(postbox),
            dictionary.getByTerm("Post", country = "DE").toList()
        )

        assertEquals(
            listOf(postboxUS),
            dictionary.getByTerm("Post", country = "US").toList()
        )
    }

    //endregion

    //region by id

    @Test
    fun find_no_entry_by_id() {
        assertNull(dictionary(bakery).getById("amenity/hospital"))
    }

    @Test
    fun find_no_entry_by_id_because_unlocalized_results_are_excluded() {
        assertNull(dictionary(bakery).getById("shop/bakery", languages = listOf("it")))
    }

    @Test
    fun find_entry_by_id() {
        val dictionary = dictionary(bakery)
        assertEquals(bakery, dictionary.getById("shop/bakery"))
        assertEquals(bakery, dictionary.getById("shop/bakery", languages = listOf("zh", null)))
    }

    @Test
    fun find_localized_entry_by_id() {
        val dictionary = dictionary(panetteria)
        assertEquals(
            panetteria,
            dictionary.getById("shop/bakery", languages = listOf("it"))
        )
        assertEquals(
            panetteria,
            dictionary.getById("shop/bakery", languages = listOf("it", null))
        )
    }

    @Test
    fun find_no_entry_by_id_because_wrong_country() {
        val dictionary = dictionary(ditsch)
        assertNull(dictionary.getById("shop/bakery/Ditsch"))
        assertNull(dictionary.getById("shop/bakery/Ditsch", country = "IT"))
        assertNull(dictionary.getById("shop/bakery/Ditsch", country = "AT-9"))
    }

    @Test
    fun find_entry_by_id_in_country() {
        val dictionary = dictionary(ditsch)
        assertEquals(ditsch, dictionary.getById("shop/bakery/Ditsch", country = "AT"))
        assertEquals(ditsch, dictionary.getById("shop/bakery/Ditsch", country = "DE"))
    }

    @Test
    fun find_country_specific_feature_by_id() {
        val dictionary = dictionary(postbox, postboxUS)

        assertEquals(
            postbox,
            dictionary.getById("amenity/post_box", country = "DE")
        )
        assertEquals(
            null,
            dictionary.getById("amenity/post_box", country = "US")
        )

        assertEquals(
            postboxUS,
            dictionary.getById("amenity/post_box/post_box-US", country = "US")
        )
        assertEquals(
            null,
            dictionary.getById("amenity/post_box/post_box-US", country = "DE")
        )

        assertEquals(
            null,
            dictionary.getById("amenity/post_box", country = null)
        )
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
                deutsche_bank, // starts-with in second word matches
                bench,  // found word in terms - higher matchScore
                stock_exchange // found word in terms - lower matchScore
                // casino,       // not included: "Spielbank" does not start with "bank"
            ),
            dictionary.getByTerm("Bank", languages = listOf(null)).toList()
        )
    }

    @Test
    fun issue19() {
        val lush = feature(
            id = "shop/cosmetics/lush-a08666",
            tags = mapOf("brand:wikidata" to "Q1585448", "shop" to "cosmetics"),
            geometries = listOf(POINT, AREA),
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

        val getByTags = dictionary.getByTags(
            tags = mapOf("brand:wikidata" to "Q1585448", "shop" to "cosmetics"),
            languages = listOf("de", null),
            country = "DE"
        )
        assertEquals(1, getByTags.size)
        assertEquals(lush, getByTags[0])

        val getByTerm = dictionary.getByTerm("Lush", languages = listOf("de", null), country = "DE")
            
        assertEquals(1, getByTerm.count())
        assertEquals(lush, getByTerm.first())

        val getById = dictionary
            .getById("shop/cosmetics/lush-a08666", languages = listOf("de", null), country = "DE")
        assertEquals(lush, getById)
    }

    @Test
    fun some_tests_with_real_data() {
        val featureCollection = IDLocalizedFeatureCollection(LivePresetDataAccessAdapter())
        featureCollection.getAll(listOf("en"))

        val dictionary = FeatureDictionary(featureCollection, null)

        val matches = dictionary.getByTags(mapOf("amenity" to "studio"), languages = listOf("en"))
        assertEquals(1, matches.size)
        assertEquals("Studio", matches[0].name)

        val matches2 = dictionary
            .getByTags(mapOf("amenity" to "studio", "studio" to "audio"), languages = listOf("en"))
        assertEquals(1, matches2.size)
        assertEquals("Recording Studio", matches2[0].name)

        val matches3 = dictionary.getByTerm("Chinese Res", languages = listOf("en"))
            
        assertEquals(1, matches3.count())
        assertEquals("Chinese Restaurant", matches3.first().name)

        assertEquals(
            "amenity/post_box",
            dictionary.getByTags(
                tags = mapOf("amenity" to "post_box"),
                languages = listOf("en"),
                country = "DE"
            ).single().id
        )
        assertEquals(
            "amenity/post_box/post_box-US",
            dictionary.getByTags(
                tags = mapOf("amenity" to "post_box"),
                languages = listOf("en"),
                country = "US"
            ).single().id
        )
        assertEquals(
            "amenity/post_box",
            dictionary.getByTerm(
                search = "Mail Drop Box",
                languages = listOf("en"),
                country = "DE"
            ).single().id
        )
        assertEquals(
            "amenity/post_box/post_box-US",
            dictionary.getByTerm(
                search = "Mail Drop Box",
                languages = listOf("en"),
                country = "US"
            ).single().id
        )
    }
}

private fun dictionary(vararg entries: Feature) = FeatureDictionary(
    TestLocalizedFeatureCollection(entries.filterNot { it.isSuggestion }),
    TestPerCountryFeatureCollection(entries.filter { it.isSuggestion })
)

private fun feature(
    id: String,
    tags: Map<String, String> = mapOf(),
    geometries: List<GeometryType> = listOf(POINT),
    names: List<String>,
    terms: List<String> = listOf(),
    includeCountryCodes: List<String> = listOf(),
    excludeCountryCodes: List<String> = listOf(),
    searchable: Boolean = true,
    matchScore: Float = 1.0f,
    addTags: Map<String, String> = mapOf(),
    isSuggestion: Boolean = false,
    language: String? = null,
    keys: Set<String> = setOf()
): Feature {
    val f = BaseFeature(
        id = id,
        tags = tags,
        geometry = geometries,
        icon = null,
        imageURL = null,
        names = names,
        terms = terms,
        includeCountryCodes = includeCountryCodes,
        excludeCountryCodes = excludeCountryCodes,
        isSearchable = searchable,
        matchScore = matchScore,
        isSuggestion = isSuggestion,
        addTags = addTags,
        removeTags = mapOf(),
        preserveTags = listOf(),
        tagKeys = keys,
        addTagKeys = setOf(),
        removeTagKeys = setOf()
    )
    return if (language != null) LocalizedFeature(f, language, f.names, f.terms) else f
}