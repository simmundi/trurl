package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.LongColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;

final class EntityWrapper extends AbstractColumnWrapper<LongColumnVector, EntityAttribute> {

    private int firstValue;

    public EntityWrapper(EntityAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createInt();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        int currentValue = attribute.getId(attributeRow);
        columnVector.vector[vectorIndex] = currentValue;
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? Integer.MIN_VALUE : attribute.getId(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setId(targetRow, (int) columnVector.vector[i]);
            }
        }
    }
}
