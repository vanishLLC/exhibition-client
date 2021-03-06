package exhibition.util;

public class Timer {
	private long prevMS;

	public Timer() {
		this.prevMS = 0L;
	}

	public boolean delay(long milliSec) {
		return getTime() - this.prevMS >= milliSec;
	}

	public boolean roundDelay(long milliSec) {
		return (milliSec - (getTime() - this.prevMS)) <= 5;
	}

	public void reset() {
		this.prevMS = getTime();
	}

	public long getTime() {
		return System.nanoTime() / 1000000L;
	}

	public long getDifference() {
		return getTime() - this.prevMS;
	}

	public void setDifference(long difference) {
		this.prevMS = (getTime() + difference);
	}
}
