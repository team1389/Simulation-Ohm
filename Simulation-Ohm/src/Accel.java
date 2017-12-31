import com.team1389.util.list.AddList;
import com.team1389.watch.CompositeWatchable;
import com.team1389.watch.Watchable;
import com.team1389.watch.info.NumberInfo;

public class Accel implements CompositeWatchable {

	@Override
	public String getName() {
		return "Accel";
	}

	@Override
	public AddList<Watchable> getSubWatchables(AddList<Watchable> stem) {
		return stem.put(new NumberInfo("XAcc", () -> 75.0), new NumberInfo("YAcc", () -> 140.0),
				new NumberInfo("ZAcc", () -> 0.0));
	}

}
