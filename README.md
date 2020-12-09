# osmfeatures

A dictionary of OSM map features, accessible by terms and by tags, for Java and Android.

Requires Java 8.

## Copyright and License

© 2019-2020 Tobias Zwick. This library is released under the terms of the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).

## Installation

Add [`de.westnordost:osmfeatures:2.0`](https://jcenter.bintray.com/de/westnordost/osmfeatures/2.0/) as a Maven dependency or download the jar from there.

For Android, use [`de.westnordost:osmfeatures-android:2.0`](https://jcenter.bintray.com/de/westnordost/osmfeatures-android/2.0/).

It's in the JCenter repository, not Maven Central.

## Usage

### Get the data

The data for the dictionary is not maintained in this repository.
It actually uses the [preset data from iD](https://github.com/openstreetmap/id-tagging-schema/blob/main/dist/presets.json) and [the translations of the presets](https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations).
Just dump all the translations and the presets.json into the same directory.
Do not forget to give attribution to iD since you are using their data.

If you use gradle as your build tool, The easiest way to get this data is to put this task into your `build.gradle` and either execute this task manually from time to time or make the build process depend on it (by adding `preBuild.dependsOn(downloadPresets)`):

```groovy
import groovy.json.JsonSlurper

task downloadPresets {
    doLast {
        def targetDir = "$projectDir/path/to/where/the/data/should/go"
        def presetsUrl = new URL("https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/main/dist/presets.json")
        def contentsUrl = new URL("https://api.github.com/repos/openstreetmap/id-tagging-schema/contents/dist/translations")

        new File("$targetDir/presets.json").withOutputStream { it << presetsUrl.openStream() }

        def slurper = new JsonSlurper()
        slurper.parse(contentsUrl, "UTF-8").each {
            if(it.type == "file") {
                def language = it.name.substring(0, it.name.lastIndexOf("."))
                def translationsUrl = new URL(it.download_url)
                def javaLanguage = bcp47LanguageTagToJavaLanguageTag(language)
                new File("$targetDir/${javaLanguage}.json").withOutputStream { it << translationsUrl.openStream() }
            }
        }
    }
}

// Java (and thus also Android) uses some old iso (language) codes. F.e. id -> in etc.
// so the localized files also need to use the old iso codes
static def bcp47LanguageTagToJavaLanguageTag(String bcp47) {
    def locale = Locale.forLanguageTag(bcp47)
    def result = locale.language
    if (!locale.country.isEmpty()) result += "-" + locale.country
    return result
}
```

### Initialize dictionary

Point the dictionary to the directory where the data is located (see above). Use `FeatureDictionary` as a singleton.
```java
FeatureDictionary dictionary = FeatureDictionary.create("path/to/data"));
```

For Android, use
```java
FeatureDictionary dictionary = AndroidFeatureDictionary.create("path/within/assets/folder/to/data"));
```

### Find matches by tags
```java
List<Feature> matches = dictionary
    .byTags(Map.of("amenity", "bench"))  // look for features that have the given tags
    .forGeometry(GeometryType.POINT)     // limit the search to features that may be points
    .forLocale(Locale.GERMAN)            // show results in German
    .find();

println(matches.get(0).getName()); // prints "Parkbank" (or something like this)
```

### Find matches by search word

```java
List<Feature> matches = dictionary
    .byTerm("Bank")                  // look for features matching "Bank"
    .forGeometry(GeometryType.AREA)  // limit the search to features that may be areas
    .forLocale(Locale.GERMAN)        // show results in German
    .inCountry("DE")                 // also include things (brands) that only exist in Germany
    .limit(10)                       // return at most 10 entries
    .find();
// result list will have matches with at least amenity=bank, but not amenity=bench because it is a point-feature
```
