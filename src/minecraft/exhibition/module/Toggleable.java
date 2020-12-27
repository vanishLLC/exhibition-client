package exhibition.module;

public interface Toggleable {
	void toggle();

	void onToggle();

	void onEnable();

	void onDisable();

	boolean isEnabled();

}
