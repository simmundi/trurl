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

public class LookAheadReader implements Closeable {
    private final Reader reader;
    private int current;
    private int next;

    public LookAheadReader(Reader reader) {
        this.reader = reader;
        current = rawRead();
        next = rawRead();
    }

    private int  rawRead() {
        try {
            return reader.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void readNext() {
        current = next;
        next = rawRead();
    }

    public int peek() {
        return next;
    }

    public int current() {
        return current;
    }

    public void close() throws IOException {
        reader.close();
    }
}
