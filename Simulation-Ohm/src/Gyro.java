import com.team1389.util.list.AddList;
import com.team1389.watch.CompositeWatchable;
import com.team1389.watch.Watchable;
import com.team1389.watch.info.NumberInfo;

public class Gyro implements CompositeWatchable {

	@Override
	public String getName() {
		return "Gyro";
	}

	@Override
	public AddList<Watchable> getSubWatchables(AddList<Watchable> stem) {
		return stem.put(new NumberInfo("Angle", () -> 59.0), new NumberInfo("Rate", () -> 11702.0));
	}

}
