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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.xml.pull.XmlPullParserImpl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ParserTest {

    public static final QName QNAME_A = new QName("a");
    public static final QName QNAME_B = new QName("b");
    public static final QName QNAME_C = new QName("c");

    private Parser parser;

    private ParserExecution parserExecution = new ParserExecution() {
        public int count = 0;
        @Override
        public void execute() {
            count++;
        }
    };

    private ParserExecution otherParserExecution = new ParserExecution() {
        public int count = 0;
        @Override
        public void execute() {
            count++;
        }
    };

    private void verify(ParserExecution execution, int times) {
        if (execution == parserExecution) {
            assertThat(((MockExecution)parserExecution).count).isEqualTo(times);
        } else {
            assertThat(((MockExecution)otherParserExecution).count).isEqualTo(times);
        }
    }

    private static class MockExecution implements ParserExecution {
        public int count = 0;
        @Override
        public void execute() {
            count++;
        }
    }

    // -------------------- TESTS --------------------

    @Test
    @DisplayName("Should execute lambda and consume the <b> tag")
    void inElement() throws XMLStreamException {
        // given
        MockExecution mockExecution = new MockExecution();
        initParser("<a><b></b></a>");
        parser.inElement(QNAME_A, () -> {
            parser.inElement(QNAME_B, mockExecution);
        });

        // assert
        assertThat(mockExecution.count).isEqualTo(1);
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
        MockExecution mockExecution = new MockExecution();
        initParser("<a><b></b></a>");
        parser.inElement(QNAME_A, () -> {
            parser.inOptionalElement(QNAME_B, mockExecution);
        });

        // assert
        assertThat(mockExecution.count).isEqualTo(1);
    }

    @Test
    @DisplayName("Should not execute lambda and not consume anything")
    void inOptionalElement__no_execution() throws XMLStreamException {
        // given
        MockExecution mockExecution = new MockExecution();
        initParser("<a><c/></a>");
        parser.inElement(QNAME_A, () -> {
            parser.inOptionalElement(QNAME_B, mockExecution);
            parser.inElement(QNAME_C, () -> {});
        });

        // assert
        assertThat(mockExecution.count).isEqualTo(0);
    }

    @Test
    @DisplayName("Should execute lambda and pass its return value")
    void fromElement() throws XMLStreamException {
        // given
        initParser("<a><b></b></a>");
        int result = parser.fromElement(QNAME_A, () -> {
            return parser.fromElement(QNAME_B, () -> 4);
        });

        // assert
        assertThat(result).isEqualTo(4);
    }

    @Test
    @DisplayName("Should consume all events until <b> tag")
    void skipSiblingsUntil() throws XMLStreamException {
        // given
        initParser("<a>x <x></x><y></y> a b c<b></b></a>");
        parser.inElement(QNAME_A, () -> {
            // execute
            parser.skipSiblingsUntil(QNAME_B);
            parser.inElement(QNAME_B, () -> {});
        });
    }

    @Test
    @DisplayName("Should not execute lambda three times, once for each <b> tag")
    void forEach() throws XMLStreamException {
        // given
        MockExecution mockExecution = new MockExecution();
        initParser("<a><b></b><b></b><b></b><c></c></a>");
        parser.inElement(QNAME_A, () -> {
            parser.forEach(QNAME_B, mockExecution);
            parser.inElement(QNAME_C, () -> {});
        });

        // assert
        assertThat(mockExecution.count).isEqualTo(3);
    }

    @Test
    @DisplayName("Should create a Map.Entry value")
    void caseIf() throws XMLStreamException {
        // execute
        initParser("<a></a>");
        MockExecution mockExecution = new MockExecution();
        Map.Entry<QName, ParserExecution> result = parser.caseIf(QNAME_A, mockExecution);

        // assert
        assertThat(result.getKey()).isEqualTo(QNAME_A);
        assertThat(result.getValue()).isEqualTo(mockExecution);
    }

    @Test
    @DisplayName("Should execute one lambda 2 times and the other 3, once for each <b> and <c> tag")
    void forEachSwitch() throws XMLStreamException {
        // given
        MockExecution mockB = new MockExecution();
        MockExecution mockC = new MockExecution();
        initParser("<a><b></b><c></c><c></c><b></b><c></c><x></x></a>");
        parser.inElement(QNAME_A, () -> {
            // execute
            parser.forEachSwitch(
                    parser.caseIf(QNAME_B, mockB),
                    parser.caseIf(QNAME_C, mockC)
            );
        });

        // assert
        assertThat(mockB.count).isEqualTo(2);
        assertThat(mockC.count).isEqualTo(3);
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
        parser = new Parser(new XmlPullParserImpl(new StringReader(xml)));
    }
}
