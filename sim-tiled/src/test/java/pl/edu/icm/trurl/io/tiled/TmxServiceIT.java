package pl.edu.icm.trurl.io.tiled;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;

@ExtendWith(MockitoExtension.class)
class TmxServiceIT {
    Bento bento = Bento.createRoot();

    @Test
    void test() {
        bento.register("tmxDir", "src/test/resources");
        TmxService tmxService = bento.get(TmxServiceFactory.IT);
        tmxService.loadTmx("basictiles.tmx");
    }
}
