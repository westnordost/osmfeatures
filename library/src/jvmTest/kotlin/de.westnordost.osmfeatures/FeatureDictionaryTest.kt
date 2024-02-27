package de.westnordost.osmfeatures

import kotlin.test.assertEquals
import kotlin.test.Test

class FeatureDictionaryTest {
    @Test
    fun some_tests_with_real_data() {
        val featureCollection = IDLocalizedFeatureCollection(LivePresetDataAccessAdapter())
        featureCollection.getAll(listOf(ENGLISH))

        val dictionary = FeatureDictionary(featureCollection, null)

        val matches = dictionary
            .byTags(mapOf("amenity" to "studio"))
            .forLocale(ENGLISH)
            .find()
        assertEquals(1, matches.size)
        assertEquals("Studio", matches[0].name)

        val matches2 = dictionary
            .byTags(mapOf("amenity" to "studio", "studio" to "audio"))
            .forLocale(ENGLISH)
            .find()
        assertEquals(1, matches2.size)
        assertEquals("Recording Studio", matches2[0].name)

        val matches3 = dictionary
            .byTerm("Chinese Res")
            .forLocale(ENGLISH)
            .find()
        assertEquals(1, matches3.size)
        assertEquals("Chinese Restaurant", matches3[0].name)
    }
}

private val ENGLISH = Locale("en")
