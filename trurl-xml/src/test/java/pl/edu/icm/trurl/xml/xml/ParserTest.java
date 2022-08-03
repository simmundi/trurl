package pl.edu.icm.trurl.xml.xml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.xml.Parser;
import pl.edu.icm.trurl.xml.ParserExecution;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ParserTest {

    public static final QName QNAME_A = new QName("a");
    public static final QName QNAME_B = new QName("b");
    public static final QName QNAME_C = new QName("c");

    private XMLEventReader reader;
    private Parser parser;

    @Mock
    private ParserExecution parserExecution;
    @Mock
    private ParserExecution otherParserExecution;

    // -------------------- TESTS --------------------

    @Test
    @DisplayName("Should execute lambda and consume the <b> tag")
    void inElement() throws XMLStreamException {
        // given
        initParser("<a><b></b></a>");
        reader.nextEvent(); // start document
        reader.nextEvent(); // start a

        // execute
        parser.inElement(QNAME_B, parserExecution);

        // assert
        verify(parserExecution, times(1)).execute();
        assertThat(reader.peek().asEndElement().getName()).isEqualTo(QNAME_A);
    }

    @Test
    @DisplayName("Should return name of the next element")
    void nextElementName() throws XMLStreamException {
        // given
        initParser("<?xml version='1.0'?><?distraction a='1'?><!--asdasd--><a><b></b></a>");

        // execute
        QName qName = parser.nextElementName();

        // assert
        assertThat(qName).isEqualTo(QNAME_A);
    }

    @Test
    @DisplayName("Should execute lambda and consume the <b> tag")
    void inOptionalElement() throws XMLStreamException {
        // given
        initParser("<a><b></b></a>");
        reader.nextEvent(); // start document
        reader.nextEvent(); // start a

        // execute
        parser.inOptionalElement(QNAME_B, parserExecution);

        // assert
        verify(parserExecution, times(1)).execute();
        assertThat(reader.peek().asEndElement().getName()).isEqualTo(QNAME_A);
    }

    @Test
    @DisplayName("Should not execute lambda and not consume anything")
    void inOptionalElement__no_execution() throws XMLStreamException {
        // given
        initParser("<a><c/></a>");
        reader.nextEvent(); // start document
        reader.nextEvent(); // start a

        // execute
        parser.inOptionalElement(QNAME_B, parserExecution);

        // assert
        verify(parserExecution, times(0)).execute();
        assertThat(reader.peek().asStartElement().getName()).isEqualTo(QNAME_C);
    }

    @Test
    @DisplayName("Should execute lambda and pass its return value")
    void fromElement() throws XMLStreamException {
        // given
        initParser("<a><b></b></a>");
        reader.nextEvent(); // start document
        reader.nextEvent(); // start a

        // execute
        int result = parser.fromElement(QNAME_B, () -> 4);

        // assert
        assertThat(result).isEqualTo(4);
    }

    @Test
    @DisplayName("Should consume all events until <b> tag")
    void skipSiblingsUntil() throws XMLStreamException {
        // given
        initParser("<a>x <x></x><y></y> a b c<b></b></a>");
        reader.nextEvent(); // start document
        reader.nextEvent(); // start a

        // execute
        parser.skipSiblingsUntil(QNAME_B);

        // assert
        assertThat(reader.peek().asStartElement().getName()).isEqualTo(QNAME_B);
    }

    @Test
    @DisplayName("Should not execute lambda three times, once for each <b> tag")
    void forEach() throws XMLStreamException {
        // given
        initParser("<a><b></b><b></b><b></b><c></c></a>");
        reader.nextEvent(); // start document
        reader.nextEvent(); // start a

        // execute
        parser.forEach(QNAME_B, parserExecution);

        // assert
        verify(parserExecution, times(3)).execute();
        assertThat(reader.peek().asStartElement().getName()).isEqualTo(QNAME_C);
    }

    @Test
    @DisplayName("Should create a Map.Entry value")
    void caseIf() throws XMLStreamException {
        // execute
        initParser("<a></a>");
        Map.Entry<QName, ParserExecution> result = parser.caseIf(QNAME_A, parserExecution);

        // assert
        assertThat(result).isEqualTo(new HashMap.SimpleEntry<>(QNAME_A, parserExecution));
    }

    @Test
    @DisplayName("Should execute one lambda 2 times and the other 3, once for each <b> and <c> tag")
    void forEachSwitch() throws XMLStreamException {
        // given
        initParser("<a><b></b><c></c><c></c><b></b><c></c><x></x></a>");
        reader.nextEvent(); // start document
        reader.nextEvent(); // start a

        // execute
        parser.forEachSwitch(
                parser.caseIf(QNAME_B, parserExecution),
                parser.caseIf(QNAME_C, otherParserExecution)
        );

        // assert
        verify(parserExecution, times(2)).execute();
        verify(otherParserExecution, times(3)).execute();
    }

    @Test
    @DisplayName("Should extract the value from the b attribute")
    void getAttribValue() throws XMLStreamException {
        // given
        initParser("<a b='some attribute'></a>");

        // execute
        String result = parser.fromElement(QNAME_A, () -> parser.getAttribValue(QNAME_B));

        // assert
        assertThat(result).isEqualTo("some attribute");
    }

    @Test
    @DisplayName("Should return null if attribute does not exist")
    void getAttribValue__no_attribute() throws XMLStreamException {
        // given
        initParser("<a x='some attribute'></a>");

        // execute
        String result = parser.fromElement(QNAME_A, () -> parser.getAttribValue(QNAME_B));

        // assert
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should extract tag's string value")
    void elementStringValue() throws XMLStreamException {
        // given
        initParser("<a>this is <b>some</b> text</a>");

        // execute
        String result = parser.fromElement(QNAME_A, () -> parser.getElementTrimmedStringValue());

        // assert
        assertThat(result).isEqualTo("this is some text");
    }

    @Test
    @DisplayName("Should extract tag's xml contents as a string")
    void elementXmlString() throws XMLStreamException {
        // given
        initParser("<a><x>this is <b>some</b> text</x></a>");

        // execute
        String result = parser.fromElement(QNAME_A, () -> parser.getElementXmlString());

        // assert
        assertThat(result).isEqualTo("<x>this is <b>some</b> text</x>");
    }

    @Test
    @DisplayName("Should extract tag's contents as a string, properly handling entities")
    void elementStringValue__entities() throws XMLStreamException {
        // given
        initParser("<a>tag &lt;i&gt;!</a>");

        // execute
        String result = parser.fromElement(QNAME_A, () -> parser.getElementStringValue());

        // assert
        assertThat(result).isEqualTo("tag <i>!");
    }

    // -------------------- PRIVATE --------------------

    private void initParser(String xml) throws XMLStreamException {
        reader = XMLInputFactory
                .newFactory()
                .createXMLEventReader(new StringReader(xml));
        parser = new Parser(reader, XMLOutputFactory.newFactory()) {};
    }
}
