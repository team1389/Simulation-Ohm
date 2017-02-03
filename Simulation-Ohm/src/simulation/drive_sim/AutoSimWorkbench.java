package simulation.drive_sim;

import com.team1389.auto.command.TurnAngleCommand;
import com.team1389.command_framework.CommandScheduler;
import com.team1389.configuration.PIDConstants;
import com.team1389.hardware.value_types.Percent;
import com.team1389.system.drive.DriveOut;

import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.SimulationRobot;

public class AutoSimWorkbench extends SimWorkbench {
	CommandScheduler scheduler;

	public AutoSimWorkbench(SimulationRobot robot) {
		super(robot);
		scheduler = new CommandScheduler();
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		DriveOut<Percent> asTank = robot.getWheels().getAsTank();
		dash.watch(asTank);
		scheduler.schedule(new TurnAngleCommand<>(90, 1, robot.getGyro(),
				TurnAngleCommand.createTurnController(asTank).invert(),
				new PIDConstants(.01, 0, 0, 0)));
		dash.watch(robot.getGyro().getWatchable("gyro"));
	}

	@Override
	protected void update() {
		scheduler.update();
	}

}
