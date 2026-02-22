package pl.edu.icm.trurl.io.tiled;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

import pl.edu.icm.trurl.xml.pull.XmlPullParser;
import pl.edu.icm.trurl.xml.pull.XmlPullParserImpl;

import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class XmlLoader {

    private final String tmxDir;

    @WithFactory
    public XmlLoader(@ByName(fallbackValue = ".") String tmxDir) {
        this.tmxDir = tmxDir;
    }

    public XmlPullParser load(String path) throws XMLStreamException, FileNotFoundException {
        return new XmlPullParserImpl(new InputStreamReader(new FileInputStream(tmxDir + "/" + path), StandardCharsets.UTF_8));
    }
}
