package pl.edu.icm.trurl.io.store;

import net.snowyhollows.bento.annotation.GwtIncompatible;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.io.ReaderProvider;
import pl.edu.icm.trurl.io.WriterProvider;
import pl.edu.icm.trurl.io.parser.Parser;
import pl.edu.icm.trurl.store.Store;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.util.Arrays.asList;

public class StoreIO {
    private static final String PROPERTIES_EXTENSION = "properties";
    private static final String METADATA_FORMAT_FIELD = "format";
    private static final String METADATA_BASE_NAME_FIELD = "base-name";
    private static final String METADATA_SUBSTORES_FIELD = "substores";
    private final SingleStoreIOProvider singleStoreIOProvider;
    private final ReaderProvider readerProvider;
    private final WriterProvider writerProvider;

    @WithFactory
    public StoreIO(SingleStoreIOProvider singleStoreIOProvider, ReaderProvider readerProvider, WriterProvider writerProvider) {
        this.singleStoreIOProvider = singleStoreIOProvider;
        this.readerProvider = readerProvider;
        this.writerProvider = writerProvider;
    }

    public void readStoreFromFiles(String metadataFile, Store store) throws IOException {
        Map<String, String> properties = loadProperties(metadataFile);
        String parentPath = getParentPath(metadataFile);

        String format = properties.get(METADATA_FORMAT_FIELD);
        String baseName = properties.get(METADATA_BASE_NAME_FIELD);
        String substores = properties.get(METADATA_SUBSTORES_FIELD);

        SingleStoreReader singleStoreReader = singleStoreIOProvider.getReaderFor(format);
        loadFiles(store, singleStoreReader, parentPath, format, baseName, substores);
    }

    private static String getParentPath(String metadataFile) {
        String separator = File.separator;
        return metadataFile.contains(separator) ? metadataFile.substring(0, metadataFile.lastIndexOf(separator)) : ".";
    }

    public void writeStoreToFiles(String metadataFile, String baseName, Store store, String format) throws IOException {
        String parentDir = getParent(metadataFile);
        checkFileExtension(metadataFile);

        Map<String, String> properties = new HashMap<>();
        properties.put(METADATA_BASE_NAME_FIELD, baseName);
        properties.put(METADATA_FORMAT_FIELD, format);

        SingleStoreWriter singleStoreWriter = singleStoreIOProvider.getWriterFor(format);
        singleStoreWriter.write(getFile(parentDir, baseName, format), store);

        List<String> substoresNamespaces = new LinkedList<>();
        for (Store substore : store.allDescendants()) {
            String namespace = substore.getName();
            substoresNamespaces.add(namespace);
            singleStoreWriter.write(getFile(parentDir, baseName + "." + namespace, format), substore);
        }

        properties.put(METADATA_SUBSTORES_FIELD, String.join(",", substoresNamespaces));
        try (Writer writer = writerProvider.writerForFile(metadataFile, 1024)) {
            writer.write("# Metadata for Store\n");
            writer.write("# " + new Date() + "\n");
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        }
    }

    private String getParent(String metadataFile) {
        return getParentPath(metadataFile);
    }

    private void loadFiles(Store store, SingleStoreReader singleStoreReader, String parentDir, String format, String baseName, String substores) throws IOException {
        List<String> substoreNames = new ArrayList<>(asList(substores.split(",")));
        singleStoreReader.read(getFile(parentDir, baseName, format), store);
        for (Store substore : store.allDescendants()) {
            String namespace = substore.getName();
            if (!substoreNames.contains(namespace)) {
                throw new IllegalStateException("No loading candidate found for substore: " + namespace);
            }
            singleStoreReader.read(getFile(parentDir, baseName + "." + namespace, format), substore);
            substoreNames.remove(namespace);
        }
        if (!substoreNames.isEmpty()) {
            System.out.println("Some substores are available to load, but were not loaded. Omitted substores: " + String.join(", ", substoreNames));
        }
    }

    private Map<String, String> loadProperties(String path) throws IOException {
        checkFileExtension(path);
        Map<String, String> properties = new HashMap<>();
        Parser propertiesParser = new Parser(readerProvider.readerForFile(path));
        while (propertiesParser.hasMore()) {
            propertiesParser.nextPropertiesLine(properties);
            propertiesParser.nextLine();
        }
        return properties;
    }

    @GwtIncompatible
    private static String getFile(String parentDir, String name, String format) {
        if (!Files.isDirectory(Paths.get(parentDir))) {
            throw new IllegalArgumentException("parentDir must point to a directory");
        }
        return new File(parentDir, name + "." + format).getAbsolutePath();
    }

    private static String getFile(Object parentDir, String name, String format) {
       return parentDir + "/" + name + "." + format;
    }


    private static String getExtensionFromPath(String path) {
        return Optional.ofNullable(path)
                .filter(name -> name.contains("."))
                .map(name -> name.substring(name.lastIndexOf(".") + 1))
                .orElseThrow(() -> new IllegalArgumentException("The path is invalid or the file has no extension."));
    }

    private static void checkFileExtension(String path) {
        if (!getExtensionFromPath(path).endsWith(PROPERTIES_EXTENSION)) {
            throw new IllegalArgumentException(".properties file should be provided");
        }
    }
}
