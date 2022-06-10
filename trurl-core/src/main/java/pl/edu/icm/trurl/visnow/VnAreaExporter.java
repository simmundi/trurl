package pl.edu.icm.trurl.visnow;

import com.google.common.base.Preconditions;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

public class VnAreaExporter<T> {
    private final Mapper<T> mapper;
    private final Store store;
    private final String baseFileName;
    private final File baseDir;
    private final List<ColumnWrapper> columns;
    private final DataOutputStream dataOut;
    private final Filesystem filesystem;
    private final int fromX;
    private final int width;
    private final int fromY;
    private final int height;

    public static <T> VnAreaExporter<T> create(Filesystem filesystem, Mapper<T> mapper, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        return new VnAreaExporter<T>(filesystem, mapper, baseFilePath, fromX, width, fromY, height);
    }

    public static <T> VnAreaExporter<T> create(Filesystem filesystem, Class<T> areaDescription, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        return new VnAreaExporter<T>(filesystem, Mappers.create(areaDescription), baseFilePath, fromX, width, fromY, height);
    }

    public static <T> VnAreaExporter<T> create(Class<T> areaDescription, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        return new VnAreaExporter<T>(new DefaultFilesystem(), Mappers.create(areaDescription), baseFilePath, fromX, width, fromY, height);
    }

    private VnAreaExporter(Filesystem filesystem, Mapper<T> mapper, String baseFilePath, int fromX, int width, int fromY, int height) throws FileNotFoundException {
        this.filesystem = filesystem;
        this.fromX = fromX;
        this.width = width;
        this.fromY = fromY;
        this.height = height;
        int size = width * height;
        File file = new File(baseFilePath);
        this.baseFileName = file.getName();
        this.store = new ArrayStore(size);
        this.mapper = mapper;
        this.mapper.configureStore(store);
        this.mapper.attachStore(store);
        mapper.ensureCapacity(size);
        this.baseDir = file.getParentFile();
        columns = mapper.attributes().stream()
                .map(c -> ColumnWrapper.from(c))
                .collect(Collectors.toList());
        dataOut = new DataOutputStream(new BufferedOutputStream(filesystem.openForWriting(new File(baseDir, baseFileName + ".vnd")), 1024 * 128));
    }

    public void append(int x, int y, T object) throws IOException {
        Preconditions.checkArgument(x >= fromX && x < fromX + width, "x out of bounds: %s", x);
        Preconditions.checkArgument(y >= fromY && y < fromY + height, "y out of bounds: %s", y);
        int index = (x - fromX) + (y - fromY) * width;
        mapper.save(object, index);
    }

    public void close() throws IOException {
        int size = width * height;
        T component = mapper.create();

        for (int idx = 0; idx < size; idx++) {
            mapper.load(null, component, idx);
            for (ColumnWrapper column : columns) {
                column.writeData(dataOut, idx);
            }
        }

        dataOut.close();
        File vnf = new File(baseDir, baseFileName + ".vnf");
        try (PrintWriter writer = new PrintWriter(filesystem.openForWriting(vnf))) {
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
