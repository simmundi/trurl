package pl.edu.icm.trurl.io.orc;

import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.trurl.io.orc.wrapper.AbstractColumnWrapper;
import pl.edu.icm.trurl.io.store.SingleStoreReader;
import pl.edu.icm.trurl.store.StoreInspector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for reading data stores from ORC files.
 **/
public class OrcReader implements SingleStoreReader {
    private final OrcImplementationsService orcImplementationsService;
    private final Logger logger = LoggerFactory.getLogger(OrcReader.class);

    public OrcReader(OrcImplementationsService orcImplementationsService) {
        this.orcImplementationsService = orcImplementationsService;
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("hduser"));
    }

    @WithFactory
    public OrcReader() {
        this.orcImplementationsService = new OrcImplementationsService();
    }

    @Override
    public void read(String fileName, StoreInspector store) throws IOException {
        File file = new File(fileName);
        TypeDescription schema;
        VectorizedRowBatch batch;
        RecordReader rows;
        try (Reader reader = orcImplementationsService.createReader(file.getAbsolutePath())) {
            schema = reader.getSchema();
            batch = schema.createRowBatch();
            rows = reader.rows();
            store.attributes().forEach(attribute -> attribute.ensureCapacity((int) reader.getNumberOfRows()));
        }
        List<AbstractColumnWrapper> wrappers = new ArrayList<>();
        List<String> unusedFieldNames = new ArrayList<>(schema.getFieldNames());

        store.attributes().forEach(attribute -> {
            int iof = schema.getFieldNames().indexOf(attribute.name());
            if (iof >= 0) {
                AbstractColumnWrapper wrapper = AbstractColumnWrapper.create(attribute);
                wrapper.setColumnVector(batch.cols[iof]);
                wrappers.add(wrapper);
                unusedFieldNames.remove(attribute.name());
            }
        });

        if (!unusedFieldNames.isEmpty()) {
            logger.warn("ignoring columns: " + unusedFieldNames);
        }

        int targetRow = 0;
        while (rows.nextBatch(batch)) {
            for (AbstractColumnWrapper wrapper : wrappers) {
                wrapper.readFromColumnVector(targetRow, batch.size);
            }
            targetRow += batch.size;
        }

        // TODO
//        store.fireUnderlyingDataChanged(0, targetRow);
    }
}
