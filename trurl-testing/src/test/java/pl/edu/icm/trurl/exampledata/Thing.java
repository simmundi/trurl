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

package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.dao.annotation.Mapped;
import pl.edu.icm.trurl.ecs.dao.annotation.Type;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import static pl.edu.icm.trurl.ecs.dao.annotation.ReverseReference.ONLY_REVERSE_ATTRIBUTE;
import static pl.edu.icm.trurl.ecs.dao.annotation.ReverseReference.WITH_REVERSE_ATTRIBUTE;

@WithDao
public class Thing {
    @Mapped(type = Type.DENSE)
    Coordinates coordinates;

    @Mapped(type = Type.DENSE, reverse = WITH_REVERSE_ATTRIBUTE)
    Coordinates coordinatesWithReverse;
    @Mapped(type = Type.DENSE, reverse = ONLY_REVERSE_ATTRIBUTE)
    Coordinates coordinatesWithReverseOnly;
    int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Coordinates getCoordinatesWithReverse() {
        return coordinatesWithReverse;
    }

    public void setCoordinatesWithReverse(Coordinates coordinatesWithReverse) {
        this.coordinatesWithReverse = coordinatesWithReverse;
    }

    public Coordinates getCoordinatesWithReverseOnly() {
        return coordinatesWithReverseOnly;
    }

    public void setCoordinatesWithReverseOnly(Coordinates coordinatesWithReverseOnly) {
        this.coordinatesWithReverseOnly = coordinatesWithReverseOnly;
    }
}
