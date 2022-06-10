package pl.edu.icm.trurl.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LoggingStatusListener implements StatusListener, AutoCloseable {
    private final TextFile log;

    public LoggingStatusListener() {
        try {
            Files.createDirectories(Paths.get("output/logs"));
            log = TextFile.create(
                    "output/logs/perf-"
                            + LocalDateTime
                            .now(ZoneOffset.UTC)
                            .format(DateTimeFormatter.ISO_DATE_TIME)
                            .replace(':','-')
                            + ".log");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void done(long millis, String text, String comment) {
        log.printlnf("%f,\"%s\",\"%s\",\"done\"", millis / 1000.0, text, comment != null ? comment : "-");
        log.flush();
    }

    @Override
    public void problem(String text, int count) {
        log.printlnf(",,\"%s\",problems,%d", text, count);
        log.flush();
    }

    public void close() throws IOException {
        log.close();
    }
}
