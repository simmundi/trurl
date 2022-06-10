package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.ShortAttribute;

final class ShortWrapper extends AbstractColumnWrapper<LongColumnVector, ShortAttribute> {

    private short firstValue;

    public ShortWrapper(ShortAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createShort();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        short currentValue = attribute.getShort(attributeRow);
        columnVector.vector[vectorIndex] = currentValue;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? Short.MIN_VALUE : attribute.getShort(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setShort(targetRow, (short) columnVector.vector[i]);
            }
        }
    }
}
