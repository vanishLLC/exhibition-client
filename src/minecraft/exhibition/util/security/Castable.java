package exhibition.util.security;

/*
 * The intended use for this class hide what the Object class is from bytecode analysis.
 * Without Example: this.foo = (Foo)object; gives away that the passed in Object instance should be Foo
 *
 * With Example: this.foo = castable.cast(); should hide what Castable is until it's directly being used.
 *
 * As an Object is handled around, using this class should hopefully complicate anyone attempting to reverse the client
 */
public abstract class Castable {

    public <T extends Castable> T cast() {
        return (T) this;
    }

}
