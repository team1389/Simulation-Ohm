package simulation;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

import com.team1389.control.MotionProfileController;
import com.team1389.hardware.inputs.software.AngleIn;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Percent;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Speed;
import com.team1389.system.drive.DriveOut;
import com.team1389.trajectory.Kinematics;
import com.team1389.trajectory.RigidTransform2d;
import com.team1389.trajectory.RigidTransform2d.Delta;
import com.team1389.trajectory.RobotState;
import com.team1389.trajectory.Rotation2d;
import com.team1389.trajectory.Translation2d;

import edu.wpi.first.wpilibj.Timer;
import simulation.motor.Attachment;
import simulation.motor.Motor;
import simulation.motor.Motor.MotorType;
import simulation.motor.MotorSystem;
import simulation.motor.element.CylinderElement;

public class SimulationRobot {
	Image robot;
	MotorSystem left = new MotorSystem(new Attachment(new CylinderElement(.51, .097), false), 6,
			new Motor(MotorType.CIM));
	MotorSystem right = new MotorSystem(new Attachment(new CylinderElement(.51, .097), false), 6,
			new Motor(MotorType.CIM));
	RobotState state = new RobotState();
	AngleIn<Position> gyro = new AngleIn<Position>(Position.class,
			() -> state.getLatestFieldToVehicle().getValue().getRotation().getDegrees());
	RangeIn<Position> leftIn = left.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Position> rightIn = right.getPositionInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> leftVel = left.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	RangeIn<Speed> rightVel = right.getSpeedInput().mapToRange(0, 1).scale(Math.PI * 7.65);
	MotionProfileController leftProf = new MotionProfileController(.01, 0, 0, 0, leftIn, leftVel,
			left.getVoltageOutput());
	MotionProfileController rightProf = new MotionProfileController(.01, 0, 0, 0, rightIn, rightVel,
			right.getVoltageOutput());

	double leftDistance = 0;
	double rightDistance = 0;
	double startX = 250;
	double startY = 250;
	int robotWidth = (int) (68 * DriveSimulator.scale);
	int robotHeight = (int) (70 * DriveSimulator.scale);
	ArrayList<Line> boundries;
	private boolean collision = false;

	public static final float collisionReboundDistancePerTick = 0.01f;

	/**
	 * 
	 * @param boundries
	 * @param collision True if collision is enabled, false if not
	 */
	public SimulationRobot(ArrayList<Line> boundries, boolean collision) {
		this(boundries);
		this.collision = collision;
	}

