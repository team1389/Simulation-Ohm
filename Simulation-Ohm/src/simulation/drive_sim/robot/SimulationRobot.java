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

//TODO get actual startPos for Red
public class SimulationRobot
{
	// all calcs done based off of proportion of dimension to height of field
	// original robot width is 24, original robot height is 26
	// original bumper offset was 3
	public static final double ROBOT_WIDTH_SCALE = .07;
	public static final double ROBOT_HEIGHT_SCALE = .08;
	static final double BUMPER_OFFSET_SCALE = .01;
	static final RigidTransform2d startPosBlue = new RigidTransform2d(
			new Translation2d(148 * DriveSimulator.scale, 128 * DriveSimulator.scale), Rotation2d.fromDegrees(60));

	static RigidTransform2d startPosRedB = new RigidTransform2d(new Translation2d(56, 270), Rotation2d.fromDegrees(0));
	// legitimate startPos: static RigidTransform2d startPosRed = new
	// RigidTransform2d(new Translation2d(48, 270), Rotation2d.fromDegrees(0));
	//startPos below is COMPLETELY ARBITRARY
	static RigidTransform2d startPosRed = new RigidTransform2d(new Translation2d(200, 270), Rotation2d.fromDegrees(0));
	static RigidTransform2d startPosRedC = new RigidTransform2d(new Translation2d(48, 71), Rotation2d.fromDegrees(0));
	static RigidTransform2d startPosRedD = new RigidTransform2d(new Translation2d(149, 244),
			Rotation2d.fromDegrees(-60));

	public static final float collisionReboundDistancePerTick = 0.005f * DriveSimulator.scale;

	static final boolean useBumpers = true;

	int robotWidth;
	int robotHeight;

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

	public SimulationRobot(SimulationField field, DriveTrain train)
	{
		this(field, train, Alliance.RED);
	}

	// (int) ((ROBOT_WIDTH + (useBumpers ? 2 * BUMPER_OFFSET : 0)) *
	// DriveSimulator.scale);
	public SimulationRobot(SimulationField field, DriveTrain train, Alliance alliance)
	{
		// startPosRed=new RigidTransform2d();
		robotWidth = (int) Math.round((ROBOT_WIDTH_SCALE * field.getFieldHeight()
				+ (useBumpers ? 2 * (BUMPER_OFFSET_SCALE * field.getFieldHeight()) : 0)));
		robotHeight = (int) Math.round((ROBOT_HEIGHT_SCALE * field.getFieldHeight()
				+ (useBumpers ? 2 * (BUMPER_OFFSET_SCALE * field.getFieldHeight()) : 0)));
		state = new RobotState();
		this.drive = train;
		this.field = field;
		this.alliance = alliance;
		try
		{
			robot = generateRobotImage().getScaledCopy(robotWidth, robotHeight);
		} catch (SlickException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * cuts all robot's momentum
	 */
	public void reset()
	{
		drive.reset();
		theta = 0;
	}

	private Image generateRobotImage() throws SlickException
	{
		return new Image(useBumpers
				? alliance == Alliance.BLUE ? Resources.blueAllianceRobotImage : Resources.redAllianceRobotImage
				: Resources.robotImage);
	}

	public void startMatch()
	{
		getState().reset(Timer.getFPGATimestamp(), new RigidTransform2d());
		theta = 0;
		carryingGear = false;
		gearsDelivered = 0;
		velocity = 0;
		drive.reset();
		enable();
	}

	public void update(double dt)
	{
		updateRobotPosition(dt);
		// updateCollision();
	}

	double theta;
	private Delta directedVel;

	protected void updateRobotPosition(double dt)
	{
		Delta velocity = disabled ? new Delta(0, 0, 0) : drive.getRobotDelta(dt);
		theta += Math.toDegrees(velocity.dtheta);
		this.velocity = Math.sqrt(Math.pow(velocity.dx, 2) + Math.pow(velocity.dy, 2)) * 1000 / dt;
		getState().addObservations(Timer.getFPGATimestamp(),
				getPose().transformBy(RigidTransform2d.fromVelocity(velocity)), velocity);

		Delta oldDirectedVel = directedVel == null ? velocity : directedVel;
		this.directedVel = new Delta(velocity.dx * 1000 / dt, velocity.dy * 1000 / dt, velocity.dtheta);
		double xAccel = (directedVel.dx - oldDirectedVel.dx) * 1000 / dt;
		double yAccel = (directedVel.dy - oldDirectedVel.dy) * 1000 / dt;
		this.acceleration = Math.sqrt(Math.pow(xAccel, 2) + Math.pow(yAccel, 2));
	}

	public void setDriveTrain(DriveTrain train)
	{
		System.out.println("switching drive trains");
		if (drive != null)
			drive.reset();
		this.drive = train;
		drive.reset();
	}

	// should be able to alter most recent pose to be 0,0 thus functionally
	// resetting, causing robot to be rendered at startpos
	protected RigidTransform2d getPose()
	{
		return getState().getLatestFieldToVehicle().getValue();
	}

	public Alliance getAlliance()
	{
		return alliance;
	}

	public AngleIn<Position> getGyro()
	{
		return new AngleIn<Position>(Position.class, () -> theta);
	}

	public double getVelocity()
	{
		return velocity;
	}

	public int getGearsDelivered()
	{
		return gearsDelivered;
	}

	public boolean hasGear()
	{
		return carryingGear;
	}

	public float getX()
	{
		return (float) getPose().getTranslation().getX();
	}

	public float getY()
	{
		return (float) getPose().getTranslation().getY();
	}

	public double getRelativeHeadingDegrees()
	{
		return getPose().getRotation().getDegrees();
	}

	protected double getRelativeHeadingRads()
	{
		return getPose().getRotation().getRadians();
	}

	public void disable()
	{
		System.out.println("robot disabled!");
		disabled = true;
	}

	public void enable()
	{
		disabled = false;
		drive.reset();
	}

	public boolean isEnabled()
	{
		return !disabled;
	}

	public RobotState getState()
	{
		return state;
	}

	/**
	 * The magnitude of the acceleration
	 * 
	 * @return
	 */
	public double getAcceleration()
	{
		return acceleration;
	}
}
