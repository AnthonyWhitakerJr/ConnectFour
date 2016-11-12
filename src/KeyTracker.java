import java.awt.event.KeyEvent;
import java.util.Arrays;

public class KeyTracker {
	private boolean[] keyState;

	public KeyTracker() {
		keyState = new boolean[256];
	}

	public void handleKeyPressed(KeyEvent e) {
		keyState[e.getKeyCode()] = true;
	}

	public void handleKeyReleased(KeyEvent e) {
		keyState[e.getKeyCode()] = false;
	}

	public boolean isPressed(int i) {
		return keyState[i];
	}

	public void resetAllKeyStates() {
		Arrays.fill(keyState, false);
	}
}