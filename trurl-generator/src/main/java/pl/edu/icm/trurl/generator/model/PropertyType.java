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

package pl.edu.icm.trurl.generator.model;

import com.squareup.javapoet.ClassName;
import pl.edu.icm.trurl.generator.CommonTypes;

public enum PropertyType {
    STRING_PROP(CommonTypes.STRING_COLUMN),
    INT_PROP(CommonTypes.INT_COLUMN),
    DOUBLE_PROP(CommonTypes.DOUBLE_COLUMN),
    FLOAT_PROP(CommonTypes.FLOAT_COLUMN),
    BYTE_PROP(CommonTypes.BYTE_COLUMN),
    SHORT_PROP(CommonTypes.SHORT_COLUMN),
    BOOLEAN_PROP(CommonTypes.BOOLEAN_COLUMN),
    ENUM_PROP(CommonTypes.CATEGORICAL_STATIC_COLUMN),
    SOFT_ENUM_PROP(CommonTypes.CATEGORICAL_DYNAMIC_COLUMN),
    EMBEDDED_LIST(CommonTypes.MAPPER),
    EMBEDDED_PROP(CommonTypes.MAPPER);

    public final ClassName columnType;

    PropertyType(ClassName columnType) {
        this.columnType = columnType;
    }

}
