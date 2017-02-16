package simulation.drive_sim.estimator;

import com.team1389.hardware.inputs.software.AngleIn;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Speed;
import com.team1389.trajectory.RobotState;
import com.team1389.trajectory.RobotStateEstimator;

public class PositionEstimator extends RobotStateEstimator{

	public PositionEstimator(RobotState state, RangeIn<Position> left, RangeIn<Position> right, RangeIn<Speed> leftS,
			RangeIn<Speed> rightS, AngleIn<Position> gyro, double trackWidth, double trackLength, double scrub) {
		super(state, left, right, leftS, rightS, gyro, trackWidth, trackLength, scrub);
		// TODO Auto-generated constructor stub
	}

}
