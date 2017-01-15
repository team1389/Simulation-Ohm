package simulation;

import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Percent;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Speed;
import com.team1389.system.drive.DriveOut;
import com.team1389.trajectory.Kinematics;
import com.team1389.trajectory.RigidTransform2d.Delta;

import simulation.motor.Attachment;
import simulation.motor.DriveTrain;
import simulation.motor.Motor;
import simulation.motor.MotorSystem;
import simulation.motor.Motor.MotorType;
import simulation.motor.element.CylinderElement;

public class MecanumDriveTrain implements DriveTrain {
	double tl, tr, bl, br;
	MotorSystem topleft = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 22 / 3, .25,
			new Motor(MotorType.CIM));
	MotorSystem topright = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 22 / 3, .25,
			new Motor(MotorType.CIM));
	MotorSystem botleft = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 22 / 3, .25,
			new Motor(MotorType.CIM));
	MotorSystem botright = new MotorSystem(new Attachment(new CylinderElement(1, 0.1), false), 22 / 3, .25,
			new Motor(MotorType.CIM));
	RangeIn<Position> leftIn = topleft.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Position> rightIn = topright.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> leftVel = topleft.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> rightVel = topright.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Position> botleftIn = botleft.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Position> botrightIn = botright.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> botleftVel = botleft.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> botrightVel = botright.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);

	public DriveOut<Percent> getTop() {
		return new DriveOut<Percent>(topleft.getVoltageOutput(), topright.getVoltageOutput());
	}

	public DriveOut<Percent> getBottom() {
		return new DriveOut<Percent>(botleft.getVoltageOutput(), botright.getVoltageOutput());
	}

	@Override
	public Delta getRobotDelta(double dt) {
		topleft.update();
		topright.update();
		botleft.update();
		botright.update();
		Delta velocity = new Kinematics(10, 23, .6).inverse(leftIn.get() - tl, rightIn.get() - tr, botleftIn.get() - bl,
				botrightIn.get() - br);
		tl = leftIn.get();
		tr = rightIn.get();
		bl = botleftIn.get();
		br = botrightIn.get();

		return new Delta(velocity.dx / 2, velocity.dy / 2, velocity.dtheta / 2);
	}

	public void reset() {
		topleft.reset();
		topright.reset();
		botleft.reset();
		botright.reset();
		tl = 0;
		tr = 0;
		bl = 0;
		br = 0;
	}

}
