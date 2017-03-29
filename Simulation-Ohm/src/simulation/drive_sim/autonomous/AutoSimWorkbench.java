package simulation.drive_sim.autonomous;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;
import com.team1389.command_framework.command_base.Command;

import simulation.drive_sim.SimWorkbench;
import simulation.drive_sim.robot.OctoRobot;

public class AutoSimWorkbench extends SimWorkbench {
	CommandScheduler scheduler;
	RobotCommands commands;
	Command val;

	public AutoSimWorkbench(OctoRobot robot) {
		super(robot);
		this.commands = new RobotCommands(robot);
		scheduler = new CommandScheduler();
		val = CommandUtil.combineSequential(commands.new DriveStraight(25), commands.new DriveStraight(-25));
		initialize();
	}

	@Override
	protected void initialize() {
		scheduler.schedule(val);
	}

	@Override
	protected void update() {
		scheduler.update();
		if (scheduler.isFinished()) {
			scheduler.schedule(val);
		}
	}

}
