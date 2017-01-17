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
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Value;
import com.team1389.system.drive.BezierCurve;
import com.team1389.system.drive.CheesyDriveSystem;
import com.team1389.system.drive.DriveSystem;
import com.team1389.system.drive.MecanumDriveSystem;
import com.team1389.util.RangeUtil;
import com.team1389.util.Timer;
import com.team1389.watch.Watcher;

import net.java.games.input.Component.Identifier.Key;
import simulation.input.Axis;
import simulation.input.KeyboardHardware;
import simulation.input.SimJoystick;

public class DriveSimulator extends BasicGame {
	static double scale = 1;
	static final int width = (int) (1432 * scale);
	static final int height = (int) (753 * scale);
	static final double MATCH_TIME_SECONDS = 135;
	private SimulationRobot robot;
	private DriveSystem drive;
	private SimulationField field;
	private Watcher dash;
	private Timer timer;
	DigitalIn controlZ;

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
		double totalSecs = timer.getSinceMark();
		totalSecs = RangeUtil.limit(totalSecs, 0, MATCH_TIME_SECONDS);
		int minutes = (int) (totalSecs % 3600) / 60;
		int seconds = (int) totalSecs % 60;

		g.drawString("Match time: " + minutes + ":" + (seconds < 10 ? "0" : "") + seconds, 0, 15);
		g.drawString("Gears placed: " + robot.getGearsDelivered(), 0, 30);

	}

	private void startMatch() {
		timer.mark();
		robot.startMatch();
		new RangeIn<Value>(Value.class, timer::getSinceMark, 0, 0).addChangeListener(d -> {
			if (d > MATCH_TIME_SECONDS && !robot.disabled) {
				robot.disable();
			}
		});
	}

	@Override
	public void init(GameContainer arg0) throws SlickException {
		timer = new Timer();
		KeyboardHardware hardware = new KeyboardHardware();
		dash = new Watcher();
		controlZ = hardware.getKey(Key.LCONTROL).combineAND(hardware.getKey(Key.Z)).getLatched();
		field = new SimulationField(width, height);
		MecanumDriveTrain mec = new MecanumDriveTrain();
		TankDriveTrain tank = new TankDriveTrain();

		robot = new SimulationRobot(field, tank);
		SimJoystick joy = new SimJoystick(0);
		PercentIn a0 = joy.isPresent() ? joy.getAxis(0).applyDeadband(.1).scale(2).limit(1).invert()
				: Axis.make(hardware, Key.W, Key.S, 1);
		PercentIn a1 = joy.isPresent() ? joy.getAxis(1).scale(2).applyDeadband(.1).limit(1).invert()
				: Axis.make(hardware, Key.A, Key.D, 1);
		PercentIn a2 = joy.isPresent() ? joy.getAxis(2).scale(.4).applyDeadband(.075).limit(1)
				: Axis.make(hardware, Key.Q, Key.R, 1);
		DigitalIn toggle = (joy.isPresent() ? joy.getButton(0) : hardware.getKey(Key.SPACE));
		BezierCurve xCurve = new BezierCurve(0, .5, .79, -0.06);
		BezierCurve yCurve = new BezierCurve(.0, 0.54, 0.45, -0.07);

		DriveSystem mecD = new MecanumDriveSystem(a1.copy().invert(), a0.copy().invert(), a2.copy(), mec.getTop(),
				mec.getBottom(), robot.getHeadingIn(), toggle);
		a0.map(d -> yCurve.getPoint(d).getY());
		a1.map(d -> xCurve.getPoint(d).getY());
		DriveSystem tankD = new CheesyDriveSystem(tank.getDrive(), a0, a1.sumInputs(a2.invert().scale(0.25)), toggle,
				0.75, .75);

		drive = tankD;
		(joy.isPresent() ? joy.getButton(2) : hardware.getKey(Key.LCONTROL)).getToggled().invert()
				.addChangeListener(b -> {
					robot.setDriveTrain(b ? tank : mec);
					drive = (b ? tankD : mecD);
				});
		(joy.isPresent() ? joy.getButton(3) : hardware.getKey(Key.C)).getLatched().addChangeListener(b -> {
			startMatch();
		});
		dash.watch(drive);
		XMLShapeReader reader = new XMLShapeReader("boundaries.xml");
		reader.getBoundaries().forEach(field::addBoundary);
		reader.getDropoffs().forEach(field::addDropoff);
		reader.getDropoffs().forEach(field::addPickup);
		startMatch();
	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		drive.update();
		dash.publish(Watcher.DASHBOARD);
		robot.update(delta);
		Input input = gc.getInput();
		int xpos = input.getMouseX();
		int ypos = input.getMouseY();

		if (input.isMousePressed(0)) {
			field.addPoint(new Point(xpos, ypos));
		}
		if (input.isMousePressed(1)) {
			field.finishBoundry();
		}
		if (input.isKeyPressed(Input.KEY_P)) {
			field.finishGearPickup();
		}
		if (input.isKeyPressed(Input.KEY_O)) {
			field.finishGearDropoff();
		}

	}

	@Override
	public boolean closeRequested() {
		new XMLShapeWriter("boundaries.xml").writeShapes(field.getBoundries(), field.getGearPickups(),
				field.getGearPickups());
		System.exit(0);
		return false;
	}
}
