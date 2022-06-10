package pl.edu.icm.trurl.util;

import com.google.common.base.Charsets;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class DebugFile implements Closeable {
    final PrintWriter printWriter;

    public DebugFile(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public void println(String x) {
        printWriter.println(x);
    }

    public void printf(String x, Object... args) {
        printWriter.printf(x, args);
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
    }

    public static DebugFile create(String path) throws FileNotFoundException {
        return new DebugFile(
                new PrintWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(path), Charsets.UTF_8)));
    }

    public void flush() {
        printWriter.flush();
    }
}
