# osmfeatures

A dictionary of OSM map features, accessible by terms and by tags, for Java and Android.

Requires Java 8.

## Copyright and License

© 2019-2022 Tobias Zwick. This library is released under the terms of the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

## Installation

Add [`de.westnordost:osmfeatures:5.2`](https://mvnrepository.com/artifact/de.westnordost/osmfeatures/5.2) as a Maven dependency or download the jar from there.

For Android, use [`de.westnordost:osmfeatures-android:5.2`](https://mvnrepository.com/artifact/de.westnordost/osmfeatures-android/5.2).

## Usage

### Get the data

The data for the dictionary is not maintained in this repository.
It actually uses the [preset data from iD](https://github.com/openstreetmap/id-tagging-schema/blob/main/dist/presets.json) and [the translations of the presets](https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations).
Just dump all the translations and the presets.json into the same directory.
Do not forget to give attribution to iD since you are using their data.

If you use gradle as your build tool, the easiest way to get this data is to put this task into your `build.gradle` and either execute this task manually from time to time or make the build process depend on it (by adding `preBuild.dependsOn(downloadPresets)`):

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
                def filename = it.name.substring(0, it.name.indexOf("."))
                def javaLanguageTag = Locale.forLanguageTag(filename.replace('@','-')).toLanguageTag()
                def translationsUrl = new URL(it.download_url)
                new File("$targetDir/${javaLanguageTag}.json").withOutputStream { it << translationsUrl.openStream() }
            }
        }
    }
}
```

In StreetComplete (app that uses this library), there is [UpdatePresetsTask.kt](https://github.com/streetcomplete/StreetComplete/blob/master/buildSrc/src/main/java/UpdatePresetsTask.kt) to do this. It's longer but it is maintained and takes care of some more edge cases. Also, take note of [UpdateNsiPresetsTask.kt](https://github.com/streetcomplete/StreetComplete/blob/master/buildSrc/src/main/java/UpdateNsiPresetsTask.kt) which fetches the [name suggestion index presets](https://github.com/osmlab/name-suggestion-index) (=brands).

### Initialize dictionary

Point the dictionary to the directory where the data is located (see above). Use `FeatureDictionary` as a singleton.
```java
FeatureDictionary dictionary = FeatureDictionary.create("path/to/data"));
```

For Android, use
```java
FeatureDictionary dictionary = AndroidFeatureDictionary.create(assetManager, "path/within/assets/folder/to/data"));
```

If brand features from the [name suggestion index](https://github.com/osmlab/name-suggestion-index) (see last paragraph in the previous "Get the data" section) should be included in the dictionary, you can specify the path to these presets as a further parameter. These will be loaded on-demand depending on for which countries you search for.

### Find matches by tags
```java
List<Feature> matches = dictionary
    .byTags(Map.of("amenity", "bench"))  // look for features that have the given tags
    .forGeometry(GeometryType.POINT)     // limit the search to features that may be points
    .forLocale(Locale.GERMAN)            // show results in German only, don't fall back to English or unlocalized results
    .find();

// prints "Parkbank" (or something like this) or index out of bounds exception
// if no preset for amenity=bench exists that is localized to German
println(matches.get(0).getName()); 
```

### Find matches by search word

```java
List<Feature> matches = dictionary
    .byTerm("Bank")                  // look for features matching "Bank"
    .forGeometry(GeometryType.AREA)  // limit the search to features that may be areas
    .forLocale(Locale.GERMAN, null)  // show results in German or fall back to unlocalized results
                                     // (brand features are usually not localized)
    .inCountry("DE")                 // also include things (brands) that only exist in Germany
    .limit(10)                       // return at most 10 entries
    .find();
// result list will have matches with at least amenity=bank, but not amenity=bench because it is a point-feature
```

### Find by id

```java
Feature feature = dictionary
    .byId("amenity/bank")
    .forLocale(Locale.GERMAN,         // show results in German, otherwise fall back to English etc.
               Locale.ENGLISH,
               null)
    .inCountry("DE")                 // also include things (brands) that only exist in Germany
    .get();
```
