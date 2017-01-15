package simulation;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Point;

import com.team1389.hardware.inputs.software.DigitalIn;
import com.team1389.hardware.inputs.software.PercentIn;
import com.team1389.system.SystemManager;
import com.team1389.system.drive.SimboticsDriveSystem;
import com.team1389.watch.Watcher;
import com.team1389.watch.info.StringInfo;

import net.java.games.input.Component.Identifier.Key;
import simulation.input.Axis;
import simulation.input.KeyboardHardware;
import simulation.input.SimJoystick;

public class DriveSimulator extends BasicGame {
	static double scale = .75;
	static final int width = (int) (1432 * scale);
	static final int height = (int) (753 * scale);

	private SimulationTankDrive robot;
	private SimboticsDriveSystem drive;
	private SimulationField field;

	private Watcher dash;
	private SystemManager manager = new SystemManager();

	public DriveSimulator(String title) {
		super(title);
	}

	public static void main(String[] args) throws SlickException {
		Simulator.initWPILib();
		DriveSimulator sim = new DriveSimulator("DriveSim");
		AppGameContainer cont = new AppGameContainer(sim);
		cont.setTargetFrameRate(50);
		cont.setDisplayMode(width, height, false);
		cont.start();
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		field.render(g);

		robot.render(container, g);
		g.setColor(Color.red);
		g.drawString("Vehicle Vel: " + Math.floor(robot.getVelocity() / 12) + " ft/sec", 0, 0);

	}

	DigitalIn controlZ;

	@Override
	public void init(GameContainer arg0) throws SlickException {
		KeyboardHardware hardware = new KeyboardHardware();
		dash = new Watcher();
		controlZ = hardware.getKey(Key.LCONTROL).combineAND(hardware.getKey(Key.Z)).getLatched();
		field = new SimulationField(width, height);
		robot = new SimulationTankDrive(field);
		SimJoystick joy = new SimJoystick(0);
		PercentIn a0 = joy.isPresent() ? joy.getAxis(0).scale(2).limit(1).invert().applyDeadband(.2) : Axis.make(hardware, Key.UP, Key.DOWN, 1);
		PercentIn a1 = joy.isPresent() ? joy.getAxis(1).scale(2).limit(1).invert().applyDeadband(.2) : Axis.make(hardware, Key.LEFT, Key.RIGHT, 1);
		drive = new SimboticsDriveSystem(robot.getDrive(), a0, a1);
		manager.register(drive);
		dash.watch(drive,new StringInfo("axis2",a0::toString));
		new XMLWriter().readFromXML().forEach(field::addPoint);

	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		manager.update();
		dash.publish(Watcher.DASHBOARD);
		robot.update(delta);
		Input input = gc.getInput();
		int xpos = input.getMouseX();
		int ypos = input.getMouseY();

		if (input.isMousePressed(0)) {
			field.addPoint(new Point(xpos, ypos));
		}
		if (input.isMousePressed(1)) {
			field.addPoint(SimulationField.DoesNotExist);
		}

		if (controlZ.get()) {
			field.removeLast();
		}

	}

	@Override
	public boolean closeRequested() {
		XMLWriter reader = new XMLWriter();
		reader.saveToXML(field.points);
		System.exit(0); // Use this if you want to quit the app.
		return false;
	}
}
