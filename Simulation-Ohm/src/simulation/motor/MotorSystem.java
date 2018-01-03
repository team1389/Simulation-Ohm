package simulation.motor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.team1389.hardware.outputs.software.PercentOut;
import com.team1389.util.RangeUtil;

import simulation.motor.element.Element;

public class MotorSystem extends RotationSim {
	Set<Motor> motors;
	public static double DEFAULT_FRICTION = 1;
	private double gearReduction;
	private Attachment attachment;
	private double rangeMin, rangeMax;
	private double friction;

	public MotorSystem(Set<Motor> motors, Attachment attachment, double gearing, double friction) {
		this.gearReduction = gearing;
		this.attachment = attachment;
		this.motors = motors;
		this.rangeMax = Double.MAX_VALUE;
		this.rangeMin = -Double.MAX_VALUE;
		this.friction = friction;
	}

	public MotorSystem(Attachment attachment, double gearing, double friction, Motor... motors) {
		this(new HashSet<Motor>(Arrays.asList(motors)), attachment, gearing, friction);
	}

	public MotorSystem(Motor motor, Attachment attachment, double gearing, double friction) {
		this(new HashSet<>(), attachment, gearing, friction);
		motors.add(motor);
	}

	public MotorSystem(Motor motor, double friction) {
		this(motor, new RotaryAttachment(Element.FREE, false), 1, friction);
	}

	public MotorSystem(Motor motor) {
		this(motor, new RotaryAttachment(Element.FREE, false), 1, DEFAULT_FRICTION);
	}

	public PercentOut getVoltageOutput() {
		return new PercentOut(this::setVoltage);
	}

	public void setRangeOfMotion(double min, double max) {
		this.rangeMax = max;
		this.rangeMin = min;
	}

	/**
	 * @param voltage
	 *            percent voltage applied to the motor
	 */
	public void setVoltage(double voltage) {
		// System.out.println(voltage + "v");
		double limVoltage = RangeUtil.limit(voltage, -1, 1);
		motors.forEach(m -> m.setPercentVoltage(limVoltage));
	}

	@Override
	public void update() {
		super.update();
		if (((omega > 0 && theta >= rangeMax) || (omega < 0 && theta <= rangeMin))) {
			theta = RangeUtil.limit(theta, rangeMin, rangeMax);
			if (((alpha > 0 && theta > rangeMax) || (alpha < 0 && theta < rangeMin))) {
				omega = 0;
			}
		}
	}

	@Override
	protected double getNetTorque() {
		double motorTorque = getMotorTorque()* gearReduction;
		double attachmentTorque = getAttachmentTorque();
		double frictionTorque = getFrictionTorque();
		return motorTorque  + attachmentTorque + frictionTorque;
	}

	@Override
	protected double getMoment() {
		return attachment.getMoment();
	}

	private double getFrictionTorque() {
		return Math.abs(omega) > 50 ? -Math.signum(omega) * friction : 0;
	}

	private double getMotorTorque() {
		return motors.stream().mapToDouble(m -> m.getTorque(omega * gearReduction)).sum();
	}

	private double getAttachmentTorque() {
		return attachment.getAddedTorque(theta);
	}
}
