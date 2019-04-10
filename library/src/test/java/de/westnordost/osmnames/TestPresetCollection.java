package de.westnordost.osmnames;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TestPresetCollection implements PresetCollection
{
	private final Map<String, Preset> presets;

	public TestPresetCollection(Preset ...presets)
	{
		this.presets = new HashMap<>(presets.length);
		for (Preset preset : presets)
		{
			this.presets.put(preset.id, preset);
		}
	}

	@Override public Collection<Preset> getAll(Locale locale)
	{
		return presets.values();
	}

	@Override public Preset get(String id, Locale locale)
	{
		return presets.get(id);
	}

}
