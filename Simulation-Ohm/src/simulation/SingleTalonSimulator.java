package simulation;

import com.team1389.util.Loopable;

public class SingleTalonSimulator implements Loopable{

	public static void main(String args) throws InterruptedException{
		Simulator.initWPILib();
		SingleTalonSimulator talonSim = new SingleTalonSimulator();
		Simulator.simulate(talonSim);
	}

	@Override
	public void update() {
		
	}
	
	@Override
	public void init(){
		
	}
}
