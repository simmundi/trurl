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

package pl.edu.icm.trurl.visnow;

import com.google.common.base.Preconditions;
import net.snowyhollows.bento.config.DefaultWorkDir;
import net.snowyhollows.bento.config.WorkDir;
import pl.edu.icm.trurl.ecs.NoCacheSession;
import pl.edu.icm.trurl.ecs.SessionFactory;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.ecs.dao.Daos;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayAttributeFactory;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class VnAreaExporter<T> {
    private final Dao<T> dao;
    private final Store store;
    private final String baseFileName;
    private final File baseDir;
    private final List<ColumnWrapper> columns;
    private final DataOutputStream dataOut;
    private final WorkDir workDir;
    private final NoCacheSession noCacheSession;
    private final int fromX;
    private final int width;
    private final int fromY;
    private final int height;

    public static <T> VnAreaExporter<T> create(WorkDir workDir, Dao<T> dao, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        return new VnAreaExporter<T>(workDir, dao, baseFilePath, fromX, width, fromY, height);
    }

    public static <T> VnAreaExporter<T> create(WorkDir workDir, Class<T> areaDescription, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        return new VnAreaExporter<T>(workDir, new Daos().create(areaDescription), baseFilePath, fromX, width, fromY, height);
    }

    public static <T> VnAreaExporter<T> create(Class<T> areaDescription, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        return new VnAreaExporter<T>(new DefaultWorkDir(), new Daos().create(areaDescription), baseFilePath, fromX, width, fromY, height);
    }

    private VnAreaExporter(WorkDir workDir, Dao<T> dao, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        this.workDir = workDir;
        this.fromX = fromX;
        this.width = width;
        this.fromY = fromY;
        this.height = height;
        int size = width * height;
        File file = new File(baseFilePath);
        this.baseFileName = file.getName();
        this.store = new Store(new ArrayAttributeFactory(), size);
        this.dao = dao;
        this.dao.configureStore(store);
        this.dao.attachStore(store);
//        dao.ensureCapacity(size);
        this.baseDir = file.getParentFile();
        columns = dao.attributes().stream()
                .map(c -> ColumnWrapper.from(c))
                .collect(Collectors.toList());
        dataOut = new DataOutputStream(new BufferedOutputStream(workDir.openForWriting(new File(baseDir, baseFileName + ".vnd")), 1024 * 128));
        noCacheSession = new SessionFactory(null, NoCacheSession.Mode.STUB_ENTITIES).createOrGet();
    }

    public void append(int x, int y, T object) throws IOException {
        Preconditions.checkArgument(x >= fromX && x < fromX + width, "x out of bounds: %s", x);
        Preconditions.checkArgument(y >= fromY && y < fromY + height, "y out of bounds: %s", y);
        int index = (x - fromX) + (y - fromY) * width;
        dao.save(noCacheSession, object, index);
    }

    public void close() throws IOException {
        int size = width * height;
        T component = dao.create();

        for (int idx = 0; idx < size; idx++) {
            dao.load(null, component, idx);
            for (ColumnWrapper column : columns) {
                column.writeData(dataOut, idx);
            }
        }

        dataOut.close();
        File vnf = new File(baseDir, baseFileName + ".vnf");
        try (PrintWriter writer = new PrintWriter(workDir.openForWriting(vnf))) {
            writer.print("#VisNow regular field\n");
            writer.printf("field \"%s\", dims %d %d\n", baseFileName, width, height);
            writer.printf("x %d %d\n", fromX, fromX + width);
            writer.printf("y %d %d\n", fromY, fromY + height);
            for (ColumnWrapper column : columns) {
                writer.println(column.headerDefinition());
            }
            writer.printf("file \"%s\" binary\n", baseFileName + ".vnd");
            writer.println(columns.stream().map(ColumnWrapper::name)
                    .collect(Collectors.joining(",")));
        }
    }
}
