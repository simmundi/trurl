package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

import java.nio.charset.StandardCharsets;

final class StringWrapper extends AbstractColumnWrapper<BytesColumnVector, StringAttribute> {

    private String firstValue;

    public StringWrapper(StringAttribute attribute) {
        super(attribute);
    }

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createString();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        String currentValue = attribute.getString(attributeRow);
        byte[] bytes = currentValue.getBytes(StandardCharsets.UTF_8);
        columnVector.setRef(vectorIndex, bytes, 0, bytes.length);
        if (currentValue != firstValue) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        this.firstValue = attribute.isEmpty(attributeRow) ? "" : attribute.getString(attributeRow);
    }


    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                attribute.setString(targetRow, new String(
                        columnVector.vector[i],
                        columnVector.start[i],
                        columnVector.length[i],
                        StandardCharsets.UTF_8));
            }
        }
    }
}
