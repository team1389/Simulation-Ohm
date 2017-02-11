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

/**
 * This is an auotn that picks up balls, then drops a gear off, then shoots high
 * goals, then crosses the baseline
 * 
 * @author raffi_000
 *
 */
public class AutoOptionSeven extends SimWorkbench {
	CommandScheduler scheduler;
	PathFollowingSystem cont;
	Trajectory traj;
	Trajectory traj2;
	Trajectory traj3;

	public AutoOptionSeven(RenderableRobot robot) {
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

		Waypoint[] points = new Waypoint[] { new Waypoint(0, 0, 0), new Waypoint(-66, -28, Pathfinder.d2r(72.3)),
				new Waypoint(-76, -65, Pathfinder.d2r(90)) };
		Waypoint[] points2 = new Waypoint[] { new Waypoint(27, -21, Pathfinder.d2r(80)),
				new Waypoint(-27, 52, Pathfinder.d2r(155)) };

		scheduler.schedule(CommandUtil.combineSequential(cont.new PathFollowCommand(points, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points2, false, -180)));

	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
