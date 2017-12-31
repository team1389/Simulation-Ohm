import com.team1389.util.Loopable;
import com.team1389.watch.Watcher;

import simulation.Simulator;

public class TestShuffle extends Watcher {
	public static void main(String[] args) throws InterruptedException {
		Loopable l = new Loopable() {

			public void update() {
				Watcher.update();
			}

			@Override
			public void init() {
				IMU g = new IMU();
				new Watcher(g).outputToDashboard();
			}
		};
		Simulator.simulate(l);

	}
}
