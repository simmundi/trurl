/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.io.parser;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public class Parser implements Closeable {
    private final LookAheadReader reader;
    private final StringBuilder buffer = new StringBuilder();

    public Parser(Reader reader) {
        this.reader = new LookAheadReader(reader);
    }

    boolean nextIsEof() {
        return currentIsIn(-1);
    }

    boolean currentIsCrLfEof() {
        return currentIsIn('\r', '\n', -1);
    }

    public void nextPropertiesLine(Map<String, String> properties) {
        if (reader.current() == '#') {
            nextPropertyComment();
            return;
        }
        String key = nextPropertyKey();
        nextEquals();
        String value = nextPropertyValue();
        if(reader.current() == '#') {
            nextPropertyComment();
        }
        properties.put(key, value.trim());
    }

    void nextPropertyComment() {
        if(reader.current() != '#') {
            throw new IllegalStateException("Expected comment, got: " + (char) reader.current());
        }
        while (true) {
            reader.readNext();
            if (currentIsCrLfEof()) {
                return;
            }
        }
    }


    public String nextValue() {
        if (reader.current() == '"') {
            return nextQuotedValue();
        } else if (reader.current() == ','){
            return "";
        } else {
            return nextSimpleValue();
        }
    }

    public String nextPropertyKey() {
        if (currentIsCrLfEof() || reader.current() == '=') {
            throw new IllegalStateException("Expected property key, got: " + (char) reader.current());
        }
        buffer.setLength(0);
        while (true) {
            buffer.append((char) reader.current());
            reader.readNext();
            if (currentIsCrLfEof() || reader.current() == '=') {
                return buffer.toString();
            }
        }
    }

    public void nextEquals() {
        if (currentIsIn('=')) {
            reader.readNext();
        } else {
            throw new IllegalStateException("Expected '=', got: " + (char) reader.current());
        }
    }

    public String nextPropertyValue() {
        buffer.setLength(0);
        if (currentIsIn('#') || currentIsCrLfEof()) {
            throw new IllegalStateException("Expected property value, got: " + (char) reader.current());
        }
        while (true) {
            buffer.append((char) reader.current());
            reader.readNext();
            if (currentIsIn('#') || currentIsCrLfEof()) {
                return buffer.toString();
            }
        }
    }

    public String nextSimpleValue() {
        buffer.setLength(0);
        while (true) {
            if (currentIsCrLfEof() || currentIsIn(',')) {
                return buffer.toString();
            }
            buffer.append((char) reader.current());
            reader.readNext();
        }
    }

    public void nextSeparator() {
        if (currentIsIn(',')) {
            reader.readNext();
        } else if (currentIsCrLfEof()) {
            // ok
        } else {
            throw new IllegalStateException("Expected separator, got: " + (char) reader.current());
        }
    }

    public void nextLine() {
        if (!currentIsIn('\r', '\n', -1)) {
            throw new IllegalStateException("Expected end of line, got: " + (char) reader.current());
        }
        while (currentIsIn('\r', '\n')) {
            reader.readNext();
        }
    }

    public void nextCsvRow(List<String> values) {
        values.clear();
        values.add(nextValue());
        while (!currentIsCrLfEof()) {
            nextSeparator();
            values.add(nextValue());
        }
    }

    public String nextQuotedValue() {
        buffer.setLength(0);
        assert currentIsIn('"');
        reader.readNext();
        while (true) {
            if (reader.current() == '"') {
                if (reader.peek() == '"') {
                    buffer.append('"');
                    reader.readNext();
                    reader.readNext();
                } else {
                    reader.readNext();
                    return buffer.toString();
                }
            } else {
                buffer.append((char) reader.current());
                reader.readNext();
            }
        }
    }


    public boolean hasMore() {
        return reader.current() != -1;
    }

    private boolean currentIsIn(int... c) {
        int read = reader.current();
        for (int c1 : c) {
            if (read == c1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
}
