package simulation.test;

public interface Testable {
	public void test() throws AssertionError;

	public default boolean testPassed() {
		try {
			test();
			return true;
		} catch (AssertionError e) {
			System.err.println(e.getMessage());
			return false;
		}
	}
}
