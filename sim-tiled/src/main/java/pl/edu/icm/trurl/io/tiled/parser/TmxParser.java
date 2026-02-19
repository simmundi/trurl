package pl.edu.icm.trurl.io.tiled.parser;

import pl.edu.icm.trurl.io.tiled.TiledCartographer;
import pl.edu.icm.trurl.io.tiled.TiledSingleMap;
import pl.edu.icm.trurl.xml.Parser;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.xml.namespace.QName.valueOf;

public class TmxParser extends Parser {
    private final Pattern DIGITS = Pattern.compile("\\d+");

    public TmxParser(XMLEventReader reader) {
        super(reader, null);
    }

    public void loadMap(TiledCartographer cartographer) throws XMLStreamException {
        inElement(valueOf("map"), () -> {
            int tileWidth = getAnInt("tilewidth");
            int tileHeight = getAnInt("tileheight");
            int mapWidth = getAnInt("width");
            int mapHeight = getAnInt("height");
            TiledSingleMap mapBuilder = cartographer.createMapBuilder();
            forEach(valueOf("tileset"), () -> {
                int firstGid = getAnInt("firstgid");
                String source = getAttribValue(valueOf("source"));
                mapBuilder.addTileset(firstGid, source);
            });
            forEachSwitch(
                    caseIf(valueOf("layer"), () -> {
                        int layerId = getAnInt("id");
                        String layerName = getAttribValue(valueOf("name"));
                        int offsetX = getAnInt("offsetx", 0);
                        int offsetY = getAnInt("offsety", 0);
                        int width = getAnInt("width");
                        int height = getAnInt("height");

                        int originX = offsetX;
                        int originY = mapHeight * tileHeight - offsetY;

                        inElement(valueOf("data"), () -> {
                            String encoding = getAttribValue(valueOf("encoding"));
                            String contents = getElementTrimmedStringValue();
                            if (!"csv".equals(encoding)) {
                                throw new IllegalArgumentException("tile layer encoding type not supported: " + encoding);
                            }

                            Matcher matcher = DIGITS.matcher(contents);

                            for (int row = 0; row < height; row++) {
                                for (int col = 0; col < width; col++) {
                                    if (!matcher.find()) {
                                        throw new IllegalArgumentException("No tile data for x=" + col + " y=" + row);
                                    }
                                    int gid = Integer.parseInt(matcher.group());
                                    mapBuilder.addTile(originX + col * tileWidth, originY - (row * tileHeight) - tileHeight, gid);
                                }
                            }
                        });
                    }),
                    caseIf(valueOf("objectgroup"), () -> {
                        int layerId = getAnInt("id"); // TODO
                        String layerName = getAttribValue(valueOf("name")); // TODO
                        int offsetX = getAnInt("offsetx", 0);
                        int offsetY = getAnInt("offsety", 0);

                        int originX = offsetX;
                        int originY = mapHeight * tileHeight - offsetY;
                        forEach(valueOf("object"), () -> {
                            String name = getAttribValue(valueOf("name"));
                            String gidString = getAttribValue(valueOf("gid"));
                            int gid = Integer.MIN_VALUE;
                            int x = getAnInt("x");
                            int y = getAnInt("y");
                            AtomicInteger width = new AtomicInteger(getAnInt("width"));
                            AtomicInteger height = new AtomicInteger(getAnInt("height"));
                            String id = getAttribValue(valueOf("id"));
                            AtomicReference<TiledSingleMap.Shape> shape = new AtomicReference<>(TiledSingleMap.Shape.TILE);

                            if (gidString == null) {
                                shape.set(TiledSingleMap.Shape.RECTANGLE);
                            } else {
                                gid = Integer.parseInt(gidString);
                            }

                            Map<String, String> properties = new HashMap<>();
                            Set<String> referenceProperties = new HashSet<>();
                            if (name != null) {
                                properties.put("name", name);
                            }


                            forEachSwitch(
                                    caseIf(valueOf("properties"), () -> {
                                        forEach(valueOf("property"), () -> {
                                            String propName = getAttribValue(valueOf("name"));
                                            String propValue = getAttribValue(valueOf("value"));

                                            if ("width".equals(propName)) {
                                                width.set((int) Float.parseFloat(propValue));
                                            } else if ("height".equals(propName)) {
                                                height.set((int) Float.parseFloat(propValue));
                                            }

                                            properties.put(propName, propValue);
                                            if ("object".equals(getAttribValue(valueOf("type")))) {
                                                referenceProperties.add(propName);
                                            }
                                        });
                                    }),
                                    caseIf(valueOf("text"), () -> {
                                        properties.put("text", getElementTrimmedStringValue());
                                    }),
                                    caseIf(valueOf("ellipse"), () -> {
                                        shape.set(TiledSingleMap.Shape.ELLIPSE);
                                    }),
                                    caseIf(valueOf("polygon"), () -> {
                                        shape.set(TiledSingleMap.Shape.POLYGON);
                                    })
                            );
                            mapBuilder.addObject(
                                    x + originX,
                                    (shape.get() != TiledSingleMap.Shape.RECTANGLE ? originY - y : originY - y - height.get()),
                                    id, gid, width.get(), height.get(), shape.get(), properties, referenceProperties);

                        });
                    })
            );
            mapBuilder.connectEntities();
        });
    }

    private int getAnInt(String attribute, int defaultValue) {
        String stringAttribValue = getAttribValue(valueOf(attribute));
        return stringAttribValue != null ? (int) Double.parseDouble(stringAttribValue) : defaultValue;
    }

    private int getAnInt(String attribute) {
        return getAnInt(attribute, Integer.MIN_VALUE);
    }

}
