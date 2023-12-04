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


    /**
     * Reads and loads data into the provided Store from the information specified in a properties file.
     * <p>
     * Currently,
     * this method loads data into empty stores and substores only
     * (i.e., those that satisfy store.getCounter().getCount() == 0),
     * as loading into non-empty stores could disrupt references and joins.
     *
     * @param metadataFile A File object representing the properties file containing metadata information.
     * @param store        The Store object where the data is to be loaded.
     * @throws IOException If an I/O error occurs while reading the properties file or loading data into the store.
     */
    public void readStoreFromFiles(File metadataFile, Store store) throws IOException {
        assertEmpty(store);
        Path path = metadataFile.toPath().toAbsolutePath();
        Path parentPath = path.getParent();
        Properties properties = loadProperties(path);
        String format = properties.getProperty(METADATA_FORMAT_FIELD);
        String baseName = properties.getProperty(METADATA_BASE_NAME_FIELD);
        String substores = properties.getProperty(METADATA_SUBSTORES_FIELD);

        SingleStoreReader singleStoreReader = singleStoreIOProvider.getReaderFor(format);
        loadFiles(store, singleStoreReader, parentPath, format, baseName, substores);
    }

    /**
     * Writes the data from the provided Store to multiple files, along with a properties file
     * containing metadata information.
     *
     * @param metadataFile   A File object representing the properties file to be created with metadata information.
     * @param baseName       The base name for the files to be created.
     * @param store          The Store object from which the data is to be written.
     * @param format         The format in which the data should be written to the files.
     *                       The format should have its single store writer implementation
     *                       registered in singleStoreIOProvider
     * @throws IOException  If an I/O error occurs while writing files.
     */
    public void writeStoreToFiles(File metadataFile, String baseName, Store store, String format) throws IOException {
        Path path = metadataFile.toPath().toAbsolutePath();
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
            assertEmpty(substore);
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

    private static void assertEmpty(Store store) {
        int storeCount = store.getCounter().getCount();
        if (storeCount > 0) {
            String name = store.getName();
            throw new IllegalStateException("Loading available only for empty stores (store" + (name.isEmpty() ? "" : "<" + name + ">") + " count is: " + storeCount + ")");
        }
    }
}
