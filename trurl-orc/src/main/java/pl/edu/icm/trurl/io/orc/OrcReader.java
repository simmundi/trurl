package pl.edu.icm.trurl.io.orc;

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

    public OrcReader() {
        this.orcImplementationsService = new OrcImplementationsService();
    }

    @Override
    public void read(File file, StoreInspector store) throws IOException {
        int storeCount = store.getCounter().getCount();
        if (storeCount > 0) {
            logger.warn(String.format("Loading from file to non-empty store (store count is: %d). New rows will be appended.", storeCount));
        }
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

        while (rows.nextBatch(batch)) {
            int targetRow = store.getCounter().getCount();
            int batchSize = batch.size;
            store.getCounter().next(batchSize);
            for (AbstractColumnWrapper wrapper : wrappers) {
                wrapper.readFromColumnVector(targetRow, batchSize);
            }
        }
    }
}
