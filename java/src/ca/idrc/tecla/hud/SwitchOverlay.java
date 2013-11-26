package ca.idrc.tecla.hud;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import ca.idrc.tecla.hud.utils.SwitchEvent;
import ca.idrc.tecla.hud.utils.SimpleOverlay;

public class SwitchOverlay extends SimpleOverlay {
	
	private SwitchEvent.OnSwitchEventListener mSwitchEventListener;

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
				mSwitchEventListener.onSwitchPress(SwitchEvent.SWITCH_P1);
				break;
			case MotionEvent.ACTION_UP:
				getRootView().setBackgroundResource(R.drawable.screen_switch_background_normal);
				mSwitchEventListener.onSwitchRelease(SwitchEvent.SWITCH_P1);
				break;
			default:
				break;
			}
			return false;
		}
	};
	
	public void registerOnSwitchEventListener(SwitchEvent.OnSwitchEventListener listener) {
		mSwitchEventListener = listener;
	}

}
