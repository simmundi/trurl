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

package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.nio.charset.StandardCharsets;

final class StringWrapper extends AbstractColumnWrapper<BytesColumnVector, StringAttribute> {

    private String firstValue;

    public StringWrapper(StringAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createString();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        String currentValue = attribute.getString(attributeRow);
        byte[] bytes = currentValue.getBytes(StandardCharsets.UTF_8);
        columnVector.setRef(vectorIndex, bytes, 0, bytes.length);
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? "" : attribute.getString(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setString(targetRow, new String(
                        columnVector.vector[i],
                        columnVector.start[i],
                        columnVector.length[i],
                        StandardCharsets.UTF_8));
            }
        }
    }
}
