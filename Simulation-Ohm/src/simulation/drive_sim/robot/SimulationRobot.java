package simulation.drive_sim.robot;

import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

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
import simulation.drive_sim.field.SimulationField;

public class SimulationRobot {
	static final int ROBOT_WIDTH = 24;
	static final int ROBOT_HEIGHT = 26;
	static final int BUMPER_OFFSET = 3;
	static final RigidTransform2d startPosBlue = new RigidTransform2d(
			new Translation2d(148 * DriveSimulator.scale, 128 * DriveSimulator.scale), Rotation2d.fromDegrees(60));

	static RigidTransform2d startPosRed = new RigidTransform2d(
			new Translation2d(567 * DriveSimulator.scale, 249 * DriveSimulator.scale), Rotation2d.fromDegrees(0));

	public static final float collisionReboundDistancePerTick = 0.005f * DriveSimulator.scale;

	static final boolean useBumpers = true;

	int robotWidth = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);
	int robotHeight = (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) * DriveSimulator.scale);

	Image robot;
	DriveTrain drive;
	private RobotState state;
	protected int gearsDelivered;
	protected boolean carryingGear;
	private boolean disabled;
	private double velocity;
	protected Alliance alliance;

	SimulationField field;

	public SimulationRobot(SimulationField field, DriveTrain train) {
		this(field, train, Alliance.RED);
	}

	public SimulationRobot(SimulationField field, DriveTrain train, Alliance alliance) {
		// startPosRed=new RigidTransform2d();
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
		getState().reset(Timer.getFPGATimestamp(), new RigidTransform2d());
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

	private void updateRobotPosition(double dt) {
		Delta velocity = disabled ? new Delta(0, 0, 0) : drive.getRobotDelta(dt);
		theta += Math.toDegrees(velocity.dtheta);
		this.velocity = Math.sqrt(Math.pow(velocity.dx, 2) + Math.pow(velocity.dy, 2)) * 1000 / dt;
		getState().addObservations(Timer.getFPGATimestamp(),
				getPose().transformBy(RigidTransform2d.fromVelocity(velocity)), velocity);
	}

	public void setDriveTrain(DriveTrain train) {
		System.out.println("switching drive trains");
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

	protected float getX() {
		return (float) getPose().getTranslation().getX();
	}

	protected float getY() {
		return (float) getPose().getTranslation().getY();
	}

	protected double getRelativeHeadingDegrees() {
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

}
