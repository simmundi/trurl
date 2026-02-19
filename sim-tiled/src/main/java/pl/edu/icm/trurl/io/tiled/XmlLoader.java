package pl.edu.icm.trurl.io.tiled;

import net.snowyhollows.bento.annotation.ByName;
import net.snowyhollows.bento.annotation.WithFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class XmlLoader {

    private final String tmxDir;

    @WithFactory
    public XmlLoader(@ByName(fallbackValue = ".") String tmxDir) {
        this.tmxDir = tmxDir;
    }

    public XMLEventReader load(String path) throws XMLStreamException, FileNotFoundException {
        return XMLInputFactory.newFactory().createXMLEventReader(new FileInputStream(tmxDir + "/" + path));
    }
}
