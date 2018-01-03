package simulation.motor;

import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Speed;
import com.team1389.util.Timer;

public abstract class RotationSim {
	protected double theta = 0; // current rotation loc
	protected double omega = 0; // current rate of rotation (rpm)
	protected double alpha = 0; // current acceleration of rotation (rpm)
	private Timer timer;

	public RotationSim() {
		timer = new Timer();
	}

	public RangeIn<Position> getPositionInput() {
		return new RangeIn<Position>(Position.class, this::getPosition, 0, 1);
	}

	public RangeIn<Speed> getSpeedInput() {
		return new RangeIn<Speed>(Speed.class, this::getOmega, 0, 1);
	}

	public void update() {
		double dt = timer.getSinceMark();
		timer.mark();
		alpha = calculateAlpha();
		/*
		 * if (Math.abs(alpha) > 10000) { throw new RuntimeException(
		 * "ERROR: specs for simulated motor exceed capabilities of simulator, please add a heavier attachment to compensate: "
		 * + this); }
		 */
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

	private double getOmega() {
		return omega;
	}

	public void setTheta(double newThetaRevs) {
		this.theta = newThetaRevs;
	}

	protected double calculateAlpha() {
		return getNetTorque() / getMoment();
	}

	protected abstract double getNetTorque();

	protected abstract double getMoment();

}
