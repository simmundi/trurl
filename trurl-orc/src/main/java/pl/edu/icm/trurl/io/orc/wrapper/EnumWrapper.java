package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;

final class EnumWrapper<E extends Enum<E>> extends AbstractColumnWrapper<LongColumnVector, EnumAttribute<E>> {

    private byte firstValue;

    public EnumWrapper(EnumAttribute<E> attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createByte();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        byte currentValue = attribute.getOrdinal(attributeRow);
        columnVector.vector[vectorIndex] = currentValue;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? Byte.MIN_VALUE : attribute.getOrdinal(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        if (columnVector == null) {
            for (int i = 0; i < numberOfRows; i++) {
                int targetRow = fromRow + i;
                attribute.setEmpty(targetRow);
            }
        } else {
            for (int i = 0; i < numberOfRows; i++) {
                int targetRow = fromRow + i;
                if (columnVector.isNull[i]) {
                    attribute.setEmpty(targetRow);
                } else {
                    attribute.setOrdinal(targetRow, (byte) columnVector.vector[i]);
                }
            }
        }
    }
}
