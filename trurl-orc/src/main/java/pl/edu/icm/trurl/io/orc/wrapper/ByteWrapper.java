package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;

public final class ByteWrapper extends AbstractColumnWrapper<LongColumnVector, ByteAttribute> {

    private byte firstValue;

    public ByteWrapper(ByteAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createByte();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        byte currentValue = attribute.getByte(attributeRow);
        columnVector.vector[vectorIndex] = currentValue;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? Byte.MIN_VALUE : attribute.getByte(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        if (columnVector == null) {
            for (int i = 0; i < numberOfRows; i++) {
                int targetRow = fromRow + i;
                attribute.setEmpty(targetRow);
            }
        } else {
            columnVector.flatten(false, null, numberOfRows);
            for (int i = 0; i < numberOfRows; i++) {
                int targetRow = fromRow + i;
                if (columnVector.isNull[i]) {
                    attribute.setEmpty(targetRow);
                } else {
                    attribute.setByte(targetRow, (byte) columnVector.vector[i]);
                }
            }
        }
    }
}
