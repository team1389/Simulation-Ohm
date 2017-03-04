package simulation.drive_sim.network;

import com.team1389.trajectory.RigidTransform2d;
import com.team1389.trajectory.RobotStateEstimator;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class NetworkPosition {

	RobotStateEstimator position;
	NetworkTable table;
	public NetworkPosition(RobotStateEstimator position){
		this.position = position;
		table = NetworkTable.getTable("estimator");
		table.putBoolean("placed", false);
	}
	
	public void updateNetwork(int gearState){
		RigidTransform2d transform = position.get();
		table.putNumber(EstimatorTableKeys.X_POSITION.key, transform.getTranslation().getX());
		table.putNumber(EstimatorTableKeys.Y_POSITION.key, transform.getTranslation().getY());
		table.putNumber(EstimatorTableKeys.ANGLE_DEGREES.key, transform.getRotation().getDegrees());
		table.putNumber(EstimatorTableKeys.GEAR.key, gearState); 
		if(gearState == 3){
			table.putBoolean("placed", true); //For now just use this
		}
	}
	
	
	private enum EstimatorTableKeys {
		X_POSITION("x"), Y_POSITION("y"), ANGLE_DEGREES("angle"), GEAR("gear"), GEARPLACED("placed");;
		protected final String key;
		private EstimatorTableKeys(String key) {
			this.key = key;
		}
	}

}
