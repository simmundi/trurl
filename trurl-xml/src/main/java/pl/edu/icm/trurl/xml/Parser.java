/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.xml;

import com.google.common.base.Preconditions;

import pl.edu.icm.trurl.xml.pull.XmlEvent;
import pl.edu.icm.trurl.xml.pull.XmlPullParser;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Common base for concrete parsers.
 * <p>
 * Wrapper around the Java pull parser, offering methods for writing recursive parsers
 * in an object-oriented, stateful way.
 * <p>
 * Since the XMLEventStream is passed into the constructor, Parser objects are meant
 * to be used only once, in a single thread.
 * <p>
 * The major intended use of the class is to extend it, adding a single method
 * performing the actual parsing.
 */
public class Parser {

    private final XmlPullParser reader;
    private XmlEvent lastEvent;

    // -------------------- CONSTRUCTORS --------------------

    public Parser(XmlPullParser reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    // -------------------- LOGIC --------------------

    /**
     * Consumes the next start element of the given name, skipping any whitespace and non-element contents
     * immediately executes the executor (assuming it will not consume events outside of the current element),
     * consumes the rest of events left before the end tag, along with the end tag
     */
    public void inElement(QName qName, ParserExecution parserExecution) throws XMLStreamException {
        skipUntilStartElement();
        enter(qName);
        parserExecution.execute();
        skipRestOfElement();
        leave(qName);
        skipWhitespace();
    }

    /**
     * Exactly like inElement, but does not throw if the element does not exist.
     */
    public void inOptionalElement(QName qName, ParserExecution parserExecution) throws XMLStreamException {
        skipWhitespace();
        XmlEvent next = peek();
        if (next == null || !next.isStartElement()) {
            return;
        }
        if (!next.getName().equals(qName.getLocalPart())) {
            return;
        }
        enter(qName);
        parserExecution.execute();
        skipRestOfElement();
        leave(qName);
        skipWhitespace();
    }

    /**
     * Exactly like inElement, but uses #ParserExtractor instead of #ParserExecution, making
     * it possible to return values from the element (handy for returning simple, immutable values)
     */
    public <R> R fromElement(QName qName, ParserExtraction<R> parserExtraction) throws XMLStreamException {
        skipWhitespace();
        enter(qName);
        R result = parserExtraction.extract();
        skipRestOfElement();
        leave(qName);
        skipWhitespace();
        return result;
    }

    /**
     * Consumes all the whitespace up to the first opening tag;
     *
     * @throws XMLStreamException if the next element is not an opening tag
     */
    public QName nextElementName() throws XMLStreamException {
        skipUntilStartElement();
        XmlEvent considering = peek();
        if (considering.isStartElement()) {
            return QName.valueOf(considering.getName());
        }
        throw new XMLStreamException("Expected start element");
    }

    /**
     * consumes all events which are siblings of the last consumed element (does not descend)
     * until the upcoming element is a start tag with the given name
     *
     * @throws XMLStreamException on wrong xml or when end of the document reached
     */
    public void skipSiblingsUntil(QName qName) throws XMLStreamException {
        while (!isNext(qName)) {
            XmlEvent considering = peek();
            if (considering.isStartElement()) {
                skipElement();
            } else {
                lastEvent = consumeNextEvent();
            }
        }
    }

    /**
     * Processes a sequence of zero or more elements with the same qName in a row,
     * executing the given #ParserExecution for each of them.
     */
    public void forEach(QName qName, ParserExecution parserExecution) throws XMLStreamException {
        skipWhitespace();
        while (isNext(qName)) {
            enter(qName);
            parserExecution.execute();
            skipRestOfElement();
            leave(qName);
            skipWhitespace();
        }
    }

    /**
     * Takes a list of qnames, each with an assigned execution; the list is meant to
     * be created with the #caseIf() helper method;
     * consumes all the remaining siblings of the element currently being parsed
     * (does not descend or ascend), if an execution is defined for the element
     * met, it is run.
     */
    public void forEachSwitch(Map.Entry<QName, ParserExecution>... cases) throws XMLStreamException {
        final Map<QName, ParserExecution> casesMap = new HashMap<>();
        for (Map.Entry<QName, ParserExecution> aCase : cases) {
            casesMap.put(aCase.getKey(), aCase.getValue());
        }
        final Set<QName> qNames = casesMap.keySet();

        skipWhitespace();

        while (isNextAStartElement()) {
            if (isNextIn(qNames)) {
                QName qName = nameOfNextStartElement();
                enter(qName);
                casesMap.get(qName).execute();
                skipRestOfElement();
                leave(qName);
            } else {
                skipElement();
            }
            skipWhitespace();
        }

    }

    /**
     * A helper method to prepare parameters for forEachSwitch
     */
    public Map.Entry<QName, ParserExecution> caseIf(QName qName, ParserExecution parserExecution) {
        return new HashMap.SimpleImmutableEntry<>(qName, parserExecution);
    }

    /**
     * returns the value of an attribute of the last event consumed or null
     * if even does not exist or the current event is not a startElement.
     *
     */
    public String getAttribValue(QName qName) {
        if (!lastEvent().isStartElement()) {
            return null;
        }
        return lastEvent().getAttribute(qName.getLocalPart());
    }

    /**
     * returns the string value of events from the last consumed until the end of the last entered element
     */
    public String getElementStringValue() throws XMLStreamException {

        StringBuilder output = new StringBuilder();
        int level = 0;

        while (!(level == 0 && peek().isEndElement())) {
            if (peek().isStartElement()) {
                level++;
            } else if (peek().isEndElement()) {
                level--;
            }

            consumeNextEvent();

            if (lastEvent().isCharacters()) {
                output.append(lastEvent().getData());
            }

        }

        return output.toString();
    }

    /**
     * Same as elementStringValue, but returns the string trimmed.
     */
    public String getElementTrimmedStringValue() throws XMLStreamException {
        return getElementStringValue().trim();
    }

    /**
     * returns the XML string being an exact copy of the next event.
     *
     * @throws XMLStreamException if the next event is not a start tag
     */
    public String getElementXmlString() throws XMLStreamException {
        skipWhitespace();
        Preconditions.checkState(peek().isStartElement(), "only elements can be turned into stringified xml");

        StringBuilder output = new StringBuilder();

        int level = 0;
        do {
            consumeNextEvent();

            if (lastEvent().isStartElement()) {
                level++;
                output.append("<").append(lastEvent().getName());
                for (Map.Entry<String, String> entry : lastEvent().getAttributes().entrySet()) {
                    output.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
                }
                output.append(">");
            } else if (lastEvent().isEndElement()) {
                level--;
                output.append("</").append(lastEvent().getName()).append(">");
            } else if (lastEvent().isCharacters()) {
                output.append(lastEvent().getData());
            }


        } while (level != 0);

        return output.toString();
    }

    // -------------------- PRIVATE --------------------

    /**
     * returns the next event that will be consumed
     */
    private XmlEvent peek() throws XMLStreamException {
        try {
            return reader.peek();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }

    /**
     * returns the last event consumed
     */
    private XmlEvent lastEvent() {
        return lastEvent;
    }

    /**
     * consumes an event while asserting that the event is opening of the given tag
     *
     * @throws XMLStreamException if the event is different than expected
     */
    private void enter(QName qName) throws XMLStreamException {
        skipWhitespace();
        consumeNextEvent();
        if (!lastEvent.isStartElement() || !lastEvent.getName().equals(qName.getLocalPart())) {
            throw new XMLStreamException(String.format("enter expected %s, instead found %s", qName, lastEvent));
        }
    }

    /**
     * consumes an event while asserting that the event is closing of the given tag
     *
     * @throws XMLStreamException if the event is different than expected
     */
    private void leave(QName qName) throws XMLStreamException {
        skipWhitespace();
        consumeNextEvent();
        if (!lastEvent.isEndElement() || !lastEvent.getName().equals(qName.getLocalPart())) {
            throw new XMLStreamException(String.format("leave expected %s, instead found %s", qName, lastEvent));
        }
    }

    private boolean isNextAStartElement() throws XMLStreamException {
        XmlEvent next = peek();
        return next != null && next.isStartElement();
    }

    private boolean isNext(QName qName) throws XMLStreamException {
        XmlEvent next = peek();
        return next != null && next.isStartElement() && next.getName().equals(qName.getLocalPart());
    }

    private boolean isNextIn(Set<QName> qNames) throws XMLStreamException {
        XmlEvent next = peek();
        if (next == null || !next.isStartElement()) {
            return false;
        }
        for (QName qName : qNames) {
            if (next.getName().equals(qName.getLocalPart())) {
                return true;
            }
        }
        return false;
    }


    private QName nameOfNextStartElement() throws XMLStreamException {
        return QName.valueOf(peek().getName());
    }

    private void skipWhitespace() throws XMLStreamException {
        while (peek() != null && (peek().isWhitespace() || peek().getType() == XmlEvent.Type.OTHER)) {
            consumeNextEvent();
        }
    }

    private void skipUntilStartElement() throws XMLStreamException {
        while (peek() != null && !peek().isStartElement()) {
            consumeNextEvent();
        }
    }

    private boolean isOnWhitespace() throws XMLStreamException {
        return peek() != null && peek().isWhitespace();
    }

    private void skipElement() throws XMLStreamException {
        int level = 0;
        do {
            consumeNextEvent();

            if (lastEvent().isStartElement()) {
                level++;
            } else if (lastEvent().isEndElement()) {
                level--;
            }

        } while (level != 0);
    }

    private void skipRestOfElement() throws XMLStreamException {
        int depth = 1; // we are inside the current element
        while (depth > 0) {
            XmlEvent next = peek();
            if (next == null) return;
            if (next.isStartElement()) {
                depth++;
                consumeNextEvent();
            } else if (next.isEndElement()) {
                depth--;
                if (depth == 0) {
                    return; // do not consume the closing tag of the current element
                }
                consumeNextEvent();
            } else {
                consumeNextEvent();
            }
        }
    }


    private XmlEvent consumeNextEvent() throws XMLStreamException {
        try {
            return lastEvent = reader.next();
        } catch (IOException e) {
            throw new XMLStreamException(e);
        }
    }
}
