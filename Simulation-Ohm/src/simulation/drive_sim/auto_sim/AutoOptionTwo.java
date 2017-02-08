package simulation.drive_sim.auto_sim;

import java.util.Arrays;

import org.newdawn.slick.Graphics;

import com.team1389.command_framework.CommandScheduler;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import simulation.drive_sim.DriveSimulator;
import simulation.drive_sim.PathFollowingSystem;
import simulation.drive_sim.SimWorkbench;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class AutoOptionTwo extends SimWorkbench {
	/**
	 * This is a very simple auto that just moves forward across the baseline
	 * 
	 * @author raffi_000
	 */
	CommandScheduler scheduler;
	PathFollowingSystem cont;
	Trajectory traj = cont.generateTrajectory(new Waypoint[] { new Waypoint(0, 40, 0), new Waypoint(14, 30, Pathfinder.d2r(200)), new Waypoint(40, 50, Pathfinder.d2r(300)) });
	Trajectory traj1;
	Trajectory traj2;

	public AutoOptionTwo(RenderableRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
		initialize();
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		Waypoint[] p = new Waypoint[] { new Waypoint(0, 50, 0), new Waypoint(0, 100, 0), new Waypoint(0, 50, 1) };
		PathFollowingSystem.Constants constants = new PathFollowingSystem.Constants(100, 20, 240, .1, .0025, 0, 0.70,
				.6);
		cont = new PathFollowingSystem(robot.tank.getDrive(), robot.tank.leftIn.copy(), robot.tank.rightIn.copy(),
				robot.getGyro(), constants);
		scheduler.schedule(cont.new PathFollowCommand(p, false, 180));
		traj = cont.generateTrajectory(new Waypoint[] { new Waypoint(0, 40, 0), new Waypoint(14, 30, Pathfinder.d2r(200)), new Waypoint(40, 50, Pathfinder.d2r(300)) });
		
	}

	@Override
	protected void update() {
		scheduler.update();
	}

	public void render(Graphics g) {
		Arrays.stream(traj.segments).forEach(s -> {
			double segx = robot.getStartPos().getTranslation().getX() * DriveSimulator.scale
					- s.x * DriveSimulator.scale;
			double segy = robot.getStartPos().getTranslation().getY() * DriveSimulator.scale
					- s.y * DriveSimulator.scale;
			g.fillOval((float) segx - 5, (float) segy - 5, 10, 10);
		});
	}
}
