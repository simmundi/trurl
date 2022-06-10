package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.DoubleColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;

final class FloatWrapper extends AbstractColumnWrapper<DoubleColumnVector, FloatAttribute> {

    private float firstValue;

    public FloatWrapper(FloatAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createFloat();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        float currentValue = attribute.getFloat(attributeRow);
        columnVector.vector[vectorIndex] = currentValue;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? Float.NaN : attribute.getFloat(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setFloat(targetRow, (float) columnVector.vector[i]);
            }
        }
    }
}
