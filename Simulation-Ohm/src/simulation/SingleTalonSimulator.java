package simulation;

import com.team1389.configuration.PIDConstants;
import com.team1389.hardware.inputs.hardware.DashboardScalarInput;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.outputs.hardware.CANTalonHardware;
import com.team1389.hardware.outputs.hardware.PIDVoltageHardware;
import com.team1389.hardware.outputs.software.RangeOut;
import com.team1389.hardware.registry.port_types.CAN;
import com.team1389.hardware.value_types.Speed;
import com.team1389.util.Loopable;
import com.team1389.watch.Watcher;

import simulation.motor.Attachment;
import simulation.motor.Motor;
import simulation.motor.MotorSystem;

public class SingleTalonSimulator implements Loopable{

	
	
	public static void main(String args[]) throws InterruptedException{
		SingleTalonSimulator talonSim = new SingleTalonSimulator();
		Simulator.simulate(talonSim);
	}
	
	MotorSystem talon;
	PIDVoltageHardware pid;
	RangeOut<Speed> setter;
	RangeIn<Speed> speed;
	
	@Override
	public void update() {
		setter.set(100000);
		System.out.println(speed.get());
		
		
	}
	
	@Override
	public void init(){
		talon = new MotorSystem(new Motor(Motor.MotorType.BAG_MOTOR), new Attachment(Attachment.FREE, false), 1, MotorSystem.DEFAULT_FRICTION);
		pid = new PIDVoltageHardware(talon.getVoltageOutput());
		setter = pid.getSpeedOutput(talon.getSpeedInput(), new PIDConstants(1,1,0,.4));
		speed = talon.getSpeedInput();
		setter.set(0);
	}
}
