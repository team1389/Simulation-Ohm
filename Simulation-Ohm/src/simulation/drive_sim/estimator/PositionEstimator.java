package simulation.drive_sim.estimator;

import com.team1389.hardware.inputs.software.AngleIn;
import com.team1389.hardware.inputs.software.RangeIn;
import com.team1389.hardware.value_types.Position;
import com.team1389.hardware.value_types.Speed;
import com.team1389.hardware.value_types.Value;
import com.team1389.trajectory.RigidTransform2d;
import com.team1389.trajectory.RobotState;
import com.team1389.trajectory.RobotStateEstimator;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import simulation.drive_sim.Alliance;

public class PositionEstimator extends RobotStateEstimator{

	Alliance color;
	RangeIn<Value> gearsDroppedOff;
	double lastGearValue = 0;
	public PositionEstimator(Alliance color, RobotState temp, RangeIn<Value> gearsDroppedOff, RangeIn<Position> left, RangeIn<Position> right, RangeIn<Speed> leftS,
			RangeIn<Speed> rightS, AngleIn<Position> gyro, double trackWidth, double trackLength, double scrub) {
		super(temp, left, right, leftS, rightS, gyro, trackWidth, trackLength, scrub);
		this.color = color;
		this.gearsDroppedOff = gearsDroppedOff;
	}
	
	@Override
	public RigidTransform2d get(){
		
		//Check change in gears. Would be different for actual robot
		double temp = gearsDroppedOff.get();
		if(temp != lastGearValue){
			lastGearValue = temp;
			pickUpGearReset();
		}
		
		
		return super.get();
	}
	
	public void publish(){
		NetworkTable.getTable("StateEstimator").putNumber("X", super.get().getTranslation().getX());
		NetworkTable.getTable("StateEstimator").putNumber("Y", super.get().getTranslation().getY());
		NetworkTable.getTable("StateEstimator").putNumber("Degrees", super.get().getRotation().getDegrees());
		//Not really sure if this will work

	}
	
	public void pickUpGearReset(){
		double thetaDegrees = gyro.get();
		if(color.equals(Alliance.RED)){
			if(thetaDegrees < -40){
				this.resetToPosition(140, 250, thetaDegrees);
			}
			else if(thetaDegrees > 40){
				this.resetToPosition(156, 120, thetaDegrees);
			}
			else{
				this.resetToPosition(118, 185, thetaDegrees);
			}
		}
		
		//TODO: Blue alliance
		
	}

}
