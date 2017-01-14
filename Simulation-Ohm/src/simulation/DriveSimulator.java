package simulation;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.command_base.Command;
import com.team1389.hardware.inputs.interfaces.BinaryInput;
import com.team1389.hardware.inputs.software.DigitalIn;
import com.team1389.system.SystemManager;
import com.team1389.system.drive.SimboticsDriveSystem;
import com.team1389.watch.Watcher;

import net.java.games.input.Component.Identifier.Key;
import simulation.input.Axis;
import simulation.input.KeyboardHardware;

public class DriveSimulator extends BasicGame {
	static double scale = .75;
	static final int width = (int) (1432 * scale);
	static final int height = (int) (753 * scale);


	private Command driveDistanceCommand;
	private CommandScheduler sched;
	//private boolean pressed = false;
	private SimulationRobot robot;
	// SimJoystick joy = new SimJoystick(1);
	private SimboticsDriveSystem drive;
	private Watcher dash;
	private SystemManager manager = new SystemManager();
	private Image map;
	private SimulationField field;
	
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

		//Checking for new clicks to render boundries
		Input input = container.getInput();
		int xpos = input.getMouseX();
		int ypos = input.getMouseY();

		if (input.isMousePressed(0)) {
			field.addPoint(new Point(xpos, ypos));
		}
		if (input.isMousePressed(1)) {
			field.addPoint(SimulationField.DoesNotExist);
		}

		if(controlZ.get()){
			field.removeLast();
		}
		
		map.draw(0, 0, width, height);
		robot.render(container, g);
		g.setColor(Color.red);
		g.drawString("Vehicle Vel: " + Math.floor(robot.getVelocity() / 12) + " ft/sec", 0, 0);

	}

	DigitalIn controlZ;
	
	@Override
	public void init(GameContainer arg0) throws SlickException {
		
		KeyboardHardware hardware = new KeyboardHardware();
		dash = new Watcher();
		try {
			map = new Image("2017-Field.png");
		} catch (SlickException e) {
			e.printStackTrace();
		}
		
		controlZ = hardware.getKey(Key.LCONTROL).combineAND(hardware.getKey(Key.Z)).getLatched();
		
		ArrayList<Line> lines = new ArrayList<Line>();
		int buffer = 0;
		lines.add(new Line(buffer, buffer, buffer, height - buffer));
		lines.add(new Line(buffer, height - buffer, width - buffer, height - buffer));
		lines.add(new Line(width - buffer, height - buffer, width - buffer, buffer));
		lines.add(new Line(width - buffer, buffer, buffer, buffer));
		field = new SimulationField(lines);
		
		robot = new SimulationRobot(field, true);
		drive = new SimboticsDriveSystem(robot.getDrive(), Axis.make(hardware, Key.UP, Key.DOWN, 0.5),
				Axis.make(hardware, Key.LEFT, Key.RIGHT, 0.5));
		sched=new CommandScheduler();
		sched.schedule(driveDistanceCommand);
		manager.register(drive);
		dash.watch(robot.leftIn.getWatchable("pos"));
		dash.watch(drive);

	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {

		//sched.update();
		manager.update();
		dash.publish(Watcher.DASHBOARD);
		robot.update(delta);

	}
	@Override 
	 public boolean closeRequested()
    {
		XMLWriter reader = new XMLWriter();
		reader.saveToXML(robot.points);
      System.exit(0); // Use this if you want to quit the app.
      return false;
    }
}
