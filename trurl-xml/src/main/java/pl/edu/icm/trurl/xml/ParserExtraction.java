package pl.edu.icm.trurl.xml;

import javax.xml.stream.XMLStreamException;

/**
 * Represents a function evaluated by a parser in a certain context
 *
 * @author fdreger
 */
@FunctionalInterface
public interface ParserExtraction<R> {
    R extract() throws XMLStreamException;
}
