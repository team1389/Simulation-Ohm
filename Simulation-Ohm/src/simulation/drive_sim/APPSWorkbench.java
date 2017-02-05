package simulation.drive_sim;

import java.util.ArrayList;
import java.util.List;

import com.team1389.system.drive.PathFollowingSystem;
import com.team1389.trajectory.Kinematics;
import com.team1389.trajectory.Path;
import com.team1389.trajectory.Path.Waypoint;
import com.team1389.trajectory.RobotStateEstimator;
import com.team1389.trajectory.Translation2d;

import simulation.drive_sim.robot.OctoRobot;
import simulation.drive_sim.robot.RenderableRobot;

public class APPSWorkbench extends SimWorkbench {
	PathFollowingSystem sys;

	public APPSWorkbench(RenderableRobot robot) {
		super(robot);
	}

	@Override
	protected void initialize() {
		OctoRobot robot = (OctoRobot) this.robot;
		RobotStateEstimator state = new RobotStateEstimator(robot.getState(), robot.tank.leftIn, robot.tank.rightIn,
				robot.tank.leftVel, robot.tank.rightVel, robot.getGyro(), new Kinematics(10, 23, .6));
		sys = new PathFollowingSystem(robot.tank.getSpeedDrive(), state, 120, 80);
		List<Waypoint> return_path = new ArrayList<>();
        return_path.add(new Waypoint(new Translation2d(200, 0), 120.0));
        return_path.add(new Waypoint(new Translation2d(200, 58), 60.0));
        return_path.add(new Waypoint(new Translation2d(160, 58), 60.0));
        return_path.add(new Waypoint(new Translation2d(0, 58), 60.0));


		dash.watch(state);
		sys.followPath(new Path(return_path), false);

	}

	@Override
	protected void update() {
		sys.update();
	}

}
