package pl.edu.icm.trurl.csv;

import net.snowyhollows.bento2.annotation.WithFactory;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.util.DefaultFilesystem;
import pl.edu.icm.trurl.util.Filesystem;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CsvWriter {
    private final Filesystem filesystem;
    private final Pattern NEEDS_ESCAPE = Pattern.compile("[,\\n\\r\"]");

    @WithFactory
    public CsvWriter() {
        this(new DefaultFilesystem());
    }

    public CsvWriter(Filesystem filesystem) {
        this.filesystem = filesystem;
    }

    public void writeCsv(String outputPath, Store store) throws IOException {
        writeCsv(outputPath, store, 0, store.getCount());
    }

    public void writeCsv(String outputPath, Store store, int fromInclusive, int toExclusive) throws IOException {
        try (
                OutputStream outputStream = this.filesystem.openForWriting(new File(outputPath));
                OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                BufferedWriter bufferedWriter = new BufferedWriter(streamWriter, 1024 * 1024 * 128)
        ) {
            Attribute[] attributes = store.attributes().collect(Collectors.toList()).toArray(new Attribute[]{});

            for (int i = 0; i < attributes.length; i++) {
                if (i != 0) {
                    bufferedWriter.write(',');
                }
                writeCharacters(bufferedWriter, attributes[i].name());
            }

            bufferedWriter.write('\n');

            for (int row = fromInclusive; row < toExclusive; row++) {
                for (int i = 0; i < attributes.length; i++) {
                    if (i != 0) {
                        bufferedWriter.write(',');
                    }
                    if (!attributes[i].isEmpty(row)) {
                        writeCharacters(bufferedWriter, attributes[i].getString(row));
                    }
                }
                bufferedWriter.write('\n');
            }
        }
    }

    private void writeCharacters(Writer writer, String chars) throws IOException {
        if (NEEDS_ESCAPE.matcher(chars).find()) {
            writeEscaped(writer, chars);
        } else {
            writer.write(chars);
        }
    }

    private void writeEscaped(Writer writer, String chars) throws IOException {
        int length = chars.length();
        writer.write('"');
        for (int i = 0; i < length; i++) {
            char current = chars.charAt(i);
            switch (current) {
                case '"':
                    writer.write("\"\"");
                    break;
                case '\n':
                case '\r':
                    writer.write("\\n");
                    break;
                default:
                    writer.write(current);
            }
        }
        writer.write('"');
    }
}
