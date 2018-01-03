package simulation;

import com.team1389.configuration.PIDConstants;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.outputs.hardware.PIDVoltageHardware;
import com.team1389.hardware.outputs.software.RangeOut;
import com.team1389.hardware.value_types.Speed;
import com.team1389.util.Loopable;

import simulation.motor.Motor;
import simulation.motor.MotorSystem;
import simulation.motor.RotaryAttachment;
import simulation.motor.element.Element;

public class SingleTalonSimulator implements Loopable {

	public static void main(String args[]) throws InterruptedException {
		SingleTalonSimulator talonSim = new SingleTalonSimulator();
		Simulator.simulate(talonSim);
	}

	MotorSystem talon;
	PIDVoltageHardware pid;
	RangeOut<Speed> setter;
	RangeIn<Speed> speed;

	@Override
	public void update() {
		talon.update();
		// System.out.println(speed.get());
	}

	@Override
	public void init() {
		talon = new MotorSystem(new Motor(Motor.MotorType.BAG_MOTOR), new RotaryAttachment(Element.FREE, false), 30, 1);
		pid = new PIDVoltageHardware(talon.getVoltageOutput());
		setter = pid.getSpeedOutput(talon.getSpeedInput(), new PIDConstants(0, 0, 0, 1));
		speed = talon.getSpeedInput();
		setter.set(100);
	}
}
