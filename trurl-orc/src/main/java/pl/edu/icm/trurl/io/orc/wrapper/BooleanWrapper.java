package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.BooleanAttribute;

final class BooleanWrapper extends AbstractColumnWrapper<LongColumnVector, BooleanAttribute> {

    private boolean firstValue;

    public BooleanWrapper(BooleanAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createByte();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        boolean currentValue = attribute.getBoolean(attributeRow);
        columnVector.vector[vectorIndex] = attribute.getBoolean(attributeRow) ? 1 : 0;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? false : attribute.getBoolean(attributeRow);
    }

    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setBoolean(targetRow, columnVector.vector[i] == 1);
            }
        }
    }
}
