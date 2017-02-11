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
 * This auton goes to the hopper collects balls and then goes to the low goal,
 * and then back across the baseline
 * 
 * @author raffi_000
 *
 */
public class AutoOptionFive extends SimWorkbench {

	CommandScheduler scheduler;
	PathFollowingSystem cont;

	public AutoOptionFive(RenderableRobot robot) {
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

		Waypoint[] hopper = new Waypoint[] { new Waypoint(0, 0, 0), new Waypoint(-76, -65, Pathfinder.d2r(90)) };
		Waypoint[] goal = new Waypoint[] { new Waypoint(-80, -30, Pathfinder.d2r(80)),
				new Waypoint(-20, -15, Pathfinder.d2r(-20)) };
		Waypoint[] baseline = new Waypoint[] { new Waypoint(-20, -15, Pathfinder.d2r(-20)),
				new Waypoint(-150, 30, Pathfinder.d2r(180)) };

		scheduler.schedule(CommandUtil.combineSequential(cont.new PathFollowCommand(hopper, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(goal, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(baseline, false, -180)));
	}

	@Override
	protected void update() {
		scheduler.update();

	}

}
