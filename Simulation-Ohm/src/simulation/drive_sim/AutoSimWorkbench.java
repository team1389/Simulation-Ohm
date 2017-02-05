package simulation.drive_sim;

import java.util.Arrays;

import org.newdawn.slick.Graphics;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.command_base.Command;
import com.team1389.hardware.value_types.Percent;
import com.team1389.system.drive.DriveOut;
import com.team1389.system.drive.DriveSignal;
import com.team1389.watch.info.NumberInfo;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.DistanceFollower;
import jaci.pathfinder.modifiers.TankModifier;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class AutoSimWorkbench extends SimWorkbench {
	CommandScheduler scheduler;

	public AutoSimWorkbench(RenderableRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
		init();
	}

	Trajectory trajectory;

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		DriveOut<Percent> asTank = robot.getWheels().getAsTank();
		dash.watch(asTank, robot.getGyro().getWatchable("gyro"), new NumberInfo("robot x", () -> (double) robot.getX()),
				new NumberInfo("robot y", () -> (double) robot.getY()));
		dash.outputToDashboard();
		Waypoint[] points = new Waypoint[] { new Waypoint(0, 0, 0),
				new Waypoint(convertX(151), convertY(243), Pathfinder.d2r(-60)) };

		Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC,
				Trajectory.Config.SAMPLES_HIGH, .05, 240, 20.0, 600.0);
		trajectory = Pathfinder.generate(points, config);
		TankModifier modifier = new TankModifier(trajectory).modify(0.6);
		DistanceFollower left = new DistanceFollower(modifier.getLeftTrajectory());
		DistanceFollower right = new DistanceFollower(modifier.getRightTrajectory());
		left.configurePIDVA(0.1, 0.0, 0.005, 1 / 240, 0);
		right.configurePIDVA(0.1, 0.0, 0.005, 1 / 240, 0);

		Command updatePath = new Command() {
			@Override
			protected void initialize() {
				super.initialize();
				robot.tank.reset();
			}

			@Override
			protected boolean execute() {
				double l = left.calculate((robot.tank.leftIn.get()));
				double r = right.calculate((robot.tank.rightIn.get()));
				double gyro_heading = 180 + robot.getGyro().get();
				double desired_heading = Pathfinder.r2d(left.getHeading());
				double angleDifference = Pathfinder.boundHalfDegrees(desired_heading - gyro_heading);
				double turn = 0.75 * angleDifference;
				System.out.println(desired_heading + " " + gyro_heading);
				asTank.set(l - turn, r + turn);
				return left.isFinished() && right.isFinished();
			}

			@Override
			protected void done() {
				super.done();
				asTank.set(DriveSignal.NEUTRAL);
			}
		};
		scheduler.schedule(updatePath);
	}

	double convertX(double val) {
		return -val + robot.getStartPos().getTranslation().getX();
	}

	double convertY(double val) {
		return -val + robot.getStartPos().getTranslation().getY();
	}

	@Override
	protected void update() {
		scheduler.update();
	}

	public void render(Graphics g) {
		Arrays.stream(trajectory.segments).forEach(s -> {
			double segx = robot.getStartPos().getTranslation().getX() * DriveSimulator.scale
					- s.x * DriveSimulator.scale;
			double segy = robot.getStartPos().getTranslation().getY() * DriveSimulator.scale
					- s.y * DriveSimulator.scale;
			// System.out.println(segx + " " + segy);
			g.fillOval((float) segx - 5, (float) segy - 5, 10, 10);
		});

	}

}
