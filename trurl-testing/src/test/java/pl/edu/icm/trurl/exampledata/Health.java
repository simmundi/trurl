/*
 * Copyright (c) 2023 ICM Epidemiological Model Team at floaterdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
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

import pl.edu.icm.trurl.ecs.dao.annotation.CategoryManagedBy;
import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

@WithDao
public class Health {
    private float howMuch;

    public Health(float howMuch, ContaminationType contaminationType) {
        this.howMuch = howMuch;
        this.contaminationType = contaminationType;
    }

    public Health() {
    }

    @CategoryManagedBy( value = ContaminationTypes.class )
    private ContaminationType contaminationType;

    public float getHowMuch() {
        return howMuch;
    }

    public void setHowMuch(float howMuch) {
        this.howMuch = howMuch;
    }

    public ContaminationType getContaminationType() {
        return contaminationType;
    }

    public void setContaminationType(ContaminationType contaminationType) {
        this.contaminationType = contaminationType;
    }
}
