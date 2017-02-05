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
import jaci.pathfinder.followers.EncoderFollower;
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
		Waypoint[] points = new Waypoint[] { new Waypoint(0, 0, 0), // Waypoint
																	// @ x=-4,
																	// y=-1,
																	// exit
																	// angle=-45
																	// degrees
				new Waypoint(convertX(151), convertY(243), Pathfinder.d2r(-60)), // Waypoint @ x=-2,
																					// y=-2, exit
				// angle=0 radians
		};

		Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC,
				Trajectory.Config.SAMPLES_HIGH, 0.05, 1, 5.0, 60.0);
		trajectory = Pathfinder.generate(points, config);
		TankModifier modifier = new TankModifier(trajectory).modify(0.6);
		EncoderFollower left = new EncoderFollower(modifier.getLeftTrajectory());
		EncoderFollower right = new EncoderFollower(modifier.getRightTrajectory());
		left.configureEncoder(0, 7200, 0.102);
		left.configurePIDVA(3.0, 0.0, 0.0, 1 / 6, 0);
		right.configureEncoder(0, 7200, 0.102);
		right.configurePIDVA(3.0, 0.0, 0.0, 1 / 6, 0);

		Command updatePath = new Command() {
			@Override
			protected void initialize() {
				super.initialize();
				robot.tank.reset();
			}

			@Override
			protected boolean execute() {
				double l = left.calculate((int) (robot.tank.left.getPositionInput().get() * 20));
				double r = right.calculate((int) (robot.tank.right.getPositionInput().get() * 20));
				double gyro_heading = 180 + robot.getGyro().get(); // Assuming the gyro is giving a
																	// value
																	// in degrees
				double desired_heading = Pathfinder.r2d(left.getHeading()); // Should also be in
																			// degrees

				double angleDifference = Pathfinder.boundHalfDegrees(desired_heading - gyro_heading);
				double turn = 40 * (-1.0 / 80.0) * angleDifference;
				System.out.println(desired_heading + " " + gyro_heading);
				asTank.set(l + turn, r - turn);
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
		System.out.println(robot.getStartPos().getTranslation().getX());
		double x = (-val + robot.getStartPos().getTranslation().getX()) * .0254;
		System.out.println(x);
		return x;
	}

	double convertY(double val) {
		double x = (-val + robot.getStartPos().getTranslation().getY()) * .0254;
		return x;
	}

	@Override
	protected void update() {
		scheduler.update();
	}

	public void render(Graphics g) {
		Arrays.stream(trajectory.segments).forEach(s -> {
			double segx = robot.getStartPos().getTranslation().getX() * DriveSimulator.scale
					- s.x * DriveSimulator.scale / .0254;
			double segy = robot.getStartPos().getTranslation().getY() * DriveSimulator.scale
					- s.y * DriveSimulator.scale / .0254;
			// System.out.println(segx + " " + segy);
			g.fillOval((float) segx - 5, (float) segy - 5, 10, 10);
		});

	}

}
