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

### Find matches by tags
```java
List<Match> matches = dictionary.get(Map.of("amenity", "bench"), null, Locale.GERMAN);

println(matches.get(0).name); // prints "Parkbank" (or something like this)
```

### Find matches by search word

```java
List<Match> matches = dictionary.find("Bank", null, null, 0, Locale.GERMAN);
// result list will have matches with at least amenity=bank and amenity=bench
```
