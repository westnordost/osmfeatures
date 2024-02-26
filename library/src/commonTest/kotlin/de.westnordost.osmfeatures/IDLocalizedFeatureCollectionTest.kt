package de.westnordost.osmfeatures

import okio.FileSystem
import okio.Source
import okio.FileNotFoundException
import okio.IOException
import okio.Path.Companion.toPath
import kotlin.test.*

class IDLocalizedFeatureCollectionTest {
    private val ENGLISH = Locale("en")
    private val GERMAN = Locale("de")

    @Test
    fun features_not_found_produces_runtime_exception() {
        try {
            IDLocalizedFeatureCollection(object : FileAccessAdapter {

                override fun exists(name: String): Boolean {
                    return false
                }

                @Override
                @Throws(IOException::class)
                override fun open(name: String): Source {
                    throw FileNotFoundException()
                }
            })
            fail()
        } catch (ignored: RuntimeException) {
        }
    }

    @Test
    fun load_features_and_two_localizations() {
        val c = IDLocalizedFeatureCollection(object : FileAccessAdapter {
            @Override
            override fun exists(name: String): Boolean {
                return listOf("presets.json", "en.json", "de.json").contains(name)
            }

            @Override
            @Throws(IOException::class)
            override fun open(name: String): Source {
                if (name == "presets.json") return getSource("some_presets_min.json")
                if (name == "en.json") return getSource("localizations_en.json")
                if (name == "de.json") return getSource("localizations_de.json")
                throw IOException("File not found")
            }
        })

        // getting non-localized features
        val notLocalized: List<Locale?> = listOf(null as Locale?)
        val notLocalizedFeatures: Collection<Feature> = c.getAll(notLocalized)
        assertEqualsIgnoreOrder(listOf("test", "test", "test"), getNames(notLocalizedFeatures))
        assertEquals("test", c["some/id", notLocalized]?.name)
        assertEquals("test", c["another/id", notLocalized]?.name)
        assertEquals("test", c["yet/another/id", notLocalized]?.name)

        // getting English features
        val english: List<Locale> = listOf(ENGLISH)
        val englishFeatures: Collection<Feature> = c.getAll(english)
        assertEqualsIgnoreOrder(listOf("Bakery"), getNames(englishFeatures))
        assertEquals("Bakery", c["some/id", english]?.name)
        assertNull(c["another/id", english])
        assertNull(c["yet/another/id", english])

        // getting Germany features
        // this also tests if the fallback from de-DE to de works if de-DE.json does not exist
        val germany: List<Locale> = listOf(GERMANY)
        val germanyFeatures: Collection<Feature> = c.getAll(germany)
        assertEqualsIgnoreOrder(listOf("Bäckerei", "Gullideckel"), getNames(germanyFeatures))
        assertEquals("Bäckerei", c["some/id", germany]?.name)
        assertEquals("Gullideckel", c["another/id", germany]?.name)
        assertNull(c["yet/another/id", germany])

        // getting features through fallback chain
        val locales: List<Locale?> = listOf(ENGLISH, GERMANY, null)
        val fallbackFeatures: Collection<Feature> = c.getAll(locales)
        assertEqualsIgnoreOrder(listOf("Bakery", "Gullideckel", "test"), getNames(fallbackFeatures))
        assertEquals("Bakery", c["some/id", locales]?.name)
        assertEquals("Gullideckel", c["another/id", locales]?.name)
        assertEquals("test", c["yet/another/id", locales]?.name)
        assertEquals(ENGLISH, c["some/id", locales]?.locale)
        assertEquals(GERMAN, c["another/id", locales]?.locale)
        assertNull(c["yet/another/id", locales]?.locale)
    }

    @Test
    fun load_features_and_merge_localizations() {
        val c = IDLocalizedFeatureCollection(object : FileAccessAdapter {
            @Override
            override fun exists(name: String): Boolean {
                return listOf(
                    "presets.json",
                    "de-AT.json",
                    "de.json",
                    "de-Cyrl.json",
                    "de-Cyrl-AT.json"
                ).contains(name)
            }

            @Override
            @Throws(IOException::class)
            override fun open(name: String): Source {
                if (name == "presets.json") return getSource("some_presets_min.json")
                if (name == "de-AT.json") return getSource("localizations_de-AT.json")
                if (name == "de.json") return getSource("localizations_de.json")
                if (name == "de-Cyrl-AT.json") return getSource("localizations_de-Cyrl-AT.json")
                if (name == "de-Cyrl.json") return getSource("localizations_de-Cyrl.json")
                throw IOException("File not found")
            }
        })

        // standard case - no merging
        val german: List<Locale> = listOf(GERMAN)
        val germanFeatures: Collection<Feature> = c.getAll(german)
        assertEqualsIgnoreOrder(listOf("Bäckerei", "Gullideckel"), getNames(germanFeatures))
        assertEquals("Bäckerei", c["some/id", german]?.name)
        assertEquals("Gullideckel", c["another/id", german]?.name)
        assertNull(c["yet/another/id", german])

        // merging de-AT and de
        val austria: List<Locale> = listOf(Locale("de", "AT"))
        val austrianFeatures: Collection<Feature> = c.getAll(austria)
        assertEqualsIgnoreOrder(
            listOf("Backhusl", "Gullideckel", "Brückle"),
            getNames(austrianFeatures)
        )
        assertEquals("Backhusl", c["some/id", austria]?.name)
        assertEquals("Gullideckel", c["another/id", austria]?.name)
        assertEquals("Brückle", c["yet/another/id", austria]?.name)

        // merging scripts
        val cryllic = listOf(Locale("de", null, "Cyrl"))
        assertEquals("бацкхаус", c["some/id", cryllic]?.name)
        val cryllicAustria = listOf(Locale("de", "AT","Cyrl"))
        assertEquals("бацкхусл", c["some/id", cryllicAustria]?.name)
    }

    @kotlin.Throws(IOException::class)
    private fun getSource(file: String): Source {
        val resourcePath = "src/commonTest/resources/${file}".toPath()
        return FileSystem.SYSTEM.source(resourcePath)
    }

    companion object {
        private fun getNames(features: Collection<Feature>): Collection<String> {
            return features.map { it.name }
        }
    }
}
