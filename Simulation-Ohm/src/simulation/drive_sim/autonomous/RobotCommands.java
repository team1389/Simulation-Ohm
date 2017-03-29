package simulation.drive_sim.autonomous;

import com.team1389.auto.command.DriveStraightCommand;
import com.team1389.auto.command.TurnAngleCommand;
import com.team1389.configuration.PIDConstants;
import com.team1389.hardware.value_types.Percent;

import simulation.drive_sim.robot.OctoRobot;

public class RobotCommands {
	OctoRobot robot;

	public RobotCommands(OctoRobot software) {
		this.robot = software;
	}

	public class DriveStraight extends DriveStraightCommand {
		public DriveStraight(double distance) {
			this(distance, 50);
		}

		public DriveStraight(double distance, double speed) {
			super(new PIDConstants(1, .03, .02), new PIDConstants(0, 0, .0), robot.tank.getDrive(), robot.tank.leftIn,
					robot.tank.rightIn, robot.getGyro().copy().invert(), distance, 40, 40, speed, .05);
		}
	}

	public class TurnAngle extends TurnAngleCommand<Percent> {

		public TurnAngle(double angle, boolean absolute) {
			super(angle, absolute, 2, robot.getGyro(), TurnAngleCommand.createTurnController(robot.tank.getDrive()),
					new PIDConstants(0.05, .001, .5));
		}

	}
}