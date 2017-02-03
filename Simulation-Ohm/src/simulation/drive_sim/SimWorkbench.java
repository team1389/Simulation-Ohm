package simulation.drive_sim;

import java.util.concurrent.CompletableFuture;

import com.team1389.watch.Watcher;

import simulation.drive_sim.robot.SimulationRobot;
import simulation.input.KeyboardJoystickEmulator;
import simulation.input.SimJoystick;

public abstract class SimWorkbench {
	protected SimulationRobot robot;
	protected Watcher dash;
	protected SimJoystick joy;

	public SimWorkbench(SimulationRobot robot) {
		this.robot = robot;
		dash = new Watcher();
		SimJoystick joy = new SimJoystick(0);
		this.joy = joy.isPresent() ? joy : new KeyboardJoystickEmulator();
	}

	public void init() {
		CompletableFuture.runAsync(Watcher::updateWatchers);
		dash.outputToDashboard();
		initialize();
	}

	protected abstract void initialize();

	protected abstract void update();

	public void updateParent() {
		update();
	}
}
