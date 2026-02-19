package pl.edu.icm.trurl.io.tiled;

import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.io.tiled.parser.TmxParser;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class TmxService {
    private final XmlLoader xmlLoader;
    private final TiledCartographer tiledCartographer;

    @WithFactory
    public TmxService(XmlLoader xmlLoader, TiledCartographer tiledCartographer) {
        this.xmlLoader = xmlLoader;
        this.tiledCartographer = tiledCartographer;
    }

    public void loadTmx(String tmxPath) {
        try {
            new TmxParser(xmlLoader.load(tmxPath)).loadMap(tiledCartographer);
        } catch (XMLStreamException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
