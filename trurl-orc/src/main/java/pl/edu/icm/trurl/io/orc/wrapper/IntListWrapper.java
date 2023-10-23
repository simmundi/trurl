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
import pl.edu.icm.trurl.store.attribute.IntListAttribute;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

final class IntListWrapper extends AbstractColumnWrapper<BytesColumnVector, IntListAttribute> {

    private byte[] firstValue;

    public IntListWrapper(IntListAttribute attribute) {
        super(attribute);
    }
    public ByteBuffer bytes = ByteBuffer.wrap(new byte[1024 * 512 * 4]); // 512K entities should be enough for everybody
    public IntBuffer ids = bytes.asIntBuffer();

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createBinary();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        ids.clear();
        attribute.loadInts(attributeRow, (idx, id) -> ids.put(id));
        byte[] data = new byte[ids.position() * 4];
        bytes.rewind();
        bytes.get(data);
        columnVector.setRef(vectorIndex, data, 0, data.length);
        if (!Arrays.equals(data, firstValue)) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        if (attribute.isEmpty(attributeRow)) {
            firstValue = new byte[0];
            return;
        }
        ids.clear();
        attribute.loadInts(attributeRow, (idx, id) -> ids.put(id));
        firstValue = new byte[ids.position() * 4];
        bytes.get(firstValue);
    }

    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                bytes.clear();
                bytes.put(columnVector.vector[i], columnVector.start[i], columnVector.length[i]);
                ids.rewind();
                int size = bytes.position() / 4;
                ids.limit(size);
                attribute.saveInts(targetRow, size, ids::get);
            }
        }
    }
}
