package exhibition.module.data;

import com.google.gson.annotations.Expose;
import exhibition.management.keybinding.KeyMask;
import org.lwjgl.input.Keyboard;

public class ModuleData {
	public final Type type;
	public final String name;
	public final String description;
	@Expose
	public int key;
	@Expose
	public KeyMask mask;

	public ModuleData(Type type, String name, String description, int key, KeyMask mask) {
		this.type = type;
		this.name = name;
		this.description = description;
		this.key = key;
		this.mask = mask;
	}

	public ModuleData(Type type, String name, String description) {
		this(type, name, description, Keyboard.CHAR_NONE, KeyMask.None);
	}

	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public KeyMask getMask() {
		return mask;
	}

	public void setMask(KeyMask mask) {
		this.mask = mask;
	}

	public void update(ModuleData data) {
		key = data.key;
		mask = data.mask;
	}

	public static class Type {

		public static Type Combat = new Type("Combat"), Player = new Type("Player"), Movement = new Type("Movement"), Visuals = new Type("Visuals"), Other = new Type("Other");

		public Type(String name) {
			this.name = name;
		}

		String name;

		public String name() {
			return this.name;
		}

		public static Type[] values() {
			return new Type[] {Combat, Player, Movement, Visuals, Other};
		}

	}
}
