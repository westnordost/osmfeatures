package de.westnordost.osmfeatures

import kotlin.test.Test
import okio.IOException
import okio.source
import kotlin.test.assertTrue
import java.net.URL

class IDPresetsJsonParserJVMTest {
    @Test
    @Throws(IOException::class)
    fun parse_some_real_data() {
        val url =
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
        val features: List<BaseFeature> = IDPresetsJsonParser().parse(url.openStream().source())
        // should not crash etc
        assertTrue(features.size > 1000)
    }
}
