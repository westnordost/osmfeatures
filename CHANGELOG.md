# Changelog

# 5.2

- Add support placeholders for preset names (breaking change in [v5.0.0](https://github.com/ideditor/schema-builder/blob/main/CHANGELOG.md#510) of iD presets schema)
- When searching by term, include results that match with the tag value at the bottom of the result list. (iD has the same behavior)

## 5.1

- add property `boolean isSuggestion` to `Feature` to be able to tell if a feature is a brand feature or not
- add method to get a feature by its id to `FeatureDictionary`

## 5.0

Added support for aliases of presets. They are treated similarily as alternative names.

## 4.1

Added support for scripts.

E.g. there can be localization of presets in "bg" (Bulgarian) and also "bg-Cyrl" (Bulgarian in Cryllic).

## 4.0

Brand features are becoming too big.

So now, brand features are loaded lazily per-country. I.e. additionally to a `presets.json`, there can be `presets-US.json` (using ISO 3166-1 alpha2 codes) and even  `presets-US-NY.json` (ISO 3166-2) in the same directory, which will be loaded on demand. This functionality is used in StreetComplete.

## 3.0

Support for NSI brand names (v6.x). Pass the path to the NSI presets as second parameter to the `create` function.

Note that the NSI presets are actually not on the root level of https://github.com/osmlab/name-suggestion-index/blob/main/dist/presets/nsi-id-presets.json but in the "presets" object. The library expects a presets.json to have all the presets at the root level.

## 2.0

- dictionary now uses indices for both the lookup by tags and the lookup by term, which speeds up individual lookups somewhat.
- added support for fallback locales. You can now specify several locales in which to search for a term
- added support for presets that are available everywhere **except** in certain countries
- added support for filtering results by term whether they are suggestions (brand names) or not
- uses new source for presets and translations (iD presets have been outsourced into an own repository) that has a slightly different format
- now, Feature objects are returned (not Match) objects in lookups which contain most informations of a preset in iD format
- added possibility to add (and thus merge) presets from several directories

## 1.2

- enable to also either exclusively search for presets that are suggestions (=brand names) and to exclusively search for presets that are not suggestions. (`QueryByTagBuilder::isSuggestion(Boolean)`)
- when searching by tags and the search is not limited by locale, sort those matches further up in the list of search results that are also not limited by locale
- when searching by tags, sort those matches further up in the list of search results of whose `addTags` match with more of the given tags
- internally use LinkedHashMap. This should have no effect on pure Java. On Android API 20+, this should sort presets that were defined further up in the `presets.json` also further up in the list of results if all other sort criteria are the same for any two matches

## 1.1

- correct the gradle script in the README
