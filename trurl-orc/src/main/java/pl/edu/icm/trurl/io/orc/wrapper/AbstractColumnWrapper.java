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

import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.*;

/**
 * Base class for classes representing type-specific operations between a single Attribute
 * and a single ColumnVector.
 *
 * @param <T> ColumnVector (e.g. LongColumnVector)
 * @param <A> Attribute (e.g. IntAttribute)
 */
public abstract class AbstractColumnWrapper<T extends ColumnVector, A extends Attribute> {
    public T columnVector;
    public A attribute;

    private String name;

    public AbstractColumnWrapper(A attribute) {
        this.name = attribute.name();
        this.attribute = attribute;
    }

    public final void setColumnVector(T columnVector) {
        this.columnVector = columnVector;
    }

    public final String getName() {
        return name;
    }

    /**
     * Returns an orc-specific descriptor of the type
     */
    public abstract TypeDescription getTypeDescription();

    /**
     * Copies a range of attribute data into the columnVector
     * @param fromRow
     * @param numberOfRows
     */
    public final void writeToColumnVector(int fromRow, int numberOfRows) {
        boolean firstEmpty = attribute.isEmpty(fromRow);
        rememberFirstValue(fromRow);
        columnVector.isRepeating = true;
        for (int vectorIndex = 0; vectorIndex < numberOfRows; vectorIndex++) {
            int attributeRow = fromRow + vectorIndex;
            boolean currentIsEmpty = attribute.isEmpty(attributeRow);
            if (currentIsEmpty != firstEmpty) {
                columnVector.isRepeating = false;
            }
            if (currentIsEmpty) {
                columnVector.isNull[vectorIndex] = true;
                columnVector.noNulls = false;
            } else {
                handleValue(vectorIndex, attributeRow);
            }
        }
    }
    /**
     * Copies all data from the columnVector into some range od the attribute
     * @param fromRow
     * @param numberOfRows
     */
    public abstract void readFromColumnVector(int fromRow, int numberOfRows);


    abstract void handleValue(int vectorIndex, int attributeRow);

    abstract void rememberFirstValue(int attributeRow);

    /**
     * Creates an instance of the correct implementation given an attribute.
     *
     */
    public static <E, T extends Attribute> AbstractColumnWrapper create(T attribute) {
        if (attribute instanceof IntAttribute) {
            return new IntWrapper((IntAttribute) attribute);
        } else if (attribute instanceof FloatAttribute) {
            return new FloatWrapper((FloatAttribute) attribute);
        } else if (attribute instanceof DoubleAttribute) {
            return new DoubleWrapper((DoubleAttribute) attribute);
        } else if (attribute instanceof ShortAttribute) {
            return new ShortWrapper((ShortAttribute) attribute);
        } else if (attribute instanceof BooleanAttribute) {
            return new BooleanWrapper((BooleanAttribute) attribute);
        } else if (attribute instanceof ByteAttribute) {
            return new ByteWrapper((ByteAttribute) attribute);
        } else if (attribute instanceof StringAttribute) {
            return new StringWrapper((StringAttribute) attribute);
        } else if (attribute instanceof IntListAttribute) {
            return new IntListWrapper((IntListAttribute) attribute);
        } else if (attribute instanceof CategoricalStaticAttribute) {
            return new EnumWrapper((CategoricalStaticAttribute<?>) attribute);
        } else if (attribute instanceof CategoricalDynamicAttribute) {
            return new SoftEnumWrapper((CategoricalDynamicAttribute<?>) attribute);
        } else {
            throw new IllegalArgumentException("Not supported attribute type: " + attribute);
        }
    }
}
