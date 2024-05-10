# osmfeatures

A Kotlin multiplatform dictionary of OSM map features, accessible by terms and by tags. Supported platforms are Android, JVM and iOS.

Due to heavy use of indices, it is very fast.

It is currently used in [StreetComplete](https://github.com/streetcomplete/streetcomplete).

## Copyright and License

© 2019-2024 Tobias Zwick. This library is released under the terms of the Apache License Version 2.0.

## Usage

Add [de.westnordost:osmfeatures:6.0.0](https://mvnrepository.com/artifact/de.westnordost/osmfeatures/5.2) as a Maven dependency or download the jar from there.

### Get the data

The data for the dictionary is not maintained in this repository.
It actually uses the [preset data from iD](https://github.com/openstreetmap/id-tagging-schema/blob/main/dist/presets.json),  [its translations](https://github.com/openstreetmap/id-tagging-schema/tree/main/dist/translations)
and optionally additionally the brand preset data from the [name suggestion index](https://github.com/osmlab/name-suggestion-index). 
Each are &copy; iD contributors, licensed under the ISC license.


So, just dump all the translations and the presets.json into the same directory. To be always 
up-to-date, it is advisable to have an automatic build task that fetches the current version of the 
presets from the repository.

The app for which this library was developed (StreetComplete), uses the following tasks:
- [UpdatePresetsTask.kt](https://github.com/streetcomplete/StreetComplete/blob/master/buildSrc/src/main/java/UpdatePresetsTask.kt) to download presets and selected translations
- [UpdateNsiPresetsTask.kt](https://github.com/streetcomplete/StreetComplete/blob/master/buildSrc/src/main/java/UpdateNsiPresetsTask.kt) to download the brand presets from the [name suggestion index](https://github.com/osmlab/name-suggestion-index)

### Initialize dictionary

Point the dictionary to the directory where the data is located (see above). Use `FeatureDictionary` as a singleton, as initialization takes a moment (loading files, building indices).
```kotlin
val dictionary = FeatureDictionary.create(fileSystem, "path/to/data")
```

For Android, use
```kotlin
val dictionary = FeatureDictionary.create(assetManager, "path/within/assets/folder/to/data")
```

If brand features from the [name suggestion index](https://github.com/osmlab/name-suggestion-index) should be included in the dictionary, you can specify the path to these presets as a third parameter. These will be loaded on-demand depending on for which countries you search for.

### Find matches by tags

```kotlin
val matches: List<Feature> = dictionary
    .byTags(mapOf("amenity" to "bench")) // look for features that have the given tags
    .forGeometry(GeometryType.POINT)     // limit the search to features that may be points
    .forLocale("de")                     // show results in German only, don't fall back to English or unlocalized results
    .find()

// prints "Parkbank" (or something like this)
// or null if no preset for amenity=bench exists that is localized to German
println(matches[0]?.getName())
```

### Find matches by search word

```kotlin
val matches: List<Feature> = dictionary
    .byTerm("Bank")                  // look for features matching "Bank"
    .forGeometry(GeometryType.AREA)  // limit the search to features that may be areas
    .forLocale("de", null)           // show results in German or fall back to unlocalized results
                                     // (brand features are usually not localized)
    .inCountry("DE")                 // also include things (brands) that only exist in Germany
    .limit(10)                       // return at most 10 entries
    .find()
// result list will have matches with at least amenity=bank, but not amenity=bench because it is a point-feature
// if the dictionary contains also brand presets, e.g. "Deutsche Bank" will certainly also be amongst the results
```

### Find by id

```kotlin
val match: Feature? = dictionary
    .byId("amenity/bank")
    .forLocale("de", "en-US", null)  // show results in German, otherwise fall back to American 
                                     // English or otherwise unlocalized results
    .inCountry("DE")                 // also include things (brands) that only exist in Germany
    .get()
```
