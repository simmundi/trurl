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

import pl.edu.icm.trurl.ecs.dao.annotation.Mapped;
import pl.edu.icm.trurl.ecs.dao.annotation.ReverseReference;
import pl.edu.icm.trurl.ecs.dao.annotation.Type;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
@Mapped(type = Type.DENSE, reverse = ReverseReference.WITH_REVERSE_ATTRIBUTE)
public class Velocity {
    private float dx;
    private float dy;

    public float getDx() {
        return dx;
    }

    public void setDx(float dx) {
        this.dx = dx;
    }

    public float getDy() {
        return dy;
    }

    public void setDy(float dy) {
        this.dy = dy;
    }
}
