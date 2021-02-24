package exhibition.util.security.natives.wrappers;

import exhibition.util.security.natives.accessors.AuthUserWrapper;

public class AuthUser implements AuthUserWrapper {

    // The pointer of the C++ AuthUser class in memory
    private final long nativeObjectPointer;

    public AuthUser(long pointer) {
        this.nativeObjectPointer = pointer;
    }

}
