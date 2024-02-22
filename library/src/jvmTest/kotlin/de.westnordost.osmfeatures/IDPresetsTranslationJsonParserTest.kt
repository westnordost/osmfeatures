package de.westnordost.osmfeatures

import okio.IOException
import java.net.URISyntaxException
import java.net.URL
import okio.source
import kotlin.test.assertTrue
import kotlin.test.Test

class IDPresetsTranslationJsonParserJVMTest {
    @Test
    @Throws(IOException::class, URISyntaxException::class)
    fun parse_some_real_data() {
        val url =
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
        val features: List<BaseFeature> =
            IDPresetsJsonParser().parse(url.openConnection().getInputStream().source())
        val featureMap = HashMap(features.associateBy { it.id })
        val rawTranslationsURL =
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/de.json")
        val translatedFeatures: List<LocalizedFeature> = IDPresetsTranslationJsonParser().parse(
            rawTranslationsURL.openStream().source(),
            Locale.GERMAN,
            featureMap
        )

        // should not crash etc
        assertTrue(translatedFeatures.size > 1000)
    }
}
