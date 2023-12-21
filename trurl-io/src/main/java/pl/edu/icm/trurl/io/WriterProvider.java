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

package pl.edu.icm.trurl.io;

import net.snowyhollows.bento.annotation.GwtIncompatible;
import net.snowyhollows.bento.annotation.WithFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class WriterProvider {
    @WithFactory
    public WriterProvider() {
    }

    public Writer writerForFile(String path, int bufferSize) {
        return createWriter(path, bufferSize);
    }

    @GwtIncompatible
    private Writer createWriter(String path, int bufferSize) {
        try {
            return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8), bufferSize);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Writer createWriter(Object path, int bufferSize) {
        throw new RuntimeException("Not implemented in GWT; no good means of writing files.");
    }
}
