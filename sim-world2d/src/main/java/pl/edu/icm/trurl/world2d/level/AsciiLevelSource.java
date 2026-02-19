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

package pl.edu.icm.trurl.world2d.level;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

/**
 * LevelSource that loads entities from an ASCII grid.
 */
public class AsciiLevelSource implements LevelSource {
    private final String[] lines;
    private final float unitWidth;
    private final float unitHeight;

    public AsciiLevelSource(InputStream inputStream, float unitWidth, float unitHeight) throws IOException {
        this.unitWidth = unitWidth;
        this.unitHeight = unitHeight;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            this.lines = reader.lines().toArray(String[]::new);
        }
    }

    @Override
    public void forEach(Consumer<EntityPrototype> consumer) {
        // Tiled usually has (0,0) at top-left, but Y increases downwards.
        // We'll follow a similar pattern: row index is Y, column index is X.
        // But we want to be consistent with our design.
        for (int row = 0; row < lines.length; row++) {
            String line = lines[row];
            for (int col = 0; col < line.length(); col++) {
                char c = line.charAt(col);
                consumer.accept(new AsciiPrototype(c, col * unitWidth, row * unitHeight, unitWidth, unitHeight));
            }
        }
    }

    private static class AsciiPrototype implements EntityPrototype {
        private final char c;
        private final float x, y, w, h;

        public AsciiPrototype(char c, float x, float y, float w, float h) {
            this.c = c;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        @Override public float x() { return x; }
        @Override public float y() { return y; }
        @Override public float width() { return w; }
        @Override public float height() { return h; }
        @Override public String type() { return String.valueOf(c); }
        @Override public String sourceId() { return null; }
        @Override public int representation() { return -1; } // No representation by default
        @Override public String getProperty(String key) { return null; }
        @Override public Map<String, String> allProperties() { return Collections.emptyMap(); }
    }
}
