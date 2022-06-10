package pl.edu.icm.trurl.io.orc;

import com.google.common.io.Files;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.orc.Reader;
import org.apache.orc.RecordReader;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import pl.edu.icm.trurl.io.orc.wrapper.AbstractColumnWrapper;
import pl.edu.icm.trurl.store.Store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for persisting data stores to local file system as ORC files, and reading them back.
 */
public class OrcStoreService {
    private final OrcImplementationsService orcImplementationsService;

    public OrcStoreService(OrcImplementationsService orcImplementationsService) {
        this.orcImplementationsService = orcImplementationsService;
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("hduser"));
    }

    public OrcStoreService() {
        this.orcImplementationsService = new OrcImplementationsService();
    }

    public boolean fileExists(String fileName) {
        return new File(fileName).exists();
    }

    public void read(Store store, String fileName) throws IOException {

        Reader reader = orcImplementationsService.createReader(fileName);
        TypeDescription schema = reader.getSchema();
        VectorizedRowBatch batch = schema.createRowBatch();
        RecordReader rows = reader.rows();

        store.attributes().forEach(attribute -> attribute.ensureCapacity((int) reader.getNumberOfRows()));
        List<AbstractColumnWrapper> wrappers = new ArrayList<>();

        store.attributes().forEach(attribute -> {
            int iof = schema.getFieldNames().indexOf(attribute.name());
            if (iof >= 0) {
                AbstractColumnWrapper wrapper = AbstractColumnWrapper.create(attribute);
                wrapper.setColumnVector(batch.cols[iof]);
                wrappers.add(wrapper);
            }
        });

        int targetRow = 0;
        while (rows.nextBatch(batch)) {
            for (AbstractColumnWrapper wrapper : wrappers) {
                wrapper.readFromColumnVector(targetRow, batch.size);
            }
            targetRow += batch.size;
        }

        store.fireUnderlyingDataChanged(0, targetRow);
    }

    public void write(Store store, String fileName) throws IOException {
        int count = store.getCount();
        TypeDescription typeDescription = TypeDescription.createStruct();
        List<AbstractColumnWrapper> wrappers = store.attributes().map(AbstractColumnWrapper::create).collect(Collectors.toList());

        wrappers.forEach(wrapper -> typeDescription.addField(wrapper.getName(), wrapper.getTypeDescription()));

        VectorizedRowBatch batch = typeDescription.createRowBatch();
        batch.reset();
        int maxSize = batch.getMaxSize();
        for (int i = 0; i < batch.cols.length; i++) {
            wrappers.get(i).setColumnVector(batch.cols[i]);
        }

        Files.createParentDirs(new File(fileName));
        Writer writer = orcImplementationsService.createWriter(fileName, typeDescription);
        batch.reset();

        int fullBatches = count / maxSize;
        int rest = count % maxSize;

        for (int row = 0; row < fullBatches * maxSize; row += maxSize) {
            writeSingleBatch(wrappers, batch, maxSize, writer, row);
        }
        if (rest > 0) {
            writeSingleBatch(wrappers, batch, rest, writer, fullBatches * maxSize);
        }
        writer.close();
    }

    private void writeSingleBatch(List<AbstractColumnWrapper> wrappers, VectorizedRowBatch batch, int size, Writer writer, int row) throws IOException {
        for (AbstractColumnWrapper wrapper : wrappers) {
            wrapper.writeToColumnVector(row, size);
        }
        batch.size = size;
        writer.addRowBatch(batch);
        batch.reset();
    }

}
