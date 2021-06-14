package exhibition.util.security;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DummyPrintStream extends PrintStream {

    public DummyPrintStream(OutputStream out) {
        super(out);
    }

    @Override
    public void write(int b) {
    }

    @Override
    public void write(byte[] b) throws IOException {
    }

    @Override
    public void write(byte[] buf, int off, int len) {
    }

}