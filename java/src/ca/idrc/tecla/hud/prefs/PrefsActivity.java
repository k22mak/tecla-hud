package ca.idrc.tecla.hud.prefs;

import ca.idrc.tecla.hud.R;
import android.app.Activity;
import android.os.Bundle;

public class PrefsActivity extends Activity {

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		init();
	}

	private void init() {
		setContentView(R.layout.preferences);
	}

}
