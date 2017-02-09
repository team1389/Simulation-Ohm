package simulation.drive_sim.auto_sim;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;
import com.team1389.configuration.PIDConstants;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import simulation.drive_sim.DriveSimulator;
import simulation.drive_sim.PathFollowingSystem;
import simulation.drive_sim.SimWorkbench;
import simulation.drive_sim.PathFollowingSystem.PathFollowCommand;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class AutonStart extends SimWorkbench {
	/**
	 * This just generates an auotn simulation where the robot just stands still
	 * The purpose is to find waypoints for paths
	 * 
	 * @author raffi_000
	 */
	private CommandScheduler scheduler;
	private PathFollowingSystem cont;

	public AutonStart(RenderableRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
		initialize();
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		Waypoint[] p = new Waypoint[] { new Waypoint(0, 1, 0), new Waypoint(1, 0, 0) };
		PathFollowingSystem.Constants constants = new PathFollowingSystem.Constants(100, 20, 240, .1, .0025, 0, 0.70,
				.6);
		cont = new PathFollowingSystem(robot.tank.getDrive(), robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);
		scheduler.schedule(cont.new PathFollowCommand(p, false, 0));

	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
