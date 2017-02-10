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
	Trajectory traj = new Trajectory(2);
	Trajectory traj2 = new Trajectory(2);
	Trajectory traj3 = new Trajectory(2);

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
		traj = cont.generateTrajectory(new Waypoint[] { new Waypoint(0, 0, 0),
				new Waypoint(-66, -28, Pathfinder.d2r(72.3)), new Waypoint(-76, -65, Pathfinder.d2r(90)) });
		traj2 = cont.generateTrajectory(new Waypoint[] { new Waypoint(-76, -65, Pathfinder.d2r(72.3)),
				new Waypoint(-34, 107, Pathfinder.d2r(113.1)) });
		traj3 = cont.generateTrajectory(new Waypoint[] { new Waypoint(-34, 107, Pathfinder.d2r(113.1)) });

		scheduler.schedule(CommandUtil.combineSequential(cont.new PathFollowCommand(traj, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(traj2, false, -180)));//,
				//CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(traj3, false, -180)));

	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
