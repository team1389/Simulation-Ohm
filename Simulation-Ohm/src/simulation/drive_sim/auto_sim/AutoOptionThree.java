package simulation.drive_sim.auto_sim;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import simulation.drive_sim.PathFollowingSystem;
import simulation.drive_sim.PathFollowingSystem.PathFollowCommand;
import simulation.drive_sim.SimWorkbench;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class AutoOptionThree extends SimWorkbench {
	CommandScheduler scheduler;
	PathFollowingSystem cont;
	Trajectory traj;

	public AutoOptionThree(RenderableRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
		initialize();
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		Waypoint[] points = new Waypoint[] { new Waypoint(0, 30, 0), new Waypoint(-101, 56, Pathfinder.d2r(300)) };
		Waypoint[] points2 = new Waypoint[] { new Waypoint(50, 50, 0), new Waypoint(-20, -20, 0) };

		PathFollowingSystem.Constants constants = new PathFollowingSystem.Constants(100, 20, 240, .1, .0025, 0, 0.70,
				.6);
		cont = new PathFollowingSystem(robot.tank.getDrive(), robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);

		scheduler.schedule(CommandUtil.combineSequential(cont.new PathFollowCommand(points, false, 180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(points2, false, 180)));

	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
