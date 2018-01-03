package simulation.motor;

public interface Attachment {
	public final static double GRAVITY_ACCEL = 9.8; // acceleration of gravity (m/s^2)

	public double getAddedTorque(double theta);

	public double getMoment();

}