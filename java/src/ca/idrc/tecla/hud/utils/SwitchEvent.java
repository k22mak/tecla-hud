package ca.idrc.tecla.hud.utils;

public class SwitchEvent {
	
	// MASKS FOR READING SWITCH STATES
	public static final int SWITCH_W1 = 0x01; //Forward / Up
	public static final int SWITCH_W2 = 0x02; //Back / Down
	public static final int SWITCH_W3 = 0x04; //Left
	public static final int SWITCH_W4 = 0x08; //Right
	public static final int SWITCH_P1 = 0x10;
	public static final int SWITCH_P2 = 0x20;

	public interface OnSwitchEventListener {
		public abstract void onSwitchPress(int switch_id);
		public abstract void onSwitchRelease(int switch_id);
	}

}
