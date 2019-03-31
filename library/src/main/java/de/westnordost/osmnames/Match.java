package de.westnordost.osmnames;

public class Match
{
	private final Entry entry;
	private final String word;
	private final int wordIndex;

	public Match(Entry entry, String word, int wordIndex)
	{
		this.entry = entry;
		this.word = word;
		this.wordIndex = wordIndex;
	}

	/** @return the actual entry that has been found */
	public Entry getEntry() { return entry; }

	/** @return the matched word with which the entry has been found. */
	public String getWord() { return word; }

	/** @return starting index at which the search word has been found in the matched
	 *          {@link #getWord() word}. -1 if the search word is not in the matched word at all
	 *          which occurs when only a keyword has been matched.
	 *   */
	public int getWordIndex() { return wordIndex; }

	/** @return whether the matched {@link #getWord() word} is the primary name of the
	 *  {@link #getEntry() entry} */
	public boolean isPrimaryNameMatch() { return word.equals(entry.getPrimaryName()); }
}