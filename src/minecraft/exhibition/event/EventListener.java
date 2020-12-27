package exhibition.event;

public interface EventListener<E extends Event> {

	enum Priority {
		FIRST, LOWEST, LOW, NORMAL, MEDIUM, HIGH, HIGHEST, LAST
	}

	default Priority getPriority() {
		return Priority.NORMAL;
	}

	void onEvent(E event);
}
