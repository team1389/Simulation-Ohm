package simulation.drive_sim;

public enum Alliance {
	RED(true), BLUE(false);
	private boolean isRed;

	private Alliance(boolean val) {
		this.isRed = val;
	}

	public boolean isRed() {
		return isRed;
	}

	public boolean isBlue() {
		return !isRed;
	}
}
