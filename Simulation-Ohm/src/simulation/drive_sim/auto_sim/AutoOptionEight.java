package simulation.drive_sim.auto_sim;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;

import jaci.pathfinder.Waypoint;
import jaci.pathfinder.Pathfinder;
import simulation.drive_sim.PathFollowingSystem;
import simulation.drive_sim.PathFollowingSystem.Constants;
import simulation.drive_sim.SimWorkbench;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class AutoOptionEight extends SimWorkbench {
	CommandScheduler scheduler;
	PathFollowingSystem cont;

	public AutoOptionEight(RenderableRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
		init();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void initialize() {
		// TODO Auto-generated method stub
		robot.getStartPos().getTranslation();
		OctoRobot robot = (OctoRobot) this.robot;
		PathFollowingSystem.Constants constants = new Constants(200, 20, 240, .17, .004, 0, 0.65, .6);
		cont = new PathFollowingSystem(robot.tank.getDrive(), robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);
		Waypoint[] feeder = new Waypoint[] { new Waypoint(0, 50, 0), new Waypoint(-75, -10, Pathfinder.d2r(90)) };
		Waypoint[] boiler = new Waypoint[] { new Waypoint(0, 10, Pathfinder.d2r(80)), new Waypoint(44, 28, Pathfinder.d2r(-30)) };
		Waypoint[] airship = new Waypoint[] { new Waypoint(0, 30, Pathfinder.d2r(0)), new Waypoint(-90, 70, Pathfinder.d2r(180)) };
		
		scheduler.schedule(CommandUtil.combineSequential(cont. new PathFollowCommand(feeder, false, 180), CommandUtil.createCommand(robot.tank::reset), 
				cont.new PathFollowCommand(boiler, false, -180), CommandUtil.createCommand(robot.tank::reset), cont. new PathFollowCommand(airship, false, 180)));

	}

	@Override
	protected void update() {
		// TODO Auto-generated method stub
		scheduler.update();
	}

}
