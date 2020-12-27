package exhibition.module.data;

import exhibition.module.data.settings.Setting;

import java.util.HashMap;

public class SettingsMap extends HashMap<String, Setting> {
	/**
	 * Updates settings
	 * 
	 * @param newMap
	 */
	public void update(HashMap<String, Setting> newMap) {
		for (String key : newMap.keySet()) {
			// Overwrite overlapping entries.
			// Do nothing for non-overlapping entries.
			if (containsKey(key)) {
				get(key).update(newMap.get(key));
			}
		}
	}
}
