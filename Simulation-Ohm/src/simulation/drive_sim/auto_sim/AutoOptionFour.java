package simulation.drive_sim.auto_sim;

import java.io.File;
import java.util.Arrays;

import org.newdawn.slick.Graphics;

import com.team1389.auto.command.TurnAngleCommand;
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

public class AutoOptionFour extends SimWorkbench {
	CommandScheduler scheduler;
	PathFollowingSystem cont;

	public AutoOptionFour(RenderableRobot robot) {
		super(robot);
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		Waypoint[] point = new Waypoint[] { new Waypoint(0, 30, 0), new Waypoint(-101, 56, Pathfinder.d2r(300)) };
		PathFollowingSystem.Constants constants = new PathFollowingSystem.Constants(100, 20, 240, .1, .0025, 0, 0.70,
				.6);
		cont = new PathFollowingSystem(robot.tank.getDrive(), robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);
		scheduler.schedule(cont.new PathFollowCommand(point, false, -180));
		
	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
