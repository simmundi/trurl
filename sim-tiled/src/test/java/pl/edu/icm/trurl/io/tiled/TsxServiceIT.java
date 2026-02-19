package pl.edu.icm.trurl.io.tiled;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.FileReader;

@ExtendWith(MockitoExtension.class)
class TsxServiceIT {

    Bento bento = Bento.createRoot();

    @Test
    void test() {
        bento.register("tmxDir", "src/test/resources");
        TsxService tsxService = bento.get(TsxServiceFactory.IT);
        tsxService.load("basictiles.tsx");
    }
}