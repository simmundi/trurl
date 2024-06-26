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

package pl.edu.icm.trurl.io.visnow;

import net.snowyhollows.bento.config.DefaultWorkDir;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.ecs.dao.DaoProducer;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class VnPointsExporter<T extends VnCoords> {
    private final Dao<T> dao;
    private final Store store;
    private final String baseFileName;
    private final File baseDir;
    private int rows;
    private final List<ColumnWrapper> columns;
    private final DataOutputStream dataOut;
    private final WorkDir workDir;

    private VnPointsExporter(Dao<T> dao, WorkDir workDir, String baseFilePath) throws FileNotFoundException {
        File file = new File(baseFilePath);
        this.baseFileName = file.getName();
        this.store = new Store(new BasicAttributeFactory(), 1);
        this.dao = dao;
        this.dao.configureStore(store);
        this.dao.attachStore(store);
        this.workDir = workDir;
        this.baseDir = file.getParentFile();
        columns = dao.getAttributes().stream()
                .filter(c -> !c.name().equals("x") && !c.name().equals("y"))
                .map(c -> ColumnWrapper.from(c))
                .collect(Collectors.toList());
        dataOut = new DataOutputStream(new BufferedOutputStream(
                workDir.openForWriting(new File(baseDir, baseFileName + ".vnd")),
                1024 * 128));
    }

    public static <T extends VnCoords> VnPointsExporter<T> create(Class<T> componentClass, String baseFileName) {
        try {
            return new VnPointsExporter<>(new DaoProducer().createDao(componentClass), new DefaultWorkDir(), baseFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends VnCoords> VnPointsExporter<T> create(Class<T> componentClass, WorkDir workDir, String baseFileName) {
        try {
            return new VnPointsExporter<>(new DaoProducer().createDao(componentClass), workDir, baseFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void append(T item) {
        try {
            rows++;
            dao.save(item, 0);
            dataOut.writeFloat(item.getX());
            dataOut.writeFloat(item.getY());
            dataOut.writeFloat(0);
            for (ColumnWrapper column : columns) {
                column.writeData(dataOut, 0);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        dataOut.close();
        File vnf = new File(baseDir, baseFileName + ".vnf");
        try (PrintWriter writer = new PrintWriter(workDir.openForWriting(vnf))) {
            writer.print("#VisNow point field\n");
            writer.printf("field \"%s\", nnodes = %d\n", baseFileName, rows);
            for (ColumnWrapper column : columns) {
                writer.println(column.headerDefinition());
            }
            writer.printf("file \"%s\" binary\n", baseFileName + ".vnd");
            writer.print("coord.0,coord.1,coord.2");
            for (ColumnWrapper column : columns) {
                writer.printf(",%s", column.name());
            }
            writer.println();
        }
    }
}
