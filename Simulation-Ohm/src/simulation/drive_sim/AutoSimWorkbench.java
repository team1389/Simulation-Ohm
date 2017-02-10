package simulation.drive_sim;

import java.util.concurrent.CompletableFuture;

import com.team1389.auto.command.TurnAngleCommand;
import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;
import com.team1389.configuration.PIDConstants;
import com.team1389.hardware.value_types.Percent;
import com.team1389.system.drive.DriveOut;
import com.team1389.watch.Watcher;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class AutoSimWorkbench extends SimWorkbench {
	CommandScheduler scheduler;

	public AutoSimWorkbench(RenderableRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
		initialize();
	}

	PathFollowingSystem cont;

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		Waypoint[] points = new Waypoint[] { new Waypoint(0, 30, 0), new Waypoint(-101, 56, Pathfinder.d2r(-60)) };
		Waypoint[] points2 = new Waypoint[] { new Waypoint(0, 0, Pathfinder.d2r(-60)),
				new Waypoint(18, -90, Pathfinder.d2r(-90)) };
		Waypoint[] points3 = new Waypoint[] { new Waypoint(0, 0, Pathfinder.d2r(-90)),
				new Waypoint(63, 16, Pathfinder.d2r(120)) };
		Waypoint[] points4 = new Waypoint[] { new Waypoint(0, 0, Pathfinder.d2r(300)),
				new Waypoint(-151, 29, Pathfinder.d2r(-180)) };
		PathFollowingSystem.Constants constants = new PathFollowingSystem.Constants(200, 20, 48, .17, .004, 0, 0.65,
				.6);

		DriveOut<Percent> drive = robot.tank.getDrive();
		cont = new PathFollowingSystem(drive, robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);

		scheduler.schedule(CommandUtil.combineSequential(cont.new PathFollowCommand(points, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points2, true, 0),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points3, false, 180),
				new TurnAngleCommand<>(-180, .05, robot.getGyro().invert(),
						TurnAngleCommand.createTurnController(drive), new PIDConstants(.03, 0, 0)),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points4, false, 180)));
		dash.watch(drive,robot.getGyro().getWatchable("angle"),robot.tank.leftIn.getWatchable("position")); 
		dash.outputToDashboard();
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

	Trajectory traj = new Trajectory(2);

	/*
	 * public void render(Graphics g) { Arrays.stream(traj.segments).forEach(s -> { double segx =
	 * robot.getStartPos().getTranslation().getX() * DriveSimulator.scale - s.x *
	 * DriveSimulator.scale; double segy = robot.getStartPos().getTranslation().getY() *
	 * DriveSimulator.scale - s.y * DriveSimulator.scale; g.fillOval((float) segx - 5, (float) segy
	 * - 5, 10, 10); }); }
	 */

}
