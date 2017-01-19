package simulation.drive_sim.field;

import org.newdawn.slick.geom.Shape;

import simulation.drive_sim.Alliance;
import simulation.drive_sim.robot.SimulationRobot;

public class AlliedBoundary {
	private Shape collision;
	private Alliance alliance;

	public AlliedBoundary(Shape collision, Alliance alliance) {
		this.collision = collision;
		this.alliance = alliance;
	}

	public boolean isRobotEligible(SimulationRobot robot) {
		return collision.contains(robot.getBoundingBox()) && isRobotFriendly(robot);
	}

	public boolean isRobotFriendly(SimulationRobot robot) {
		return robot.getAlliance() == alliance;
	}

	public Shape getBoundary() {
		return collision;
	}

	public Alliance getAlliance() {
		return alliance;
	}
}
