package de.westnordost.osmfeatures

import kotlin.test.Test
import okio.source
import kotlin.test.assertTrue
import java.net.URL

class IDPresetsJsonParserTest {
    @Test
    fun parse_some_real_data() {
        val url = URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
        val features = IDPresetsJsonParser().parse(url.openStream().source())
        // should not crash etc
        assertTrue(features.size > 1000)
    }
}
