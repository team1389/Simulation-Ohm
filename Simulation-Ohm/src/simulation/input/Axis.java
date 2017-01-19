package simulation.input;

import com.team1389.hardware.inputs.interfaces.ScalarInput;
import com.team1389.hardware.inputs.software.DigitalIn;
import com.team1389.hardware.inputs.software.PercentIn;
import com.team1389.hardware.value_types.Percent;
import com.team1389.system.drive.BezierCurve;
import com.team1389.util.RangeUtil;
import com.team1389.util.Timer;

import net.java.games.input.Component.Identifier.Key;

public class Axis implements ScalarInput<Percent> {
	static final double timeRamp = 0.5;
	double scale;
	DigitalIn up;
	DigitalIn upLatched;
	DigitalIn down;
	DigitalIn downLatched;
	Timer timer;
	BezierCurve curve = new BezierCurve(.69, .18, .26, .37);

	public Axis(Key up, Key down, double scale) {
		this(new KeyboardHardware(), up, down, scale);
	}

	public Axis(KeyboardHardware keyboard, Key upKey, Key downKey, double scale) {
		this.up = keyboard.getKey(upKey);
		this.down = keyboard.getKey(downKey);
		downLatched = down.copy().getLatched();
		upLatched = up.copy().getLatched();
		timer = new Timer();
		this.scale = scale;
	}

	public Double get() {
		if (upLatched.get() || downLatched.get()) {
			timer.mark();
			return 0.0;
		} else {
			double bezier = curve
					.getPoint(
							RangeUtil.applyDeadband(RangeUtil.limit(timer.getSinceMark(), 0, timeRamp), 0.1) / timeRamp)
					.getY();
			return scale * Math.abs(bezier) * (up.get() ? 1 : down.get() ? -1 : 0);
		}
	}

	public static PercentIn make(Key up, Key down, double scale) {
		return new PercentIn(new Axis(up, down, scale)::get);
	}

	public static PercentIn make(Key up, Key down, double scale, int val) {
		Axis axis = new Axis(up, down, scale);
		return new PercentIn(axis::get);
	}

	public static PercentIn make(KeyboardHardware keyboard, Key up, Key down, double scale) {
		return new PercentIn(new Axis(keyboard, up, down, scale)::get);
	}
}
