package simulation;

import java.util.ArrayList;

import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;

import com.team1389.command_framework.CommandScheduler;
import com.team1389.command_framework.CommandUtil;
import com.team1389.command_framework.command_base.Command;
import com.team1389.motion_profile.MotionProfile;
import com.team1389.motion_profile.ProfileUtil;
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

	boolean pressed = false;
	SimulationRobot robot;
	// SimJoystick joy = new SimJoystick(1);
	SimboticsDriveSystem drive;

	Watcher dash;
	SystemManager manager = new SystemManager();
	Image map;

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		map.draw(0, 0, width, height);
		robot.render(container, g);
		g.setColor(Color.red);
		g.drawString("Vehicle Vel: " + Math.floor(robot.getVelocity() / 12) + " ft/sec", 0, 0);
	}

	Command driveDistanceCommand;
	CommandScheduler sched;
	@Override
	public void init(GameContainer arg0) throws SlickException {
		KeyboardHardware hardware = new KeyboardHardware();
		dash = new Watcher();
		try {
			map = new Image("2017-Field.png");
		} catch (SlickException e) {
			e.printStackTrace();
		}
		ArrayList<Line> lines = new ArrayList<Line>();
		int buffer = 0;
		lines.add(new Line(buffer, buffer, buffer, height - buffer));
		lines.add(new Line(buffer, height - buffer, width - buffer, height - buffer));
		lines.add(new Line(width - buffer, height - buffer, width - buffer, buffer));
		lines.add(new Line(width - buffer, buffer, buffer, buffer));

		robot = new SimulationRobot(lines, true);
		drive = new SimboticsDriveSystem(robot.getDrive(), Axis.make(hardware, Key.UP, Key.DOWN, 0.5),
				Axis.make(hardware, Key.LEFT, Key.RIGHT, 0.5));
		MotionProfile trapezoidal = ProfileUtil.trapezoidal(48, 0, 24, 24, 120);
		driveDistanceCommand = CommandUtil.combineSimultaneous(robot.leftProf.followProfileCommand(trapezoidal),
				robot.rightProf.followProfileCommand(trapezoidal));
		sched=new CommandScheduler();
		sched.schedule(driveDistanceCommand);
		manager.register(drive);
		dash.watch(robot.leftIn.getWatchable("pos"));
		dash.watch(drive);

	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		sched.update();
		//manager.update();
		dash.publish(Watcher.DASHBOARD);
		robot.update(delta);

	}

}
