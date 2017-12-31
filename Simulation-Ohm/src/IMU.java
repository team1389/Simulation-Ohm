import com.team1389.util.list.AddList;
import com.team1389.watch.CompositeWatchable;
import com.team1389.watch.Watchable;

public class IMU implements CompositeWatchable {

	@Override
	public String getName() {
		return "IMU";
	}

	@Override
	public AddList<Watchable> getSubWatchables(AddList<Watchable> stem) {
		return stem.put(new Gyro(), new Accel());
	}

}
