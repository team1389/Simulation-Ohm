package simulation.motor;

public class Motor {
	public final double kT;
	public final double kV;
	public final double maxVoltage;
	private double voltage;

	public Motor(double stallTorque, double freeSpeed, double maxVoltage) {
		this.kT = stallTorque / maxVoltage;
		this.kV = freeSpeed / maxVoltage;
		this.maxVoltage = maxVoltage;
	}

	public Motor(MotorType type) {
		this(type.stallTorque, type.freeSpeed, type.voltage);
	}

	/**
	 * Calculate the torque contributed from the motor.
	 * 
	 * @param omega
	 *            current angular velocity
	 * @return torque from motor
	 */
	public double getTorque(double omega) {
		return kT * (voltage - omega / kV);
	}

	public void setPercentVoltage(double voltage) {
		this.voltage = voltage*maxVoltage;
	}

	public enum MotorType {
		CIM(2.42, 5330, 12.0), m775_PRO(.71, 18730, 12.0), MINI_CIM(1.4, 5840, 12), BAG_MOTOR(.4, 13180, 12);
		private double stallTorque;
		private double freeSpeed;
		private double voltage;

		private MotorType(double stallTorque, double freeSpeed, double voltage) {
			this.stallTorque = stallTorque;
			this.freeSpeed = freeSpeed;
			this.voltage = voltage;
		}
	}
}