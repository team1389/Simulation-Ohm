package simulation.drive_sim.robot;

import java.util.function.Supplier;

import com.team1389.hardware.inputs.software.DigitalIn;
import com.team1389.hardware.outputs.software.DigitalOut;
import com.team1389.hardware.outputs.software.PercentOut;
import com.team1389.hardware.value_types.Percent;
import com.team1389.system.drive.FourDriveOut;
import com.team1389.trajectory.RobotState;

import simulation.drive_sim.Alliance;
import simulation.drive_sim.field.SimulationField;
import simulation.motor.MotorSystem;

public class OctoRobot extends RenderableRobot {
	private static final long SWITCH_TIME_DELAY_MILLIS = 250;
	boolean currentMode;
	private MecanumDriveTrain mec;
	public TankDriveTrain tank;

	public OctoRobot(SimulationField field) {
		this(field, Alliance.RED);
	}

	public OctoRobot(SimulationField field, Alliance alliance) {
		super(field, null, alliance);
		this.tank = new TankDriveTrain();
		this.mec = new MecanumDriveTrain();
		setDriveTrain(tank);
	}
	
	public TankDriveTrain getTank(){
		return tank;
	}

	@Override
	public void startMatch() {
		super.startMatch();
		tank.reset();
		mec.reset();
		setModeInstantaneous(true);
		setDriveTrain(tank);
	}

	public DigitalIn getModeSensor() {
		return new DigitalIn(this::isTankMode);
	}

	public DigitalOut getModeSetter() {
		return new DigitalOut(this::setMode);
	}

	public boolean isTankMode() {
		return currentMode;
	}

	public void setMode(boolean mode) {
		System.out.println("setting mode "+mode+" currently in "+isTankMode());
		setModeInstantaneous(mode);
	}
	public void resetPosition(RobotState state)
	{
		tank.reset();
		mec.reset();
		robot.drawCentered((float)getStartPos().getTranslation().getX(), (float)getStartPos().getTranslation().getY());
		state.reset(0, getStartPos());
	}
	


	private void setModeInstantaneous(boolean mode) {
		setDriveTrain(mode ? tank : mec);
		this.currentMode = mode;
	}

	private PercentOut genConditOut(Supplier<MotorSystem> tank, Supplier<MotorSystem> mec) {
		return new PercentOut(v -> {
			if (currentMode) {
				tank.get().setVoltage(v);
			} else {
				mec.get().setVoltage(v);
			}
		});
	}

	private PercentOut genConditOut(Supplier<MotorSystem> mec) {
		return new PercentOut(v -> {
			if (!currentMode) {
				mec.get().setVoltage(v);
			}
		});
	}
	

	public FourDriveOut<Percent> getWheels() {
		PercentOut frontLeft = genConditOut(() -> tank.left, () -> mec.topLeft);
		PercentOut frontRight = genConditOut(() -> tank.right, () -> mec.topRight);
		PercentOut backLeft = genConditOut(() -> mec.botLeft);
		PercentOut backRight = genConditOut(() -> mec.botRight);
		return new FourDriveOut<>(frontLeft, frontRight, backLeft, backRight);
	}

	public MecanumDriveTrain getMecanum() {
		return mec;
	}
}
