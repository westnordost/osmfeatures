package de.westnordost.osmfeatures;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringUtils
{
	private static final Pattern FIND_DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

	public static String canonicalize(String str)
	{
		return stripDiacritics(str).toLowerCase(Locale.US);
	}

	private static String stripDiacritics(String str)
	{
		return FIND_DIACRITICS.matcher(Normalizer.normalize(str, Normalizer.Form.NFD)).replaceAll("");
	}
}
