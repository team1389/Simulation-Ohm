package simulation.drive_sim;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.command_base.Command;
import com.team1389.hardware.value_types.Percent;
import com.team1389.system.drive.DriveOut;
import com.team1389.system.drive.DriveSignal;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.EncoderFollower;
import jaci.pathfinder.modifiers.TankModifier;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.SimulationRobot;

public class AutoSimWorkbench extends SimWorkbench {
	CommandScheduler scheduler;

	public AutoSimWorkbench(SimulationRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		DriveOut<Percent> asTank = robot.getWheels().getAsTank();
		dash.watch(asTank);
		Waypoint[] points = new Waypoint[] { new Waypoint(-4, -1, Pathfinder.d2r(-45)), // Waypoint
																						// @ x=-4,
																						// y=-1,
																						// exit
																						// angle=-45
																						// degrees
				new Waypoint(-2, -2, 0), // Waypoint @ x=-2, y=-2, exit angle=0 radians
				new Waypoint(0, 0, 0) // Waypoint @ x=0, y=0, exit angle=0 radians
		};

		Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC,
				Trajectory.Config.SAMPLES_HIGH, 0.05, 1.7, 2.0, 60.0);
		Trajectory trajectory = Pathfinder.generate(points, config);
		TankModifier modifier = new TankModifier(trajectory).modify(0.6);
		EncoderFollower left = new EncoderFollower(modifier.getLeftTrajectory());
		EncoderFollower right = new EncoderFollower(modifier.getRightTrajectory());
		left.configureEncoder(0, 7200, 0.102);
		left.configurePIDVA(1.0, 0.0, 0.0, 1 / 6, 0);
		right.configureEncoder(0, 7200, 0.102);
		right.configurePIDVA(1.0, 0.0, 0.0, 1 / 6, 0);

		Command updatePath = new Command() {
			@Override
			protected void initialize() {
				super.initialize();
				robot.tank.reset();
			}

			@Override
			protected boolean execute() {
				double leftOut = left.calculate((int) (robot.tank.left.getPositionInput().get() * 20));
				double rightOut = right.calculate((int) (robot.tank.right.getPositionInput().get() * 20));
				asTank.set(leftOut, rightOut);
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

	@Override
	protected void update() {
		scheduler.update();
	}

}
