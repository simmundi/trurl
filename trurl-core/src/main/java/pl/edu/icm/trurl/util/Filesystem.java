package pl.edu.icm.trurl.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Filesystem {
    OutputStream openForWriting(File file);

    InputStream openForReading(File file);

    File[] listFiles(File dir, FileFilter fileFilter);
}
