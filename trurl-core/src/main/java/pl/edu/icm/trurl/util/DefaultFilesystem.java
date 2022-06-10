package pl.edu.icm.trurl.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class DefaultFilesystem implements Filesystem {

    @Override
    public OutputStream openForWriting(File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public InputStream openForReading(File file) {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public File[] listFiles(File dir, FileFilter fileFilter) {
        return dir.listFiles(fileFilter);
    }

}
