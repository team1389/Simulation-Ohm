package simulation.drive_sim.auto_sim;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import simulation.drive_sim.PathFollowingSystem;
import simulation.drive_sim.SimWorkbench;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class AutoOptionFour extends SimWorkbench {
	CommandScheduler scheduler;
	PathFollowingSystem cont;
	Trajectory traj;
	Trajectory traj2;
	Trajectory traj3;

	public AutoOptionFour(RenderableRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
		initialize();
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;

		PathFollowingSystem.Constants constants = new PathFollowingSystem.Constants(200, 20, 240, .17, .004, 0, 0.65,
				.6);
		cont = new PathFollowingSystem(robot.tank.getDrive(), robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);

		Waypoint[] points = new Waypoint[] { new Waypoint(0, 0, 0), new Waypoint(-66, -28, Pathfinder.d2r(73.2)),
				new Waypoint(-76, -65, Pathfinder.d2r(90)) };
		Waypoint[] points2 = new Waypoint[] { new Waypoint(27, -21, Pathfinder.d2r(80)),
				new Waypoint(-27, 52, Pathfinder.d2r(155)) };
		Waypoint[] points3 = new Waypoint[] { new Waypoint(-27, 20, Pathfinder.d2r(155)),
				new Waypoint(25, -30, Pathfinder.d2r(-60)) };
		Waypoint[] points4 = new Waypoint[] { new Waypoint(40, 40, Pathfinder.d2r(-60)),
				new Waypoint(35, 40, Pathfinder.d2r(210)), new Waypoint(-20, -10, Pathfinder.d2r(180)) };

		traj = cont.generateTrajectory(new Waypoint[] { new Waypoint(0, 0, 0),
				new Waypoint(-66, -28, Pathfinder.d2r(72.3)), new Waypoint(-76, -65, Pathfinder.d2r(90)) });

		traj2 = cont.generateTrajectory(
				new Waypoint[] { new Waypoint(-76, -65, Pathfinder.d2r(-180)), new Waypoint(100, 0, 0) });

		// traj3 = cont.generateTrajectory(new Waypoint[] { new Waypoint(-34,
		// 107, Pathfinder.d2r(113.1)) });

		scheduler.schedule(CommandUtil.combineSequential(cont.new PathFollowCommand(traj, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points2, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points3, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points4, false, -180)));

	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
