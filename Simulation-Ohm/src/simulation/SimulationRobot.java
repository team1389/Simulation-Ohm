package simulation;

import java.util.ArrayList;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
import org.newdawn.slick.geom.Point;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Shape;
import org.newdawn.slick.geom.Transform;
import org.newdawn.slick.geom.Vector2f;

import com.team1389.hardware.inputs.software.AngleIn;
import com.team1389.hardware.value_types.Position;
import com.team1389.trajectory.RigidTransform2d;
import com.team1389.trajectory.RigidTransform2d.Delta;
import com.team1389.trajectory.RobotState;
import com.team1389.trajectory.Rotation2d;
import com.team1389.trajectory.Translation2d;

import edu.wpi.first.wpilibj.Timer;
import simulation.motor.DriveTrain;

public class SimulationRobot {
	static final int ROBOT_WIDTH = 68;
	static final int ROBOT_HEIGHT = 70;
	static final int BUMPER_OFFSET = 6;

	Image robot;
	RobotState state = new RobotState();
	DriveTrain drive;
	boolean disabled;
	AngleIn<Position> gyro = new AngleIn<Position>(Position.class,
			() -> state.getLatestFieldToVehicle().getValue().getRotation().getDegrees());

	static final double startX = 148 * DriveSimulator.scale;
	static final double startY = 128 * DriveSimulator.scale;
	static final double startTheta = 60;
	static final RigidTransform2d startPos = new RigidTransform2d(new Translation2d(startX, startY),
			Rotation2d.fromDegrees(startTheta));
	boolean useBumpers = true;
	boolean bumperColor = false;
	int robotWidth = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);
	int robotHeight = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);
	int gearsDelivered = 0;
	private boolean carryingGear = false;

	SimulationField field;

	public static final float collisionReboundDistancePerTick = 0.01f;

	public SimulationRobot(SimulationField field, DriveTrain train) {
		state.reset(Timer.getFPGATimestamp(), startPos);
		this.drive = train;
		try {
			robot = new Image(useBumpers ? bumperColor ? "octi-red bumpers.png" : "octi-blue bumpers.png" : "octi.png")
					.getScaledCopy(robotWidth, robotHeight);
		} catch (SlickException e) {
			e.printStackTrace();
		}
		this.field = field;

	}

	private Vector2f extraTranslate = null;
	private double velocity;

	public void update(double dt) {
		Delta velocity = disabled ? new Delta(0, 0, 0) : drive.getRobotDelta(dt);
		this.velocity = Math.sqrt(Math.pow(velocity.dx, 2) + Math.pow(velocity.dy, 2)) * 1000 / dt;
		state.addObservations(Timer.getFPGATimestamp(),
				state.getLatestFieldToVehicle().getValue().transformBy(RigidTransform2d.fromVelocity(velocity)),
				velocity);

		for (Shape p : field.getBoundries()) {
			for (int i = 0; i < p.getPointCount(); i++) {
				float[] point1 = p.getPoint(i);
				float[] point2 = p.getPoint((i + 1) % (p.getPointCount()));
				Line l = new Line(point1[0], point1[1], point2[0], point2[1]);
				while (checkCollision(l)) {
					Vector2f translateDirection = new Vector2f((float) getHeadingDegrees());
					Vector2f unitVector = translateDirection.normalise();
					Vector2f antiUnitVector = unitVector.copy().negate();
					double secondDistance = l.distance(new Vector2f(getX(), getY()).add(unitVector));
					double thirdDistance = l.distance(new Vector2f(getX(), getY()).sub(unitVector));
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

		for (Shape gearPickup : field.getGearPickups()) {
			System.out.println(gearPickup.subtract(getBoundingBox()));
			if (gearPickup.contains(getBoundingBox()) && !carryingGear) {
				System.out.println("picked up gear");
				carryingGear = true;
			}
		}

		for (Shape gearDropoff : field.getGearDropoffs()) {
			if (gearDropoff.contains(getBoundingBox()) && carryingGear) {
				carryingGear = false;
				System.out.println("dropped off gear");
				gearsDelivered++;
			}
		}

	}

	public void setDriveTrain(DriveTrain train) {
		this.drive = train;
		drive.reset();
	}

	public double getVelocity() {
		return velocity;
	}

	public boolean checkCollision(Line l) {
		return getBoundingBox().intersects(l);
	}

	public AngleIn<Position> getHeadingIn() {

		return new AngleIn<Position>(Position.class, this::getHeadingDegrees);
	}

	public int getGearsDelivered() {
		return gearsDelivered;
	}

	public boolean hasGear() {
		return carryingGear;
	}

	private float getX() {
		Translation2d trans = getPose().getTranslation();
		return 2 * (float) trans.getX() + (extraTranslate != null ? extraTranslate.x : 0);
	}

	private float getY() {
		Translation2d trans = getPose().getTranslation();
		return 2 * (float) trans.getY() + (extraTranslate != null ? extraTranslate.y : 0);
	}

	private RigidTransform2d getPose() {
		return state.getLatestFieldToVehicle().getValue();
	}

	protected ArrayList<Point> points = new ArrayList<Point>();

	private static String gearPath = "Gear.png";
	private static final int gearSize = (int) (30 * DriveSimulator.scale);

	public void render(GameContainer container, Graphics g) throws SlickException {

		// Drawing robot
		robot.setRotation((float) getHeadingDegrees() + 90);
		robot.setCenterOfRotation(robotWidth / 2, robotHeight / 2);
		robot.drawCentered(getX(), getY());

		// Drawing gear
		if (carryingGear) {
			Image Gear = new Image(gearPath).getScaledCopy(gearSize, gearSize);
			Gear.setCenterOfRotation(gearSize / 2, gearSize / 2);
			Gear.setRotation((float) getHeadingDegrees());
			Gear.draw(getX() - gearSize / 2, getY() - gearSize / 2);
		}

	}

	public double getHeadingDegrees() {
		return getPose().getRotation().getDegrees();
	}

	public double getHeadingRads() {
		return getPose().getRotation().getRadians();
	}

	public void disable() {
		System.out.println("robot disabled!");
		disabled = true;
	}

	public void enable() {
		disabled = false;
		drive.reset();
	}

	public void startMatch() {
		state.reset(Timer.getFPGATimestamp(), startPos);
		extraTranslate = null;
		carryingGear = false;
		gearsDelivered = 0;
		enable();
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
