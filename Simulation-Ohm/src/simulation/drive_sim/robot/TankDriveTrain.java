package simulation.drive_sim.robot;

import com.team1389.control.PIDController;
import com.team1389.hardware.inputs.software.EncoderIn;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.outputs.software.RangeOut;
import com.team1389.hardware.value_types.Percent;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Speed;
import com.team1389.system.drive.DriveOut;
import com.team1389.trajectory.Kinematics;
import com.team1389.trajectory.RigidTransform2d.Delta;

import simulation.drive_sim.DriveTrain;
import simulation.motor.Attachment;
import simulation.motor.Motor;
import simulation.motor.Motor.MotorType;
import simulation.motor.MotorSystem;
import simulation.motor.element.CylinderElement;

public class TankDriveTrain implements DriveTrain {
	public double leftDistance = 0;
	public double rightDistance = 0;
	public MotorSystem left = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 4, 1,
			new Motor(MotorType.CIM));
	public MotorSystem right = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 4, 1,
			new Motor(MotorType.CIM));
	public RangeIn<Position> leftIn;
	public RangeIn<Position> rightIn;

	public TankDriveTrain() {
		EncoderIn.setGlobalWheelDiameter(4);
		leftIn = left.getPositionInput().getInches();
		rightIn = right.getPositionInput().getInches();
	}

	public RangeIn<Speed> leftVel = left.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 4);
	public RangeIn<Speed> rightVel = right.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 4);

	public DriveOut<Percent> getDrive() {
		return new DriveOut<Percent>(left.getVoltageOutput(), right.getVoltageOutput());
	}

	public DriveOut<Speed> getSpeedDrive() {
		RangeOut<Speed> speedL = new PIDController<Percent, Speed>(0.01, 0, 0, .1, leftVel, left.getVoltageOutput())
				.getSetpointSetter();
		RangeOut<Speed> speedR = new PIDController<Percent, Speed>(0.01, 0, 0, .1, rightVel, right.getVoltageOutput())
				.getSetpointSetter();
		return new DriveOut<Speed>(speedL, speedR);
	}

	@Override
	public Delta getRobotDelta(double dt) {
		left.update();
		right.update();
		Delta velocity = new Kinematics(10, 23, .6).forwardKinematics(leftIn.get() - leftDistance,
				rightIn.get() - rightDistance);
		leftDistance = leftIn.get();
		rightDistance = rightIn.get();
		return velocity;
	}

	@Override
	public void reset() {
		System.out.println("resetting tank");
		leftDistance = 0;
		rightDistance = 0;
		left.reset();
		right.reset();
	}
}
