package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.BytesColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

final class EntityListWrapper extends AbstractColumnWrapper<BytesColumnVector, EntityListAttribute> {

    private byte[] firstValue;

    public EntityListWrapper(EntityListAttribute attribute) {
        super(attribute);
    }
    public ByteBuffer bytes = ByteBuffer.wrap(new byte[1024 * 16 * 4]); // 16K entities should be enough for everybody
    public IntBuffer ids = bytes.asIntBuffer();

    @Override
    public TypeDescription getTypeDescription() {
        return TypeDescription.createBinary();
    }

    @Override
    void handleValue(int vectorIndex, int attributeRow) {
        ids.clear();
        attribute.loadIds(attributeRow, (idx, id) -> ids.put(id));
        byte[] data = new byte[ids.position() * 4];
        bytes.rewind();
        bytes.get(data);
        columnVector.setRef(vectorIndex, data, 0, data.length);
        if (!Arrays.equals(data, firstValue)) {
            columnVector.isRepeating = false;
        }
    }

    @Override
    void rememberFirstValue(int attributeRow) {
        if (attribute.isEmpty(attributeRow)) {
            firstValue = new byte[0];
            return;
        }
        ids.clear();
        attribute.loadIds(attributeRow, (idx, id) -> ids.put(id));
        firstValue = new byte[ids.position() * 4];
        bytes.get(firstValue);
    }

    @Override
    public void readFromColumnVector(int fromRow, int numberOfRows) {
        columnVector.flatten(false, null, numberOfRows);
        for (int i = 0; i < numberOfRows; i++) {
            int targetRow = fromRow + i;
            if (columnVector.isNull[i]) {
                attribute.setEmpty(targetRow);
            } else {
                bytes.clear();
                bytes.put(columnVector.vector[i], columnVector.start[i], columnVector.length[i]);
                ids.rewind();
                int size = bytes.position() / 4;
                ids.limit(size);
                attribute.saveIds(targetRow, size, ids::get);
            }
        }
    }
}
