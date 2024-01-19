/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.io.orc;

import com.google.common.io.Files;
import net.snowyhollows.bento.annotation.WithFactory;
import org.apache.hadoop.hive.ql.exec.vector.VectorizedRowBatch;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.orc.TypeDescription;
import org.apache.orc.Writer;
import pl.edu.icm.trurl.io.orc.wrapper.AbstractColumnWrapper;
import pl.edu.icm.trurl.io.store.SingleStoreWriter;
import pl.edu.icm.trurl.store.StoreAccess;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for persisting data stores to a local file system as ORC files.
 */
public class OrcWriter implements SingleStoreWriter {
    private final OrcImplementationsService orcImplementationsService;

    public OrcWriter(OrcImplementationsService orcImplementationsService) {
        this.orcImplementationsService = orcImplementationsService;
        UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("hduser"));
    }

    @WithFactory
    public OrcWriter() {
        this.orcImplementationsService = new OrcImplementationsService();
    }


    @Override
    public void write(String fileName, StoreAccess store) throws IOException {
        File file = new File(fileName);
        int count = store.getCounter().getCount();
        TypeDescription typeDescription = TypeDescription.createStruct();
        List<AbstractColumnWrapper> wrappers = store.getAllAttributes().stream().map(AbstractColumnWrapper::create).collect(Collectors.toList());

        wrappers.forEach(wrapper -> typeDescription.addField(wrapper.getName(), wrapper.getTypeDescription()));

        VectorizedRowBatch batch = typeDescription.createRowBatch();
        batch.reset();
        int maxSize = batch.getMaxSize();
        for (int i = 0; i < batch.cols.length; i++) {
            wrappers.get(i).setColumnVector(batch.cols[i]);
        }

        Files.createParentDirs(file);
        Writer writer = orcImplementationsService.createWriter(file.toString(), typeDescription);
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


    private void writeSingleBatch(List<AbstractColumnWrapper> wrappers,
                                  VectorizedRowBatch batch,
                                  int size,
                                  Writer writer,
                                  int row) throws IOException {
        for (AbstractColumnWrapper wrapper : wrappers) {
            wrapper.writeToColumnVector(row, size);
        }
        batch.size = size;
        writer.addRowBatch(batch);
        batch.reset();
    }
}
