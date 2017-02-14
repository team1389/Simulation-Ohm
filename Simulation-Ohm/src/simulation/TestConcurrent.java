package simulation;

import com.team1389.concurrent.OhmThreadService;
import com.team1389.util.Loopable;
import com.team1389.watch.Watcher;

public class TestConcurrent implements Loopable {
	public static void main(String[] args) throws InterruptedException {
		Simulator.simulate(new TestConcurrent());
	}

	@Override
	public void init() {
		OhmThreadService ohm = new OhmThreadService(3);
		ohm.init();
		Watcher dash = new Watcher();
		dash.outputToConsole();
		dash.watch(ohm);
		ohm.borrowThreadToRun(() -> System.out.println("hello"));
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		ohm.reset();
		System.err.println("RESET ATTEMPTED");

	}

	@Override
	public void update() {

	}
}
