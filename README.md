# osmfeatures

A dictionary of OSM map features, accessible by terms and by tags, for Java and Android.

Requires Java 8.

## Copyright and License

© 2019 Tobias Zwick. This library is released under the terms of the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

## Installation

Add [`de.westnordost:osmfeatures:1.0`](http://jcenter.bintray.com/de/westnordost/osmfeatures/1.0/) as a Maven dependency or download the jar from there.

For Android, use [`de.westnordost:osmfeatures-android:1.0`](http://jcenter.bintray.com/de/westnordost/osmfeatures-android/1.0/).

It's in the JCenter repository, not Maven Central.

## Usage

### Get the data

The data for the dictionary is not maintained in this repository. It actually uses the [preset data from iD](https://github.com/openstreetmap/iD/blob/master/data/presets/presets.json) and [the translations of the presets](https://github.com/openstreetmap/iD/tree/master/dist/locales). The former can just be copied, the latter must be fished out of the global localization files for iD.
Do not forget to give attribution to iD since you are using their data.

If you use gradle as your build tool, The easiest way to get this data is to put this task into your `build.gradle` and either execute this task manually from time to time or make the build process depend on it (by adding `preBuild.dependsOn(downloadPresets)`):

```groovy
task downloadPresets {
    doLast {
        def targetDir = "path/to/data" // <- the relative path to the directory where the data should go
        def presetsUrl = new URL("https://raw.githubusercontent.com/openstreetmap/iD/master/data/presets/presets.json")
        def contentsUrl = new URL("https://api.github.com/repos/openstreetmap/iD/contents/dist/locales")

        new File("$targetDir/presets.json").withOutputStream { it << presetsUrl.openStream() }

        def slurper = new JsonSlurper()
        slurper.parse(contentsUrl, "UTF-8").each {
            if(it.type == "file") {
                def content = slurper.parse(new URL(it.download_url),"UTF-8")
                def presets = content.values()[0]?.presets?.presets
                if(presets) {
                    def json = JsonOutput.prettyPrint(JsonOutput.toJson([presets: presets]))
                    new File("$targetDir/${it.name}").write(json, "UTF-8")
                }
            }
        }
    }
}
```

### Initialize dictionary

Point the dictionary to the directory where the data is located (see above). Use `FeaturesDictionary` as a singleton.
```java
FeatureDictionary dictionary = FeatureDictionary.create("path/to/data"));
```

For Android, use
```java
FeatureDictionary dictionary = AndroidFeatureDictionary.create("path/within/assets/folder/to/data"));
```

### Find matches by tags
```java
List<Match> matches = dictionary
    .byTags(Map.of("amenity", "bench"))  // look for features that have the given tags
    .forGeometry(GeometryType.POINT)     // limit the search to features that may be points
    .forLocale(Locale.GERMAN)            // show results in German
    .find();

println(matches.get(0).name); // prints "Parkbank" (or something like this)
```

### Find matches by search word

```java
List<Match> matches = dictionary
    .byTerm("Bank")                  // look for features matching "Bank"
    .forGeometry(GeometryType.AREA)  // limit the search to features that may be areas
    .forLocale(Locale.GERMAN)        // show results in German
    .inCountry("DE")                 // also include things (brands) that only exist in Germany
    .limit(10)                       // return at most 10 entries
    .find();
// result list will have matches with at least amenity=bank, but not amenity=bench because it is a point-feature
```
