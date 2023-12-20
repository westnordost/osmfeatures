//import groovy.json.JsonSlurper
//import java.io.File
//import java.net.URL
//import java.util.Locale
//
//plugins {
//    `java`
//}
//
//repositories {
//    mavenCentral()
//    google()
//}
//
//val downloadPresets by tasks.registering {
//    doLast {
//        val targetDir = "$projectDir/presets"
//        val presetsUrl = URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
//        val contentsUrl = URL("https://api.github.com/repos/openstreetmap/id-tagging-schema/contents/dist/translations")
//
//        File("$targetDir/presets.json").outputStream().use { it.write(presetsUrl.openStream().readBytes()) }
//
//        val slurper = JsonSlurper()
//        val contents = slurper.parse(contentsUrl, "UTF-8") as List<Map<String, Any>>
//        contents.forEach {
//            if (it["type"] == "file") {
//                val language = it["name"].toString().substringBeforeLast(".")
//                val translationsUrl = URL(it["download_url"].toString())
//                val javaLanguage = bcp47LanguageTagToJavaLanguageTag(language)
//                File("$targetDir/${javaLanguage}.json").outputStream().use { os ->
//                    os.write(translationsUrl.openStream().readBytes())
//                }
//            }
//        }
//    }
//}
//
//fun bcp47LanguageTagToJavaLanguageTag(bcp47: String): String {
//    val locale = Locale.forLanguageTag(bcp47)
//    var result = locale.language
//    if (locale.country.isNotEmpty()) result += "-" + locale.country
//    return result
//}
