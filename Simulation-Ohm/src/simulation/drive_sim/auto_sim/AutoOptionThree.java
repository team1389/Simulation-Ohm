package simulation.drive_sim.auto_sim;

import com.team1389.command_framework.CommandScheduler;

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
		Waypoint[] points = new Waypoint[] { new Waypoint(0, 30, 0), new Waypoint(-101, 25, 0) };
		Waypoint[] points2 = new Waypoint[] { new Waypoint(0, 0, Pathfinder.d2r(-60)),
				new Waypoint(18, -90, Pathfinder.d2r(-90)) };

		PathFollowingSystem.Constants constants = new PathFollowingSystem.Constants(100, 20, 240, .1, .0025, 0, 0.70,
				.6);
		cont = new PathFollowingSystem(robot.tank.getDrive(), robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);
		Trajectory traj3 = /*Pathfinder.readFromFile(new File("third.traj"));*/cont.generateTrajectory(new Waypoint[] { new Waypoint(0, 0, Pathfinder.d2r(-90)), new Waypoint(63, 16, Pathfinder.d2r(120)) });
				
		traj = /*Pathfinder.readFromFile(
				new File("first.traj"));*/
										 cont.generateTrajectory(new Waypoint[] { new Waypoint(0,
										 30, 0), new Waypoint(30, 60,
										 Pathfinder.d2r(-180)) });
										

		Trajectory traj2 = /*Pathfinder.readFromFile(new File("second.traj")); */ cont.generateTrajectory(points2);
		/*scheduler.schedule(CommandUtil.combineSequential(cont.new PathFollowCommand(points, false, -180),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(traj2, true, 0),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(traj3, false, 180),
				new TurnAngleCommand<>(-180, .05, robot.getGyro().invert(),
						TurnAngleCommand.createTurnController(robot.tank.getDrive()), new PIDConstants(.03, 0, 0)),
				CommandUtil.createCommand(robot.tank::reset), cont.new PathFollowCommand(traj, false, 180)));*/
		scheduler.schedule(cont.new PathFollowCommand(points, false, -180));
	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
