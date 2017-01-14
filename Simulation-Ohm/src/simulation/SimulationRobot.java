package simulation;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

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

	double leftDistance = 0;
	double rightDistance = 0;
	double startX = 250;
	double startY = 250;
	int robotWidth = (int) (68 * DriveSimulator.scale);
	int robotHeight = (int) (70 * DriveSimulator.scale);
	SimulationField field;
	private boolean collision = false;

	public static final float collisionReboundDistancePerTick = 0.01f;

	/**
	 * 
	 * @param boundries
	 * @param collision True if collision is enabled, false if not
	 */
	public SimulationRobot(SimulationField field, boolean collision) {
		this(field);
		this.collision = collision;
	}

	public SimulationRobot(SimulationField field) {
		state.reset(Timer.getFPGATimestamp(), new RigidTransform2d(new Translation2d(), new Rotation2d()));
		try {
			robot = new Image("robot.png").getScaledCopy(robotWidth, robotHeight);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		this.field = field;

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
			for (Line l : field.getLines()) {
				while (checkCollision(l)) {
					Vector2f translateDirection = new Vector2f((float) getHeadingDegrees());
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

		return new AngleIn<Position>(Position.class, this::getHeadingDegrees);
	}

	private float getX() {
		Translation2d trans = getPose().getTranslation();
		return 2 * (float) (trans.getX() + startX) + (extraTranslate != null ? extraTranslate.x : 0);
	}

	private float getY() {
		Translation2d trans = getPose().getTranslation();
		return 2 * (float) (trans.getY() + startY) + (extraTranslate != null ? extraTranslate.y : 0);
	}

	private RigidTransform2d getPose() {
		return state.getLatestFieldToVehicle().getValue();
	}

	protected ArrayList<Point> points = new ArrayList<Point>();

	public void render(GameContainer container, Graphics g) throws SlickException {

		// Drawing robot
		robot.setRotation((float) getHeadingDegrees());
		robot.setCenterOfRotation(robotWidth / 2, robotHeight / 2);
		robot.drawCentered(getX(), getY());

		// Render this stuff only if collision is enabled
		if (collision) {
			g.setLineWidth(2);
			g.setColor(Color.orange);
			g.draw(getBoundingBox());
		}
	}

	public double getHeadingDegrees() {
		return getPose().getRotation().getDegrees();
	}

	public double getHeadingRads() {
		return getPose().getRotation().getRadians();
	}

	private Polygon getBoundingBox() {
		float renderX = getX();
		float renderY = getY();
		Polygon r = new Polygon();
		r.addPoint(renderX - robotWidth / 2, renderY - robotWidth / 2);
		r.addPoint(renderX + robotWidth / 2, renderY - robotWidth / 2);
		r.addPoint(renderX + robotWidth / 2, renderY + robotWidth / 2);
		r.addPoint(renderX - robotWidth / 2, renderY + robotWidth / 2);
		r = (Polygon) r.transform(Transform.createRotateTransform((float) getHeadingRads(), renderX, renderY));
		return r;
	}

}
