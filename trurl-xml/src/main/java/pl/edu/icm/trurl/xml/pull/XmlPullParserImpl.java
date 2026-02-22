/*
 * Copyright (c) 2026 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.xml.pull;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class XmlPullParserImpl implements XmlPullParser {
    private final PushbackReader reader;
    private final LinkedList<XmlEvent> eventQueue = new LinkedList<>();

    public XmlPullParserImpl(Reader reader) {
        this.reader = new PushbackReader(reader, 1024);
    }

    @Override
    public XmlEvent next() throws IOException {
        if (eventQueue.isEmpty()) {
            readNextInternal();
        }
        return eventQueue.poll();
    }

    @Override
    public XmlEvent peek() throws IOException {
        if (eventQueue.isEmpty()) {
            readNextInternal();
        }
        return eventQueue.peek();
    }

    private void readNextInternal() throws IOException {
        int c = reader.read();
        if (c == -1) return;

        if (c == '<') {
            int next = reader.read();
            if (next == '/') {
                eventQueue.add(XmlEvent.endElement(readEndElementName()));
            } else if (next == '!') {
                // Comment or CDATA or DOCTYPE
                int n2 = reader.read();
                if (n2 == '-' && (reader.read() == '-')) {
                    skipComment();
                    eventQueue.add(XmlEvent.other());
                } else {
                    // Simplified: just skip until >
                    skipUntil('>');
                    eventQueue.add(XmlEvent.other());
                }
            } else if (next == '?') {
                skipUntil('>');
                eventQueue.add(XmlEvent.other());
            } else {
                reader.unread(next);
                readStartElement();
            }
        } else {
            reader.unread(c);
            eventQueue.add(readCharacters());
        }
    }

    private void readStartElement() throws IOException {
        String name = readName();
        Map<String, String> attributes = new HashMap<>();
        while (true) {
            skipWhitespace();
            int c = reader.read();
            if (c == '>') {
                eventQueue.add(XmlEvent.startElement(name, attributes));
                return;
            } else if (c == '/') {
                if (reader.read() == '>') {
                    eventQueue.add(XmlEvent.startElement(name, attributes));
                    eventQueue.add(XmlEvent.endElement(name));
                    return;
                }
            } else if (c == -1) {
                eventQueue.add(XmlEvent.startElement(name, attributes));
                return;
            } else {
                reader.unread(c);
                String attrName = readName();
                skipWhitespace();
                if (reader.read() == '=') {
                    skipWhitespace();
                    String attrValue = readAttributeValue();
                    attributes.put(attrName, attrValue);
                }
            }
        }
    }

    private String readEndElementName() throws IOException {
        String name = readName();
        skipUntil('>');
        return name;
    }

    private XmlEvent readCharacters() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1 && c != '<') {
            sb.append((char) c);
        }
        if (c != -1) reader.unread(c);
        return XmlEvent.characters(decodeEntities(sb.toString()));
    }

    private String readName() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1 && (Character.isLetterOrDigit(c) || c == ':' || c == '_' || c == '.' || c == '-')) {
            sb.append((char) c);
        }
        if (c != -1) reader.unread(c);
        return sb.toString();
    }

    private String readAttributeValue() throws IOException {
        int quote = reader.read();
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = reader.read()) != -1 && c != quote) {
            sb.append((char) c);
        }
        return decodeEntities(sb.toString());
    }

    private void skipWhitespace() throws IOException {
        int c;
        while ((c = reader.read()) != -1 && Character.isWhitespace(c)) ;
        if (c != -1) reader.unread(c);
    }

    private void skipUntil(char target) throws IOException {
        int c;
        while ((c = reader.read()) != -1 && c != target) ;
    }

    private void skipComment() throws IOException {
        int c;
        while ((c = reader.read()) != -1) {
            if (c == '-' && reader.read() == '-' && reader.read() == '>') {
                return;
            }
        }
    }

    private String decodeEntities(String input) {
        return input.replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&apos;", "'");
    }
}
