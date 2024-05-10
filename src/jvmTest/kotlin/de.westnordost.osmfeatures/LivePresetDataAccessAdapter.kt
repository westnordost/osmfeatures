package de.westnordost.osmfeatures

import java.net.URL
import okio.source

class LivePresetDataAccessAdapter : FileAccessAdapter {

    override fun exists(name: String): Boolean =
        name in listOf("presets.json", "de.json", "en.json", "en-GB.json")

    override fun open(name: String): okio.Source {
        val url = URL(when(name) {
            "presets.json" -> "https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json"
            else -> "https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/translations/$name"
        })
        return url.openStream().source()
    }
}