	public SimulationRobot(ArrayList<Line> boundries) {
		state.reset(Timer.getFPGATimestamp(), new RigidTransform2d(new Translation2d(), new Rotation2d()));
		try {
			robot = new Image("robot.png").getScaledCopy(robotWidth, robotHeight);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		this.boundries = boundries;

	}

	public SimulationRobot() {
		this(new ArrayList<Line>());
	}

	private Vector2f extraTranslate = null;
	double velocity;

	public void update(double dt) {

		left.update();
		right.update();
		Delta velocity = new Kinematics(10, 23, .8).forwardKinematics(leftIn.get() - leftDistance,
				rightIn.get() - rightDistance);
		state.addObservations(Timer.getFPGATimestamp(),
				state.getLatestFieldToVehicle().getValue().transformBy(RigidTransform2d.fromVelocity(velocity)),
				velocity);
		this.velocity = Math.sqrt(Math.pow(velocity.dx, 2) + Math.pow(velocity.dy, 2)) * 1000 / dt;
		leftDistance = leftIn.get();
		rightDistance = rightIn.get();

		if (collision) {
			for (Line l : boundries) {
				while (checkCollision(l)) {
					Vector2f translateDirection = new Vector2f(getHeadingIn().get());
					Vector2f unitVector = translateDirection.scale(1 / translateDirection.length());
					Vector2f antiUnitVector = unitVector.copy().scale(-1);
					double secondDistance = l.distance(new Vector2f(getX(), getY()).add(unitVector));
					double thirdDistance = l.distance(new Vector2f(getX(), getY()).add(antiUnitVector));

					if (secondDistance > thirdDistance) {
						extraTranslate = unitVector.scale(collisionReboundDistancePerTick)
								.add(extraTranslate != null ? extraTranslate : new Vector2f(0, 0));
					} else {
						extraTranslate = antiUnitVector.scale(collisionReboundDistancePerTick)
								.add(extraTranslate != null ? extraTranslate : new Vector2f(0, 0));
					}

				}
			}

		}
	}

	Line someLine = null;
	Line toPrint = null;
	Point tp = null;

	public double getVelocity() {
		return velocity;
	}

	public boolean checkCollision(Line l) {
		return getBoundingBox().intersects(l);
	}

	public DriveOut<Percent> getDrive() {
		return new DriveOut<Percent>(left.getVoltageOutput(), right.getVoltageOutput());
	}

	public AngleIn<Position> getHeadingIn() {

		return new AngleIn<Position>(Position.class,
				() -> state.getLatestFieldToVehicle().getValue().getRotation().getDegrees());
	}

	private float getX() {
		Translation2d trans = getTransform().getTranslation();
		return 2 * (float) (trans.getX() + startX) + (extraTranslate != null ? extraTranslate.x : 0);
	}

	private float getY() {
		Translation2d trans = getTransform().getTranslation();
		return 2 * (float) (trans.getY() + startY) + (extraTranslate != null ? extraTranslate.y : 0);
	}

	private RigidTransform2d getTransform() {
		return state.getLatestFieldToVehicle().getValue();
	}

	private ArrayList<Point> points = new ArrayList<Point>();

	public void render(GameContainer container, Graphics g) throws SlickException {

		// Drawing robot
		Rotation2d rot = getTransform().getRotation();
		float renderX = getX();
		float renderY = getY();
		robot.setRotation((float) rot.getDegrees());
		robot.setCenterOfRotation(robotWidth / 2, robotHeight / 2);
		robot.drawCentered(renderX, renderY);
		g.setColor(Color.white);
		g.fillOval(renderX - 5, renderY - 5, 10, 10);

		// Render this stuff only if collision is enabled
		if (collision) {
			// Checking for new clicks to render boundries
			Input input = container.getInput();
			int xpos = input.getMouseX();
			int ypos = input.getMouseY();

			if (input.isMousePressed(0)) {
				points.add(new Point(xpos, ypos));
			}
			if (input.isMousePressed(1)) {
				points.add(null);
			}

			if (points.size() > 1) {
				Point point1 = points.get(points.size() - 1);
				Point point2 = points.get(points.size() - 2);
				if (point1 != null && point2 != null) {
					boundries.add(new Line(point1.getX(), point1.getY(), point2.getX(), point2.getY()));
				}
			}

			// Drawing collision helpers
			g.setLineWidth(2);
			g.setColor(Color.orange);
			for (Line l : boundries) {
				g.draw(l);
			}

			g.draw(getBoundingBox());
			if (someLine != null) {
				g.setColor(Color.green);
				g.draw(someLine);
				someLine = null;
			}

			g.setLineWidth(4);
			if (toPrint != null) {
				g.setColor(Color.magenta);
				g.draw(toPrint);
				g.fillOval(tp.getX() - 5, tp.getY() - 5, 10, 10);
			}
			toPrint = null;
		}

	}

	private Polygon getBoundingBox() {
		Rotation2d rot = getTransform().getRotation();
		float renderX = getX();
		float renderY = getY();
		Polygon r = new Polygon();
		r.addPoint(renderX - robotWidth / 2, renderY - robotWidth / 2);
		r.addPoint(renderX + robotWidth / 2, renderY - robotWidth / 2);
		r.addPoint(renderX + robotWidth / 2, renderY + robotWidth / 2);
		r.addPoint(renderX - robotWidth / 2, renderY + robotWidth / 2);

		r = (Polygon) r.transform(Transform.createRotateTransform((float) rot.getRadians(), renderX, renderY));
		return r;
	}

}
