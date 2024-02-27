package de.westnordost.osmfeatures

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.Test
import okio.Source

class IDBrandPresetsFeatureCollectionTest {
    @Test
    fun load_brands() {
        val c = IDBrandPresetsFeatureCollection(object : FileAccessAdapter {
            override fun exists(name: String): Boolean = name == "presets.json"
            override fun open(name: String): Source = getSource("brand_presets_min.json")
        })
        assertEqualsIgnoreOrder(
            listOf("Duckworths", "Megamall"),
            c.getAll(listOf(null)).map { it.name }
        )
        assertEquals("Duckworths", c.get("a/brand", listOf(null))?.name)
        assertEquals("Megamall", c.get("another/brand", listOf(null))?.name)
        assertEquals(true, c.get("a/brand", listOf(null))?.isSuggestion)
        assertEquals(true, c.get("another/brand", listOf(null))?.isSuggestion)
    }

    @Test
    fun load_brands_by_country() {
        val c = IDBrandPresetsFeatureCollection(object : FileAccessAdapter {
            override fun exists(name: String): Boolean = name == "presets-DE.json"
            override fun open(name: String): Source = getSource("brand_presets_min2.json")
        })
        assertEqualsIgnoreOrder(
            listOf("Talespin"),
            c.getAll(listOf("DE")).map { it.name }
        )
        assertEquals("Talespin", c.get("yet_another/brand", listOf("DE"))?.name)
        assertEquals(true, c.get("yet_another/brand", listOf("DE"))?.isSuggestion)
    }
}

private fun getSource(file: String): Source =
    FileSystemAccess("src/commonTest/resources").open(file)
