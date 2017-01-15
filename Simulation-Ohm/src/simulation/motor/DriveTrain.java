package simulation.motor;

import com.team1389.trajectory.RigidTransform2d.Delta;

public interface DriveTrain {
	public Delta getRobotDelta(double dt);
	public void reset();
}
