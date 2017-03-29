package simulation.drive_sim;

import org.newdawn.slick.Graphics;

import com.team1389.concurrent.OhmThreadService;
import com.team1389.watch.Watcher;

import simulation.drive_sim.robot.RenderableRobot;
import simulation.input.KeyboardJoystickEmulator;
import simulation.input.SimJoystick;

public abstract class SimWorkbench {
	protected RenderableRobot robot;
	protected Watcher dash;
	protected SimJoystick joy;

	public SimWorkbench(RenderableRobot robot) {
		this.robot = robot;
		dash = new Watcher();
		SimJoystick joy = new SimJoystick(0);
		this.joy = joy.isPresent() ? joy : new KeyboardJoystickEmulator();
		new OhmThreadService(1000).init();
	}

	public void init() {
		dash.outputToDashboard();
		initialize();
	}
	public void render(Graphics g){
		
	}
	protected abstract void initialize();

	protected abstract void update();

	public void updateParent() {
		update();
	}
}
