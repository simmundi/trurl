package pl.edu.icm.trurl.xml;

import javax.xml.stream.XMLStreamException;

/**
 * Represents a procedure executed by a parser in a certain context
 *
 * @author fdreger
 */
@FunctionalInterface
public interface ParserExecution {
    void execute() throws XMLStreamException;
}
