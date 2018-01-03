package simulation.motor;

public class LinearAttachment implements Attachment {
	double mass;

	public LinearAttachment(double mass) {
		this.mass = mass;
	}

	@Override
	public double getAddedTorque(double theta) {
		double torqueGravity = -GRAVITY_ACCEL * mass;
		return torqueGravity;
	}

	@Override
	public double getMoment() {
		return 1;
	}
}
