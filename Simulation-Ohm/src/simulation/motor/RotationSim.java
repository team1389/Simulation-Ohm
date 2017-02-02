package simulation.motor;

import com.team1389.hardware.inputs.software.PositionEncoderIn;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Speed;
import com.team1389.util.Timer;

public abstract class RotationSim {
	protected double theta = 0; // current angle
	protected double omega = 0; // current rate of rotation (rad/sec)
	protected double alpha = 0; // current acceleration of rotation (rad^2/sec)
	private Timer timer;

	public RotationSim() {
		timer = new Timer();
	}

	public PositionEncoderIn getPositionInput() {
		return new PositionEncoderIn(this::getPositionDegrees, 360);
	}

	public RangeIn<Speed> getSpeedInput() {
		return new RangeIn<>(Speed.class, this::getOmegaDegrees, 0, 360);
	}

	public void update() {
		double dt = timer.getSinceMark();
		timer.mark();
		alpha = calculateAlpha();
		omega += alpha * dt; // add to velocity
		theta += omega * dt; // add to position
	}

	public void reset() {
		theta = 0;
		omega = 0;
		timer.zero();
	}

	private double getPosition() {
		return theta;
	}

	private double getPositionDegrees() {
		return Math.toDegrees(getPosition());
	}

	private double getOmega() {
		return omega;
	}

	private double getOmegaDegrees() {
		return Math.toDegrees(getOmega());
	}

	protected double calculateAlpha() {
		return getNetTorque() / getMoment();
	}

	protected abstract double getNetTorque();

	protected abstract double getMoment();

}
