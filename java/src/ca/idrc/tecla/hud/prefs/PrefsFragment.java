package ca.idrc.tecla.hud.prefs;

import ca.idrc.tecla.hud.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PrefsFragment extends PreferenceFragment {

	/* (non-Javadoc)
	 * @see android.preference.PreferenceFragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}
	
	private void init() {
		addPreferencesFromResource(R.xml.preferences);
	}

}
