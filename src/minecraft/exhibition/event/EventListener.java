package exhibition.event;

public interface EventListener<E extends Event> {

    class Priority {
        final int ordinal;

        public static Priority FIRST = c(0), LOWEST = c(1), LOW = c(2), NORMAL = c(3),
                MEDIUM = c(4), HIGH = c(5), HIGHEST = c(6), LAST = c(7);

        public Priority(int ordinal) {
            this.ordinal = ordinal;
        }

        public int ordinal() {
            return this.ordinal;
        }

        // Static constructor
        static Priority c(int ordinal) {
            return new Priority(ordinal);
        }

    }

    default Priority getPriority() {
        return Priority.NORMAL;
    }

    default int getPriorityOrdinal() {
        return getPriority().ordinal();
    }

    String getName();

    void onEvent(E event);
}
