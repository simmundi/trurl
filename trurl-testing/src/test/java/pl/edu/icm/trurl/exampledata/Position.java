/*
 * Copyright (c) 2024 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.dao.annotation.ArrayPacked;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class Position {

    @ArrayPacked(name = "position", offset = 0)
    private float x;
    @ArrayPacked(name = "position", offset = 1)
    private float y;
    @ArrayPacked(name = "position", offset = 2)
    private float z;
    @ArrayPacked(name = "position", offset = 3)
    private float color;

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getColor() {
        return color;
    }

    public void setColor(float color) {
        this.color = color;
    }
}
