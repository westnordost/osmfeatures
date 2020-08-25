# Changelog

## 1.2

- enable to also either exclusively search for presets that are suggestions (=brand names) and to exclusively search for presets that are not suggestions. (`QueryByTagBuilder::isSuggestion(Boolean)`)
- when searching by tags and the search is not limited by locale, sort those matches further up in the list of search results that are also not limited by locale
- when searching by tags, sort those matches further up in the list of search results of whose `addTags` match with more of the given tags
- internally use LinkedHashMap. This should have no effect on pure Java. On Android API 20+, this should sort presets that were defined further up in the `presets.json` also further up in the list of results if all other sort criteria are the same for any two matches

## 1.1

- correct the gradle script in the README
