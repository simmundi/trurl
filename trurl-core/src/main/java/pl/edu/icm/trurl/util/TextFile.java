package pl.edu.icm.trurl.util;

import com.google.common.base.Charsets;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class TextFile implements Closeable {
    final PrintWriter printWriter;

    public TextFile(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }

    public void println(String x) {
        printWriter.println(x);
    }

    public void println() {
        printWriter.println();
    }

    public void printf(String x, Object... args) {
        printWriter.printf(x, args);
    }

    public void printlnf(String x, Object... args) {
        printWriter.printf(x, args);
        printWriter.println();
    }

    public void print(int i) {
        printWriter.print(i);
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
    }

    public static TextFile create(String path) throws FileNotFoundException {
        return create(path, 1024);
    }

    public static TextFile create(String path, int bufferSize) throws FileNotFoundException {
        return create(new DefaultFilesystem(), path, bufferSize);
    }

    public static TextFile create(Filesystem filesystem, String path, int bufferSize) throws FileNotFoundException {
        return new TextFile(
                new PrintWriter(
                        new OutputStreamWriter(
                                new BufferedOutputStream(new FileOutputStream(path), bufferSize),
                                Charsets.UTF_8)));
    }

    public void flush() {
        printWriter.flush();
    }
}
