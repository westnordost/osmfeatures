package de.westnordost.osmfeatures;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

class StringUtils
{
	private static final Pattern FIND_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	static String stripDiacritics(String str)
	{
		return FIND_DIACRITICS.matcher(Normalizer.normalize(str, Normalizer.Form.NFD)).replaceAll("");
	}

	static String canonicalize(String str)
	{
		return stripDiacritics(str).toLowerCase(Locale.US);
	}

	static boolean startsWordWith(String haystack, String needle)
	{
		int indexOf = haystack.indexOf(needle);
		return indexOf == 0 || indexOf > 0 && haystack.charAt(indexOf-1) == ' ';
	}
}
