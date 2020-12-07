# Changelog

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
