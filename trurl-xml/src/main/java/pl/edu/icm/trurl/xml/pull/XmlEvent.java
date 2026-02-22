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

import java.util.Collections;
import java.util.Map;

public class XmlEvent {
    public enum Type {
        START_ELEMENT,
        END_ELEMENT,
        CHARACTERS,
        OTHER
    }

    private final Type type;
    private final String name;
    private final String data;
    private final Map<String, String> attributes;

    private XmlEvent(Type type, String name, String data, Map<String, String> attributes) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.attributes = attributes != null ? Collections.unmodifiableMap(attributes) : Collections.emptyMap();
    }

    public static XmlEvent startElement(String name, Map<String, String> attributes) {
        return new XmlEvent(Type.START_ELEMENT, name, null, attributes);
    }

    public static XmlEvent endElement(String name) {
        return new XmlEvent(Type.END_ELEMENT, name, null, null);
    }

    public static XmlEvent characters(String data) {
        return new XmlEvent(Type.CHARACTERS, null, data, null);
    }

    public static XmlEvent other() {
        return new XmlEvent(Type.OTHER, null, null, null);
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getData() {
        return data;
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public boolean isStartElement() {
        return type == Type.START_ELEMENT;
    }

    public boolean isEndElement() {
        return type == Type.END_ELEMENT;
    }

    public boolean isCharacters() {
        return type == Type.CHARACTERS;
    }

    public boolean isWhitespace() {
        return type == Type.CHARACTERS && data.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "XmlEvent{" +
                "type=" + type +
                ", name='" + name + '\'' +
                ", data='" + data + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
