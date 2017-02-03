package simulation.input;

import java.util.List;

import com.team1389.hardware.inputs.interfaces.BinaryInput;
import com.team1389.hardware.inputs.software.DigitalIn;
import com.team1389.hardware.inputs.software.PercentIn;
import com.team1389.util.list.AddList;

import net.java.games.input.Component.Identifier.Key;

public class KeyboardJoystickEmulator extends SimJoystick {
	private List<DigitalIn> buttons;
	private List<Axis> axes;
	private KeyboardHardware keyboard;

	public KeyboardJoystickEmulator() {
		super(20);
		keyboard = new KeyboardHardware();
		axes = generateAxes();
		buttons = generateButtons();
	}

	/**
	 * @param button the button port to check
	 * @return a boolean stream of the button data
	 */
	public BinaryInput getRawButton(int button) {
		return buttons.get(button)::get;
	}

	/**
	 * 
	 * @param axis the axis to track
	 * @return a percent stream that tracks the value of the axis
	 */
	public PercentIn getAxis(int axis) {
		return axis < axes.size() ? new PercentIn(axes.get(axis)::get) : new PercentIn(() -> 0.0);
	}

	public boolean isPresent() {
		return true;
	}

	private AddList<Axis> generateAxes() {
		return new AddList<Axis>()
				.put(new Axis(keyboard, Key.S, Key.W, 1))
					.put(new Axis(keyboard, Key.D, Key.A, 1))
					.put(new Axis(keyboard, Key.E, Key.Q, 1));
	}

	private AddList<DigitalIn> generateButtons() {
		return new AddList<DigitalIn>()
				.put(keyboard.getKey(Key.SPACE))
					.put(keyboard.getKey(Key.V))
					.put(keyboard.getKey(Key.F))
					.put(keyboard.getKey(Key.T))
					.put(keyboard.getKey(Key.G));
	}
}
