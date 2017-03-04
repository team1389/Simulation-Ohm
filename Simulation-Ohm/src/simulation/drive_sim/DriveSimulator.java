package simulation.drive_sim;


import java.io.File;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Vector2f;

import com.team1389.hardware.inputs.software.AngleIn;
import com.team1389.hardware.inputs.software.DigitalIn;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Value;
import com.team1389.trajectory.RigidTransform2d;
import com.team1389.trajectory.RobotState;
import com.team1389.trajectory.RobotStateEstimator;
import com.team1389.util.RangeUtil;
import com.team1389.util.Timer;

import net.java.games.input.Component.Identifier.Key;
import simulation.Simulator;
import simulation.drive_sim.field.SimulationField;
import simulation.drive_sim.network.NetworkPosition;
import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;
import simulation.drive_sim.robot.TankDriveTrain;
import simulation.drive_sim.xml.XMLShapeReader;
import simulation.drive_sim.xml.XMLShapeWriter;
import simulation.input.KeyboardHardware;

public class DriveSimulator extends BasicGame {
	public static float scale = 1f;
	public static final int width = (int) (716 * scale);
	public static final int height = (int) (376 * scale);
	public static final double MATCH_TIME_SECONDS = 135;
	private RenderableRobot robot;
	private SimulationField field;
	private SimWorkbench workbench;
	private Timer timer;
	DigitalIn controlZ;	
	public RigidTransform2d measuredTransform;

	private RobotStateEstimator estimator;
	private NetworkPosition network;
	private RangeIn<Value> gearsDelivered = new RangeIn<Value>(Value.class, 
			() -> (double)robot.getGearsDelivered(), 0.0, 1.0).addChangeListener((n) -> gearCollected(),true);
	
	public DriveSimulator(String title) {
		super(title);
		timer = new Timer();
	}

	public static void main(String[] args) throws SlickException {
		System.setProperty("org.lwjgl.librarypath", new File("native/" + getOsName()).getAbsolutePath());
		Simulator.initWPILib();
		DriveSimulator sim = new DriveSimulator("DriveSim");
		AppGameContainer cont = new AppGameContainer(sim);
		cont.setTargetFrameRate(70);
		cont.setFullscreen(true);
		cont.setDisplayMode(width, height, false);
		cont.start();
	}

	private static String getOsName() {
		String property = System.getProperty("os.name");
		return property.toLowerCase().substring(0, property.indexOf(' '));
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException {
		field.render(g);
		robot.render(container, g, estimator.get());
		if (robot.isEnabled()) {
			// field.renderVisibility();
		}
		workbench.render(g);

		g.setColor(Color.red);
		g.drawString("Vehicle Vel: " + Math.floor(robot.getVelocity() / 12) + " ft/sec", 0, 0);
		double totalSecs = timer.getSinceMark();
		totalSecs = RangeUtil.limit(totalSecs, 0, MATCH_TIME_SECONDS);
		int minutes = (int) (totalSecs % 3600) / 60;
		int seconds = (int) totalSecs % 60;
		g.drawString("Match time: " + minutes + ":" + (seconds < 10 ? "0" : "") + seconds, 0, 15);
		g.drawString("Gears placed: " + robot.getGearsDelivered(), 0, 30);
		float x = Mouse.getX();
		float y = height - Mouse.getY();
		//double xInches = XFromRobotInches(pixelsToInches(x));
		//double yInches = YFromRobotInches(pixelsToInches(y));
		g.drawString(format(robot.getRenderX()) + " " + format(robot.getRenderY()) + " " + robot.getGyro().get(), x, y - 15);
		Vector2f gyro = new Vector2f((float) robot.getGyro().get()).scale(20);
		g.setLineWidth(5);
		g.drawLine(30, 80, gyro.x + 30, gyro.y + 80);
		if (robot.hasGear())
			g.setColor(Color.green);
		g.fillOval(60, 60, 20, 20);

	}
	public double format(double initial){
		return ((int)initial*10)/10;
	}
	public double pixelsToInches(double pixels) {
		return pixels / scale;
	}

	public double XFromRobotInches(double coordInches) {
		return coordInches - robot.getAdjustedPose().getTranslation().getX();
	}

	public double YFromRobotInches(double coordInches) {
		return coordInches - robot.getAdjustedPose().getTranslation().getY();
	}

	private void startMatch() {
		timer.mark();
		workbench.init();
		robot.startMatch();
		new RangeIn<Value>(Value.class, timer::getSinceMark, 0, 0).addChangeListener(d -> {
			if (d > MATCH_TIME_SECONDS && robot.isEnabled()) {
				robot.disable();
			}
		},true);
	}

	@Override
	public void init(GameContainer arg0) throws SlickException {
		field = new SimulationField(width, height);
		robot = new OctoRobot(field, Alliance.RED);
		workbench = new DriverSimWorkbench(robot);

		TankDriveTrain drive = ((OctoRobot)robot).getTank();
		RobotState temp = new RobotState();
		temp.reset(0, robot.getStartPos());


		estimator = new RobotStateEstimator(new RobotState(), drive.leftIn.getInches() , drive.rightIn.getInches(), drive.leftVel.mapToRange(0, 1).scale(4 * 2 * Math.PI), drive.rightVel.mapToRange(0, 1).scale(4 * 2 * Math.PI), new AngleIn<Position>(Position.class ,() -> robot.getRelativeHeadingDegrees()), 10, 23, .6);
		network = new NetworkPosition(estimator);
		
		//TODO: Switch between mecanum and tank


		KeyboardHardware hardware = new KeyboardHardware();
		controlZ = hardware.getKey(Key.LCONTROL).combineAND(hardware.getKey(Key.Z)).latched();

		XMLShapeReader reader = new XMLShapeReader("boundaries.xml");
		reader.getBoundaries().forEach(field::addBoundary);
		reader.getDropoffs().forEach(field::addDropoff);
		reader.getPickups().forEach(field::addPickup);
		// startMatch();
		
		
	}

	@Override
	public void update(GameContainer gc, int delta) throws SlickException {
		robot.update(delta);
		workbench.updateParent();
		/*Input input = gc.getInput();
		int xpos = input.getMouseX();
		int ypos = input.getMouseY();
		if (input.isKeyDown(Input.KEY_C)) {
			startMatch();
		}
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
		}*/
		
		//double accel = robot.getAcceleration();
		//System.out.println(accel);
		//gearsDelivered.get();
		network.updateNetwork(2);
	}
	
	private void gearCollected(){
		network.updateNetwork(3);
		network.updateNetwork(2);
	}

	@Override
	public boolean closeRequested() {
		new XMLShapeWriter("boundaries.xml").writeShapes(field.getBoundries(), field.getGearPickups(),
				field.getGearDropoffs());
		System.exit(0);
		return false;
	}
}
