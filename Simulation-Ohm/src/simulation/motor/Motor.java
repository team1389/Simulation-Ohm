package simulation.motor;

public class Motor {
	public final double stallTorque;
	public final double freeSpeed;
	private boolean brake;
	private double voltage;

	public Motor(double stallTorque, double freeSpeedRPM, boolean brake) {
		this.stallTorque = stallTorque;
		this.freeSpeed = freeSpeedRPM * 2 * Math.PI / 60;
		this.brake = brake;
	}

	public Motor(MotorType type, boolean brake) {
		this(type.stallTorque, type.freeSpeed, brake);
	}

	public Motor(MotorType type) {
		this(type, true);
	}

	/**
	 * Calculate the torque contributed from the motor.
	 * 
	 * @param omega current angular velocity
	 * @return torque from motor
	 */
	public double getTorque(double omega) {
		if (voltage < .05 && !brake) {
			return 0;
		}
		return stallTorque * (voltage - omega / freeSpeed);
	}

	public void setPercentVoltage(double voltage) {
		this.voltage = voltage;
	}

	public enum MotorType {
		CIM(2.42, 5330), m775_PRO(.71, 18730), MINI_CIM(1.4, 5840), BAG_MOTOR(.4, 13180);
		private double stallTorque;
		private double freeSpeed;

		private MotorType(double stallTorque, double freeSpeed) {
			this.stallTorque = stallTorque;
			this.freeSpeed = freeSpeed;
		}
	}
}