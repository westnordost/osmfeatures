# osmnames

Java and Android library to translate OSM tags to and from localized names.

Requires Java 8.

## Copyright and License

© 2019 Tobias Zwick. This library is released under the terms of the [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

## Installation

Add [`de.westnordost:osmnames:1.0`](https://maven-repository.com/artifact/de.westnordost/osmnames/1.0) as a Maven dependency or download the jar from there.

For Android, use [`de.westnordost:osmnames-android:1.0`](https://maven-repository.com/artifact/de.westnordost/osmnames-android/1.0).

## Usage

Point the dictionary to the directory where the data is located, use `NamesDictionary` as a singleton.
```java
NamesDictionary dictionary = NamesDictionary.create("path/to/data/"));
```

For Android, use
```java
NamesDictionary dictionary = AndroidNamesDictionary.create("path/within/assets/folder/to/data"));
```

TODO note about how/where to get the data.

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
