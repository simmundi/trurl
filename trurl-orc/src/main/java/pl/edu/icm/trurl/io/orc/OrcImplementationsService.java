package pl.edu.icm.trurl.io.orc;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.orc.OrcConf;
import org.apache.orc.OrcFile;
import org.apache.orc.Reader;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;

import java.io.IOException;

/**
 * This service wraps static calls to Orc factory methods
 * and helps to centralize configuration.
 */
public class OrcImplementationsService {

    private final Configuration configuration = new Configuration();

    public OrcImplementationsService() {
        OrcConf.OVERWRITE_OUTPUT_FILE.setBoolean(configuration, true);
    }

    public Writer createWriter(String pathString, TypeDescription typeDescription) throws IOException {
        return OrcFile
                .createWriter(new Path(pathString), OrcFile.writerOptions(configuration)
                        .setSchema(typeDescription));
    }

    public Reader createReader(String pathString) throws IOException {
        return OrcFile.createReader(new Path(pathString),
                OrcFile.readerOptions(configuration));
    }
}
