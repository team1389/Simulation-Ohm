package simulation.drive_sim.robot;

import com.team1389.hardware.inputs.software.RangeIn;
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
	double leftDistance = 0;
	double rightDistance = 0;
	MotorSystem left = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 4, 5,
			new Motor(MotorType.CIM));
	MotorSystem right = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 4, 5,
			new Motor(MotorType.CIM));
	RangeIn<Position> leftIn = left.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Position> rightIn = right.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> leftVel = left.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> rightVel = right.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);

	public DriveOut<Percent> getDrive() {
		return new DriveOut<Percent>(left.getVoltageOutput(), right.getVoltageOutput());
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
		leftDistance = 0;
		rightDistance = 0;
		left.reset();
		right.reset();
	}
}
