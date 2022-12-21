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

package pl.edu.icm.trurl.exampledata.pizza;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Random;

@WithMapper
public class Olive {
    private OliveColor color;
    private float size;

    public Olive() {
    }

    public Olive(OliveColor color, float size) {
        this.color = color;
        this.size = size;
    }

    public OliveColor getColor() {
        return color;
    }

    public void setColor(OliveColor color) {
        this.color = color;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public static Olive of(OliveColor color, float size) {
        return new Olive(color, size);
    }

    public static Olive random() {
        OliveColor[] colors = OliveColor.values();
        Random random = new Random();
        return of(colors[random.nextInt(colors.length)], random.nextFloat() * 10 + 1);
    }

}
