package pl.edu.icm.trurl.io.tiled.parser;

import pl.edu.icm.trurl.io.tiled.model.TileMetadata;
import pl.edu.icm.trurl.io.tiled.model.TilesetMetadata;
import pl.edu.icm.trurl.xml.Parser;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;

import static javax.xml.namespace.QName.valueOf;

public class TsxParser extends Parser {

    private final String tsxPath;

    public TsxParser(XMLEventReader reader, String tsxPath) {
        super(reader, null);
        this.tsxPath = tsxPath;
    }

    public TilesetMetadata load() throws XMLStreamException {
        TilesetMetadata result = new TilesetMetadata();

        inElement(valueOf("tileset"), () -> {
            int tileWidth = Integer.valueOf(getAttribValue(valueOf("tilewidth")));
            int tileHeight = Integer.valueOf(getAttribValue(valueOf("tileheight")));
            int tileCount = Integer.parseInt(getAttribValue(valueOf("tilecount")));
            int columns = Integer.parseInt(getAttribValue(valueOf("columns")));

            result.setTileWidth(tileWidth);
            result.setTileHeight(tileHeight);
            result.setTileCount(tileCount);
            result.setColumns(columns);

            forEachSwitch(
                    caseIf(valueOf("image"), () -> {
                        String source = getAttribValue(valueOf("source"));
                        result.setImagePath(source);
                    }),
                    caseIf(valueOf("tile"), () -> {
                        int localId = Integer.valueOf(getAttribValue(valueOf("id")));
                        TileMetadata tile = result.getTileByLocalId(localId);
                        String type = getAttribValue(valueOf("type"));

                        tile.setType(type);
                        forEachSwitch(
                                caseIf(valueOf("properties"), () -> {
                                    forEach(valueOf("property"), () -> {
                                        String name = getAttribValue(valueOf("name"));
                                        String value = getAttribValue(valueOf("value"));
                                        String propType = getAttribValue(valueOf("type"));
                                        if (!"object".equals(propType)) {
                                            tile.putProperty(name, value);
                                        }
                                    });
                                }),
                                caseIf(valueOf("animation"), () -> {
                                    forEach(valueOf("frame"), () -> {
                                        int tileId = Integer.valueOf(getAttribValue(valueOf("tileid")));
                                        int duration = Integer.valueOf(getAttribValue(valueOf("duration")));
                                        tile.addAnimationFrame(tileId, duration / 1000f);
                                    });
                                })
                        );
                    })
            );
        });
        return result;
   }
}
