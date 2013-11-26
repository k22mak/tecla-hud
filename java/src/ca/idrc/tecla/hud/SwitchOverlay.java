package ca.idrc.tecla.hud;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import ca.idrc.tecla.hud.utils.SimpleOverlay;

public class SwitchOverlay extends SimpleOverlay {

	public SwitchOverlay(Context context) {
		super(context);

		final WindowManager.LayoutParams params = getParams();
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		setParams(params);

		View rView = getRootView();
		rView.setBackgroundResource(R.drawable.screen_switch_background_normal);
		rView.setOnTouchListener(mOverlayTouchListener);
	}

	/**
	 * Listener for full-screen switch actions
	 */
	private View.OnTouchListener mOverlayTouchListener = new View.OnTouchListener() {
		
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				getRootView().setBackgroundResource(R.drawable.screen_switch_background_pressed);
				//TeclaApp.a11yservice.injectSwitchEvent(
						//new SwitchEvent(SwitchEvent.MASK_SWITCH_E1, 0)); //Primary switch pressed
				break;
			case MotionEvent.ACTION_UP:
				getRootView().setBackgroundResource(R.drawable.screen_switch_background_normal);
				//TeclaApp.a11yservice.injectSwitchEvent(
						//new SwitchEvent(0,0)); //Switches released
				// if (TeclaApp.DEBUG) Log.d(TeclaApp.TAG, CLASS_TAG + "Fullscreen switch up!");
				break;
			default:
				break;
			}
			return false;
		}
	};

}
