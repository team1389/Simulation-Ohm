package simulation.drive_sim.robot;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import com.team1389.hardware.inputs.software.AngleIn;
import com.team1389.hardware.value_types.Position;
import com.team1389.trajectory.RigidTransform2d;
import com.team1389.trajectory.RigidTransform2d.Delta;
import com.team1389.trajectory.RobotState;

import edu.wpi.first.wpilibj.Timer;
import simulation.drive_sim.Alliance;
import simulation.drive_sim.DriveSimulator;
import simulation.drive_sim.DriveTrain;
import simulation.drive_sim.Resources;
import simulation.drive_sim.field.SimulationField;

public abstract class SimulationRobot {
	public static final int ROBOT_WIDTH = 24;
	public static final int ROBOT_HEIGHT = 26;
	static final int BUMPER_OFFSET = 3;

	static final boolean useBumpers = true;

	int robotWidth = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);
	int robotHeight = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);

	Image robot;
	DriveTrain drive;
	private RobotState state;
	protected int gearsDelivered;
	protected boolean carryingGear;
	protected boolean disabled;
	protected double velocity;
	protected double acceleration;
	protected Alliance alliance;

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

	public void reset() {
		drive.reset();
		theta = 0;
	}

	private Image generateRobotImage() throws SlickException {
		return new Image(useBumpers
				? alliance == Alliance.BLUE ? Resources.blueAllianceRobotImage : Resources.redAllianceRobotImage
				: Resources.robotImage);
	}

	public void startMatch() {
		getState().reset(Timer.getFPGATimestamp(), new RigidTransform2d());
		theta = 0;
		carryingGear = false;
		gearsDelivered = 0;
		velocity = 0;
		drive.reset();
		enable();
	}

	public void update(double dt) {
		updateRobotPosition(dt);
		// updateCollision();
	}

	double theta;

	protected void updateRobotPosition(double dt) {
		Delta velocity = disabled ? new Delta(0, 0, 0) : drive.getRobotDelta(dt);
		theta += Math.toDegrees(velocity.dtheta);
		this.velocity = Math.sqrt(Math.pow(velocity.dx, 2) + Math.pow(velocity.dy, 2)) * 1000 / dt;
		getState().addObservations(Timer.getFPGATimestamp(),
				getPose().transformBy(RigidTransform2d.fromVelocity(velocity)), velocity);
	}

	public void setDriveTrain(DriveTrain train) {
		if (drive != null)
			drive.reset();
		this.drive = train;
		drive.reset();
	}

	protected RigidTransform2d getPose() {
		return getState().getLatestFieldToVehicle().getValue();
	}

	public Alliance getAlliance() {
		return alliance;
	}

	public AngleIn<Position> getGyro() {
		return new AngleIn<Position>(Position.class, () -> theta);
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

	public float getX() {
		return (float) getPose().getTranslation().getX();
	}

	public float getY() {
		return (float) getPose().getTranslation().getY();
	}

	public double getRelativeHeadingDegrees() {
		return getPose().getRotation().getDegrees();
	}

	protected double getRelativeHeadingRads() {
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

	public RobotState getState() {
		return state;
	}

	/**
	 * The magnitude of the acceleration
	 * @return
	 */
	public double getAcceleration() {
		return acceleration;
	}
}
