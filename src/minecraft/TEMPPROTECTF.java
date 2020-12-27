/*
 * Copyright (c) MineSense.pub 2018.
 * Developed by Arithmo
 */

public class TEMPPROTECTF {

    public static void main(String[] args) throws Exception {
        bruh(args);
    }

    public static void bruh(String[] args) throws Exception {
        Class.forName("Main").getDeclaredMethod("bruh", String[].class).invoke(null, new Object[] {args});
    }

}
