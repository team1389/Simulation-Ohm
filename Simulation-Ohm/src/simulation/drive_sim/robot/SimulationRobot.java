package simulation.drive_sim.robot;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;
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
import simulation.drive_sim.Alliance;
import simulation.drive_sim.DriveSimulator;
import simulation.drive_sim.DriveTrain;
import simulation.drive_sim.Resources;
import simulation.drive_sim.field.AlliedBoundary;
import simulation.drive_sim.field.SimulationField;

public class SimulationRobot {
	static final int ROBOT_WIDTH = 48;
	static final int ROBOT_HEIGHT = 52;
	static final int BUMPER_OFFSET = 6;
	static final RigidTransform2d startPosBlue = new RigidTransform2d(
			new Translation2d(148 * DriveSimulator.scale, 128 * DriveSimulator.scale), Rotation2d.fromDegrees(60));
	static final RigidTransform2d startPosRed = new RigidTransform2d(
			new Translation2d(567 * DriveSimulator.scale, 249 * DriveSimulator.scale), Rotation2d.fromDegrees(-120));

	private static final int gearSize = (int) (30 * DriveSimulator.scale);

	public static final float collisionReboundDistancePerTick = 0.01f;

	static final boolean useBumpers = true;

	int robotWidth = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);
	int robotHeight = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);

	Image robot;
	DriveTrain drive;
	RobotState state;
	private int gearsDelivered;
	private boolean carryingGear;
	private boolean disabled;
	private double velocity;
	private Alliance alliance;
	private Vector2f extraTranslate;

	SimulationField field;

	public SimulationRobot(SimulationField field, DriveTrain train) {
		this(field, train, Alliance.RED);
	}

	public SimulationRobot(SimulationField field, DriveTrain train, Alliance alliance) {
		state = new RobotState();
		this.drive = train;
		this.field = field;
		this.alliance = alliance;
		try {
			robot = generateRobotImage().getScaledCopy(robotWidth, robotHeight);
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}

	private Image generateRobotImage() throws SlickException {
		return new Image(useBumpers
				? alliance == Alliance.BLUE ? Resources.blueAllianceRobotImage : Resources.redAllianceRobotImage
				: Resources.robotImage);
	}

	public void startMatch() {
		state.reset(Timer.getFPGATimestamp(), alliance == Alliance.BLUE ? startPosBlue : startPosRed);
		extraTranslate = null;
		carryingGear = false;
		gearsDelivered = 0;
		velocity = 0;
		drive.reset();
		enable();
	}

	public void update(double dt) {
		updateRobotPosition(dt);
		updateCollision();
	}

	private void updateRobotPosition(double dt) {
		Delta velocity = disabled ? new Delta(0, 0, 0) : drive.getRobotDelta(dt);
		this.velocity = Math.sqrt(Math.pow(velocity.dx, 2) + Math.pow(velocity.dy, 2)) * 1000 / dt;
		state.addObservations(Timer.getFPGATimestamp(),
				state.getLatestFieldToVehicle().getValue().transformBy(RigidTransform2d.fromVelocity(velocity)),
				velocity);
	}

	private void updateCollision() {
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
		for (AlliedBoundary gearPickup : field.getGearPickups()) {
			if (gearPickup.isRobotEligible(this) && !carryingGear) {
				System.out.println("picked up gear");
				carryingGear = true;
			}
		}

		for (AlliedBoundary gearDropoff : field.getGearDropoffs()) {
			if (gearDropoff.isRobotEligible(this) && carryingGear) {
				carryingGear = false;
				System.out.println("dropped off gear");
				gearsDelivered++;
			}
		}

	}

	public void render(GameContainer container, Graphics g) throws SlickException {

		// Drawing robot
		robot.setRotation((float) getHeadingDegrees() + 90);
		robot.setCenterOfRotation(robotWidth / 2, robotHeight / 2);
		robot.drawCentered(getX(), getY());
		// Drawing gear
		if (carryingGear) {
			Image Gear = new Image(Resources.gearImage).getScaledCopy(gearSize, gearSize);
			Gear.setCenterOfRotation(gearSize / 2, gearSize / 2);
			Gear.setRotation((float) getHeadingDegrees());
			Gear.draw(getX() - gearSize / 2, getY() - gearSize / 2);
		}

	}

	public void setDriveTrain(DriveTrain train) {
		this.drive = train;
		drive.reset();
	}

	private RigidTransform2d getPose() {
		return state.getLatestFieldToVehicle().getValue();
	}

	public Alliance getAlliance() {
		return alliance;
	}

	public Polygon getBoundingBox() {
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

	public boolean checkCollision(Line l) {
		return getBoundingBox().intersects(l);
	}

	public AngleIn<Position> getGyro() {
		return new AngleIn<Position>(Position.class, () -> (double) robot.getRotation());
	}

	public double getVelocity() {
		return velocity;
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

	public boolean isEnabled() {
		return !disabled;
	}

}
