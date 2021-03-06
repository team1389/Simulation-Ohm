package simulation.drive_sim;

import com.team1389.hardware.inputs.software.DigitalIn;
import com.team1389.hardware.inputs.software.PercentIn;
import com.team1389.system.drive.CurvatureDriveSystem;
import com.team1389.system.drive.MecanumDriveSystem;
import com.team1389.util.bezier.BezierCurve;

import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class DriverSimWorkbench extends SimWorkbench {

	public DriverSimWorkbench(RenderableRobot robot) {
		super(robot);
		initialize();
	}

	CurvatureDriveSystem tankD;
	MecanumDriveSystem mecD;
	OctoRobot myRobot;

	public void initialize() {
		myRobot = (OctoRobot) this.robot;
		PercentIn a0 = joy.getAxis(0).applyDeadband(.1).scale(2).limit(1).invert();
		PercentIn a1 = joy.getAxis(1).scale(2).applyDeadband(.1).limit(1).invert();
		PercentIn a2 = joy.getAxis(2).scale(.4).applyDeadband(.075).limit(1);
		DigitalIn toggle = joy.getButton(0);
		BezierCurve xCurve = new BezierCurve(0, .5, .79, -0.06);
		BezierCurve yCurve = new BezierCurve(.0, 0.54, 0.45, -0.07);
		a0.map(d -> yCurve.getPoint(d).getY());
		a1.map(d -> xCurve.getPoint(d).getY());
		joy.getButton(2).latched().addChangeListener(b -> {
			if (b)
				myRobot.setMode(!myRobot.isTankMode());
		}, true);
		mecD = new MecanumDriveSystem(a1.copy().invert(), a0.copy().invert(), a2.copy(), myRobot.getWheels(),
				myRobot.getGyro(), toggle);
		tankD = new CurvatureDriveSystem(myRobot.getWheels().getAsTank(), a0, a1, toggle, .55, .75);
		PercentIn a3 = joy.getAxis(3).adjustRange(0.44, -.7, 0, 1).setRange(-1, 1).mapToPercentIn().limit(.15, .75);
		a3.addChangeListener(((CurvatureDriveSystem) tankD).calc::setCurveSensitivity, true);

		myRobot.setMode(true);
	}

	public void update() {
		if (myRobot.isTankMode())
			tankD.update();
		else
			mecD.update();
	}
}
