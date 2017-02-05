package simulation.drive_sim;

import java.util.Arrays;

import com.team1389.command_framework.command_base.Command;
import com.team1389.hardware.inputs.software.AngleIn;
import com.team1389.hardware.inputs.software.PositionEncoderIn;
import com.team1389.hardware.value_types.Percent;
import com.team1389.hardware.value_types.Position;
import com.team1389.system.drive.DriveOut;
import com.team1389.system.drive.DriveSignal;
import com.team1389.system.drive.DriveSystem;
import com.team1389.trajectory.AdaptivePurePursuitController;
import com.team1389.trajectory.Path;
import com.team1389.util.list.AddList;
import com.team1389.watch.Watchable;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.followers.DistanceFollower;
import jaci.pathfinder.modifiers.TankModifier;

/**
 * drive system that follows paths autonomously using the Adaptive Pure Pursuit Controller
 * 
 * @see AdaptivePurePursuitController
 * @author amind
 *
 */
public class PathFollowingSystem extends DriveSystem {
	/**
	 * the default time between updates
	 */
	public static double UPDATE_DT = 1 / 50;
	private DistanceFollower leftDistance, rightDistance;
	private PositionEncoderIn leftPos, rightPos;
	private DriveOut<Percent> wheelVoltageOut;
	private Constants constants;
	private AngleIn<Position> heading;

	public PathFollowingSystem(DriveOut<Percent> voltage, PositionEncoderIn leftPos, PositionEncoderIn rightPos,
			AngleIn<Position> heading) {
		this.leftPos = leftPos;
		this.rightPos = rightPos;
		this.wheelVoltageOut = voltage;
		this.heading = heading;
	}

	/**
	 * The robot follows a set path, which is defined by Waypoint objects.
	 * 
	 * @param path the path to follow
	 * @param reversed whether to follow the path in reverse
	 * @see Path
	 */
	public synchronized void followPath(Trajectory path) {
		TankModifier modifier = new TankModifier(path).modify(0.6);
		DistanceFollower left = new DistanceFollower(modifier.getLeftTrajectory());
		DistanceFollower right = new DistanceFollower(modifier.getRightTrajectory());
		constants.configureFollowers(left, right);
	}

	/**
	 * @return Returns if the robot mode is Path Following Control and the set path is complete.
	 */
	public synchronized boolean isFinishedPath() {
		return leftDistance.isFinished() && rightDistance.isFinished();
	}

	/**
	 * updates the pathFollower, setting the speeds of the wheels based on the robot's current pose
	 */
	public void update() {
		double l = leftDistance.calculate(leftPos.get());
		double r = rightDistance.calculate(rightPos.get());
		double gyro_heading = 180 + heading.get();
		double desired_heading = Pathfinder.r2d(leftDistance.getHeading());
		double angleDifference = Pathfinder.boundHalfDegrees(desired_heading - gyro_heading);
		double turn = constants.gyroP * angleDifference;
		wheelVoltageOut.set(l - turn, r + turn);
	}

	@Override
	public AddList<Watchable> getSubWatchables(AddList<Watchable> stem) {
		return stem.put(wheelVoltageOut, heading.getWatchable("angle"), leftPos.getWatchable("leftPos"),
				rightPos.getWatchable("rightPos"));
	}

	@Override
	public String getName() {
		return "Path follower";
	}

	@Override
	public void init() {

	}

	public class Constants {
		public final double maxJerk, maxAccel, maxVel, pathP, pathD, pathA, gyroP, trackWidth;

		public Constants(double maxJerk, double maxAccel, double maxVel, double pathP, double pathD, double pathA,
				double gyroP, double trackWidth) {
			this.maxAccel = maxAccel;
			this.maxJerk = maxJerk;
			this.maxVel = maxVel;
			this.pathP = pathP;
			this.pathD = pathD;
			this.pathA = pathA;
			this.gyroP = gyroP;
			this.trackWidth = trackWidth;
		}

		private void configureFollower(DistanceFollower follower) {
			follower.configurePIDVA(pathP, 0.0, pathD, 1 / maxVel, pathA);
		}

		public void configureFollowers(DistanceFollower... followers) {
			Arrays.stream(followers).forEach(this::configureFollower);
		}
	}

	public Trajectory generateTrajectory(Waypoint[] points) {
		Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC,
				Trajectory.Config.SAMPLES_HIGH, UPDATE_DT, constants.maxVel, constants.maxAccel, constants.maxJerk);
		return Pathfinder.generate(points, config);
	}

	public class PathFollowCommand extends Command {
		private Trajectory path;

		public PathFollowCommand(Trajectory path) {
			this.path = path;
		}

		public PathFollowCommand(Waypoint[] points) {
			this(generateTrajectory(points));
		}

		@Override
		protected void initialize() {
			followPath(path);
		}

		@Override
		protected boolean execute() {
			update();
			return isFinishedPath();
		}

		@Override
		protected void done() {
			wheelVoltageOut.set(DriveSignal.NEUTRAL);
		}

	}
}
