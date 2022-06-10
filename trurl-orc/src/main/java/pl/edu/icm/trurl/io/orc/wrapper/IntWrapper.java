package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

final class IntWrapper extends AbstractColumnWrapper<LongColumnVector, IntAttribute> {

    private int firstValue;

    public IntWrapper(IntAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createInt();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        int currentValue = attribute.getInt(attributeRow);
        columnVector.vector[vectorIndex] = currentValue;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? Integer.MIN_VALUE : attribute.getInt(attributeRow);
    }

    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setInt(targetRow, (int) columnVector.vector[i]);
            }
        }
    }
}
