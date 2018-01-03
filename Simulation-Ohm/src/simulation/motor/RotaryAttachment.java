package simulation.motor;

import simulation.motor.element.Element;

public class RotaryAttachment implements Attachment {


	final boolean hasWeight;
	Element element;

	public RotaryAttachment(Element e, boolean hasWeight) {
		this.element = e;
		this.hasWeight = hasWeight;
	}

	public double getAddedTorque(double theta) {
		if (hasWeight) {
			double torqueGravity = -GRAVITY_ACCEL * element.mass * Math.cos(theta) * element.centerOfMass;
			return torqueGravity;
		} else {
			return 0;
		}
	}

	public double getMoment() {
		return element.moment;
	}
}
