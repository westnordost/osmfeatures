package de.westnordost.osmfeatures

import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder
import okio.Source

class IDBrandPresetsFeatureCollectionTest {
    @Test
    fun load_brands() {
        val c = IDBrandPresetsFeatureCollection(object : FileAccessAdapter {
            override fun exists(name: String): Boolean {
                return name == "presets.json"
            }

            override fun open(name: String): Source {
                return getSource("brand_presets_min.json")
            }
        })
        assertEqualsIgnoreOrder(
            listOf("Duckworths", "Megamall"),
            getNames(c.getAll(listOf(null as String?)))
        )
        assertEquals("Duckworths", c["a/brand", listOf(null as String?)]?.name)
        assertEquals("Megamall", c["another/brand", listOf(null as String?)]?.name)
    }

    @Test
    fun load_brands_by_country() {
        val c = IDBrandPresetsFeatureCollection(object : FileAccessAdapter {

            override fun exists(name: String): Boolean {
                return name == "presets-DE.json"
            }

            override fun open(name: String): Source {
                return getSource("brand_presets_min2.json")
            }
        })
        assertEqualsIgnoreOrder(listOf("Talespin"), getNames(c.getAll(listOf("DE"))))
        assertEquals("Talespin", c["yet_another/brand", listOf("DE")]?.name)
        c["yet_another/brand", listOf("DE")]?.isSuggestion?.let { assertTrue(it) }
    }

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
