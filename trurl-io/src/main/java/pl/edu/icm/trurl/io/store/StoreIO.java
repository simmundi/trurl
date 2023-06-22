package pl.edu.icm.trurl.io.store;

import net.snowyhollows.bento.annotation.WithFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.icm.trurl.store.Store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.util.Arrays.asList;

public class StoreIO {
    private static final String PROPERTIES_EXTENSION = "properties";
    private static final String METADATA_FORMAT_FIELD = "format";
    private static final String METADATA_BASE_NAME_FIELD = "base-name";
    private static final String METADATA_SUBSTORES_FIELD = "substores";
    private final Logger logger = LoggerFactory.getLogger(StoreIO.class);
    private final SingleStoreIOProvider singleStoreIOProvider;

    @WithFactory
    public StoreIO(SingleStoreIOProvider singleStoreIOProvider) {
        this.singleStoreIOProvider = singleStoreIOProvider;
    }


    public void readStoreFromFiles(File metadataFile, Store store) throws IOException {
        Path path = metadataFile.toPath();
        Path parentPath = path.getParent();
        Properties properties = loadProperties(path);
        String format = properties.getProperty(METADATA_FORMAT_FIELD);
        String baseName = properties.getProperty(METADATA_BASE_NAME_FIELD);
        String substores = properties.getProperty(METADATA_SUBSTORES_FIELD);

        SingleStoreReader singleStoreReader = singleStoreIOProvider.getReaderFor(format);
        loadFiles(store, singleStoreReader, parentPath, format, baseName, substores);
    }

    public void writeStoreToFiles(File metadataFile, String baseName, Store store, String format) throws IOException {
        Path path = metadataFile.toPath();
        Path parentDir = path.getParent();
        checkFileExtension(path);

        Properties properties = new Properties();
        properties.setProperty(METADATA_BASE_NAME_FIELD, baseName);
        properties.setProperty(METADATA_FORMAT_FIELD, format);

        SingleStoreWriter singleStoreWriter = singleStoreIOProvider.getWriterFor(format);
        singleStoreWriter.write(getFile(parentDir, baseName, format), store);

        List<String> substoresNamespaces = new LinkedList<>();
        for (Store substore : store.allDescendants()) {
            String namespace = substore.getName();
            substoresNamespaces.add(namespace);
            singleStoreWriter.write(getFile(parentDir, baseName + "." + namespace, format), substore);
        }

        properties.setProperty(METADATA_SUBSTORES_FIELD, String.join(",", substoresNamespaces));
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            properties.store(outputStream, "Metadata for Store");
        }
    }

    private void loadFiles(Store store, SingleStoreReader singleStoreReader, Path parentDir, String format, String baseName, String substores) throws IOException {
        List<String> substoreNames = new ArrayList<>(asList(substores.split(",")));
        singleStoreReader.read(getFile(parentDir, baseName, format), store);
        for (Store substore : store.allDescendants()) {
            String namespace = substore.getName();
            if (!substoreNames.contains(namespace)) {
                throw new IllegalStateException(String.format("No loading candidate found for substore: %s", namespace));
            }
            singleStoreReader.read(getFile(parentDir, baseName + "." + namespace, format), substore);
            substoreNames.remove(namespace);
        }
        if (!substoreNames.isEmpty()) {
            logger.warn(String.format("Some substores are available to load, but were not loaded. Omitted substores: %s", String.join(", ", substoreNames)));
        }
    }

    private static Properties loadProperties(Path path) throws IOException {
        checkFileExtension(path);
        Properties properties = new Properties();
        try (InputStream inStream = Files.newInputStream(path)) {
            properties.load(inStream);
        }
        return properties;
    }

    private static File getFile(Path parentDir, String name, String format) {
        if (!Files.isDirectory(parentDir)) {
            throw new IllegalArgumentException("parentDir must point to a directory");
        }
        return new File(parentDir.toFile(), name + "." + format);
    }

    private static String getExtensionFromPath(Path path) {
        return Optional.ofNullable(path)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf(".") + 1))
                .orElseThrow(() -> new IllegalArgumentException("The path is invalid or the file has no extension."));
    }

    private static void checkFileExtension(Path path) {
        if (!getExtensionFromPath(path).endsWith(PROPERTIES_EXTENSION)) {
            throw new IllegalArgumentException(".properties file should be provided");
        }
    }
}
