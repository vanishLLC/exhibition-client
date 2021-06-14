package exhibition.util.misc;

public class NanoTimer {
	private long previousTime;

	public NanoTimer() {
		previousTime = -1L;
	}

	public boolean check(double milliseconds) {
		return getTime() >= (long)(1000000L * milliseconds);
	}
	
	public double getTime(){
		return (getCurrentNanoTime() - previousTime) / 1000000D;
	}

	public void reset() {
		previousTime = getCurrentNanoTime();
	}

	public double getCurrentTimeMS() {
		return getCurrentNanoTime() / 1000000D;
	}

	public long getCurrentNanoTime() {
		return System.nanoTime();
	}
}
