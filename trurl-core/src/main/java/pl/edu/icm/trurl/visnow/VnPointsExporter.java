package pl.edu.icm.trurl.visnow;

import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class VnPointsExporter<T extends VnCoords> {
    private final Mapper<T> mapper;
    private final Store store;
    private final String baseFileName;
    private final File baseDir;
    private int rows;
    private final List<ColumnWrapper> columns;
    private final DataOutputStream dataOut;
    private final Filesystem filesystem;

    private VnPointsExporter(Mapper<T> mapper, Filesystem filesystem, String baseFilePath) throws FileNotFoundException {
        File file = new File(baseFilePath);
        this.baseFileName = file.getName();
        this.store = new ArrayStore(1);
        this.mapper = mapper;
        this.mapper.configureStore(store);
        this.mapper.attachStore(store);
        this.filesystem = filesystem;
        mapper.ensureCapacity(1);
        this.baseDir = file.getParentFile();
        columns = mapper.attributes().stream()
                .filter(c -> !c.name().equals("x") && !c.name().equals("y"))
                .map(c -> ColumnWrapper.from(c))
                .collect(Collectors.toList());
        dataOut = new DataOutputStream(new BufferedOutputStream(
                filesystem.openForWriting(new File(baseDir, baseFileName + ".vnd")),
                1024 * 128));
    }

    public static <T extends VnCoords> VnPointsExporter<T> create(Class<T> componentClass, String baseFileName) {
        try {
            return new VnPointsExporter<>(Mappers.create(componentClass), new DefaultFilesystem(), baseFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends VnCoords> VnPointsExporter<T> create(Class<T> componentClass, Filesystem filesystem, String baseFileName) {
        try {
            return new VnPointsExporter<>(Mappers.create(componentClass), filesystem, baseFileName);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void append(T item) {
        try {
            rows++;
            mapper.save(item, 0);
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
        try (PrintWriter writer = new PrintWriter(filesystem.openForWriting(vnf))) {
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
