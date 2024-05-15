package de.westnordost.osmfeatures

import kotlin.test.assertEquals
import kotlin.test.Test

class IDBrandPresetsFeatureCollectionTest {
    @Test
    fun load_brands() {
        val c = IDBrandPresetsFeatureCollection(object : ResourceAccessAdapter {
            override fun exists(name: String) = name == "presets.json"
            override fun open(name: String) = resource("brand_presets_min.json")
        })
        assertEquals(
            setOf("Duckworths", "Megamall"),
            c.getAll(listOf(null)).map { it.name }.toSet()
        )
        assertEquals("Duckworths", c.get("a/brand", listOf(null))?.name)
        assertEquals("Megamall", c.get("another/brand", listOf(null))?.name)
        assertEquals(true, c.get("a/brand", listOf(null))?.isSuggestion)
        assertEquals(true, c.get("another/brand", listOf(null))?.isSuggestion)
    }

    @Test
    fun load_brands_by_country() {
        val c = IDBrandPresetsFeatureCollection(object : ResourceAccessAdapter {
            override fun exists(name: String) = name == "presets-DE.json"
            override fun open(name: String) = resource("brand_presets_min2.json")
        })
        assertEquals(
            setOf("Talespin"),
            c.getAll(listOf("DE")).map { it.name }.toSet()
        )
        assertEquals("Talespin", c.get("yet_another/brand", listOf("DE"))?.name)
        assertEquals(true, c.get("yet_another/brand", listOf("DE"))?.isSuggestion)
    }
}
