package de.westnordost.osmnames;

import java.util.Collection;
import java.util.Locale;

public interface PresetCollection
{
	Collection<Preset> getAll(Locale locale);
	Preset get(String id, Locale locale);
}
