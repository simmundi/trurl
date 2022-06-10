package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.DoubleColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.DoubleAttribute;

final class DoubleWrapper extends AbstractColumnWrapper<DoubleColumnVector, DoubleAttribute> {

    private double firstValue;

    public DoubleWrapper(DoubleAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createDouble();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        double currentValue = attribute.getDouble(attributeRow);
        columnVector.vector[vectorIndex] = currentValue;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? Double.NaN : attribute.getDouble(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setDouble(targetRow, columnVector.vector[i]);
            }
        }
    }
}
