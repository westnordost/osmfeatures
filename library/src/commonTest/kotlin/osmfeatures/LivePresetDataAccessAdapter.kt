package osmfeatures

import java.io.IOException
import java.net.URL
import osmfeatures.TestUtils.listOf
import okio.source

class LivePresetDataAccessAdapter : FileAccessAdapter {
    @Override
    override fun exists(name: String): Boolean {
        return listOf("presets.json", "de.json", "en.json", "en-GB.json").contains(name)
    }

    @Override
    @kotlin.Throws(IOException::class)
    override fun open(name: String): okio.Source {
        val url: URL = if (name == "presets.json") {
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
        } else {
            URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/$name")
        }
        return url.openStream().source()
    }
}
