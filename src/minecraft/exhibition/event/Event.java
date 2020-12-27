package exhibition.event;

public abstract class Event {

	private boolean cancelled;

	public void fire() {
		cancelled = false;
		EventSystem.fire(this);
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public <T extends Event> T cast() {
		return (T) this;
	}

}
