package osmfeatures

import de.westnordost.osmfeatures.Feature
import de.westnordost.osmfeatures.IDBrandPresetsFeatureCollection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import de.westnordost.osmfeatures.TestUtils.assertEqualsIgnoreOrder
import de.westnordost.osmfeatures.TestUtils.listOf
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Source
import okio.source
import org.junit.Test
import java.io.IOException
import java.util.ArrayList

class IDBrandPresetsFeatureCollectionTest {
    @Test
    fun load_brands() {
        val c = IDBrandPresetsFeatureCollection(object : FileAccessAdapter {
            @Override
            override fun exists(name: String): Boolean {
                return name == "presets.json"
            }

            @Override
            @Throws(IOException::class)
            override fun open(name: String): Source {
                if (name == "presets.json") return getSource("brand_presets_min.json")
                throw IOException("wrong file name")
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
            @Override
            override fun exists(name: String): Boolean {
                return name == "presets-DE.json"
            }

            @Override
            @Throws(IOException::class)
            override fun open(name: String): Source {
                if (name == "presets-DE.json") return getSource("brand_presets_min2.json")
                throw IOException("File not found")
            }
        })
        assertEqualsIgnoreOrder(listOf("Talespin"), getNames(c.getAll(listOf("DE"))))
        assertEquals("Talespin", c["yet_another/brand", listOf("DE")]?.name)
        c["yet_another/brand", listOf("DE")]?.isSuggestion?.let { assertTrue(it) }
    }

    @kotlin.Throws(IOException::class)
    private fun getSource(file: String): Source {
        val resourceStream = this.javaClass.getResourceAsStream(file)
            ?: throw IOException("Could not retrieve file $file in resource assets")
        return resourceStream.source()
    }

    companion object {
        private fun getNames(features: Collection<Feature>): Collection<String> {
           return features.map { it.name }
        }
    }
}
