package simulation.drive_sim.robot;

import com.team1389.hardware.inputs.software.EncoderIn;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Percent;
import com.team1389.hardware.value_types.Position;
import com.team1389.system.drive.DriveOut;
import com.team1389.system.drive.FourDriveOut;
import com.team1389.trajectory.Kinematics;
import com.team1389.trajectory.RigidTransform2d.Delta;

import simulation.drive_sim.DriveTrain;
import simulation.motor.Attachment;
import simulation.motor.Motor;
import simulation.motor.Motor.MotorType;
import simulation.motor.MotorSystem;
import simulation.motor.element.CylinderElement;

public class MecanumDriveTrain implements DriveTrain {
	double tl, tr, bl, br;
	MotorSystem topLeft = new MotorSystem(new Attachment(new CylinderElement(.5, 0.1), false), 3.5, 0.01,
			new Motor(MotorType.CIM));
	MotorSystem topRight = new MotorSystem(new Attachment(new CylinderElement(.5, 0.1), false), 3.5, 0.01,
			new Motor(MotorType.CIM));
	MotorSystem botLeft = new MotorSystem(new Attachment(new CylinderElement(.5, 0.1), false), 3.5, 0.01,
			new Motor(MotorType.CIM));
	MotorSystem botRight = new MotorSystem(new Attachment(new CylinderElement(.5, 0.1), false), 3.5, 0.01,
			new Motor(MotorType.CIM));
	RangeIn<Position> topLeftIn, topRightIn, botLeftIn, botRightIn;

	public MecanumDriveTrain() {
		EncoderIn.setGlobalWheelDiameter(4);
		topLeftIn = topLeft.getPositionInput().getInches();
		topRightIn = topRight.getPositionInput().getInches();
		botLeftIn = botLeft.getPositionInput().getInches();
		botRightIn = botRight.getPositionInput().getInches();
	}

	public DriveOut<Percent> getTop() {
		return new DriveOut<Percent>(topLeft.getVoltageOutput(), topRight.getVoltageOutput());
	}

	public DriveOut<Percent> getBottom() {
		return new DriveOut<Percent>(botLeft.getVoltageOutput(), botRight.getVoltageOutput());
	}

	public FourDriveOut<Percent> getWheels() {
		return new FourDriveOut<>(getTop(), getBottom());
	}

	@Override
	public Delta getRobotDelta(double dt) {
		topLeft.update();
		topRight.update();
		botLeft.update();
		botRight.update();
		Delta velocity = new Kinematics(10, 23, .6).inverse(topLeftIn.get() - tl, topRightIn.get() - tr,
				botLeftIn.get() - bl, botRightIn.get() - br);
		tl = topLeftIn.get();
		tr = topRightIn.get();
		bl = botLeftIn.get();
		br = botRightIn.get();
		System.out.println(tl);

		return new Delta(velocity.dx / 2, velocity.dy / 2, velocity.dtheta / 2);
	}

	public void reset() {
		topLeft.reset();
		topRight.reset();
		botLeft.reset();
		botRight.reset();
		tl = 0;
		tr = 0;
		bl = 0;
		br = 0;
	}

}
