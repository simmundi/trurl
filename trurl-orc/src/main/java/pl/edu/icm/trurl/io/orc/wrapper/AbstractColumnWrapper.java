package pl.edu.icm.trurl.io.orc.wrapper;

import org.apache.hadoop.hive.ql.exec.vector.ColumnVector;
import org.apache.orc.TypeDescription;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import pl.edu.icm.trurl.store.attribute.ByteAttribute;
import pl.edu.icm.trurl.store.attribute.DoubleAttribute;
import pl.edu.icm.trurl.store.attribute.EntityAttribute;
import pl.edu.icm.trurl.store.attribute.EntityListAttribute;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.ShortAttribute;
import pl.edu.icm.trurl.store.attribute.StringAttribute;

/**
 * Base class for classes representing type-specific operations between a single Attribute
 * and a single ColumnVector.
 *
 * @param <T> ColumnVector (e.g. LongColumnVector)
 * @param <A> Attribute (e.g. IntAttribute)
 */
public abstract class AbstractColumnWrapper<T extends ColumnVector, A extends Attribute> {
    public T columnVector;
    public A attribute;

    private String name;

    public AbstractColumnWrapper(A attribute) {
        this.name = attribute.name();
        this.attribute = attribute;
    }

    public final void setColumnVector(T columnVector) {
        this.columnVector = columnVector;
    }

    public final String getName() {
        return name;
    }

    /**
     * Returns an orc-specific descriptor of the type
     */
    public abstract TypeDescription getTypeDescription();

    /**
     * Copies a range of attribute data into the columnVector
     * @param fromRow
     * @param numberOfRows
     */
    public final void writeToColumnVector(int fromRow, int numberOfRows) {
        boolean firstEmpty = attribute.isEmpty(fromRow);
        rememberFirstValue(fromRow);
        columnVector.isRepeating = true;
        for (int vectorIndex = 0; vectorIndex < numberOfRows; vectorIndex++) {
            int attributeRow = fromRow + vectorIndex;
            boolean currentIsEmpty = attribute.isEmpty(attributeRow);
            if (currentIsEmpty != firstEmpty) {
                columnVector.isRepeating = false;
            }
            if (currentIsEmpty) {
                columnVector.isNull[vectorIndex] = true;
                columnVector.noNulls = false;
            } else {
                handleValue(vectorIndex, attributeRow);
            }
        }
    }
    /**
     * Copies all data from the columnVector into some range od the attribute
     * @param fromRow
     * @param numberOfRows
     */
    public abstract void readFromColumnVector(int fromRow, int numberOfRows);


    abstract void handleValue(int vectorIndex, int attributeRow);

    abstract void rememberFirstValue(int attributeRow);

    /**
     * Creates an instance of the correct implementation given an attribute.
     *
     */
    public static <E, T extends Attribute> AbstractColumnWrapper create(T attribute) {
        if (attribute instanceof IntAttribute) {
            return new IntWrapper((IntAttribute) attribute);
        } else if (attribute instanceof FloatAttribute) {
            return new FloatWrapper((FloatAttribute) attribute);
        } else if (attribute instanceof DoubleAttribute) {
            return new DoubleWrapper((DoubleAttribute) attribute);
        } else if (attribute instanceof ShortAttribute) {
            return new ShortWrapper((ShortAttribute) attribute);
        } else if (attribute instanceof BooleanAttribute) {
            return new BooleanWrapper((BooleanAttribute) attribute);
        } else if (attribute instanceof ByteAttribute) {
            return new ByteWrapper((ByteAttribute) attribute);
        } else if (attribute instanceof StringAttribute) {
            return new StringWrapper((StringAttribute) attribute);
        } else if (attribute instanceof EntityListAttribute) {
            return new EntityListWrapper((EntityListAttribute) attribute);
        } else if (attribute instanceof EntityAttribute) {
            return new EntityWrapper((EntityAttribute) attribute);
        } else if (attribute instanceof EnumAttribute) {
            return new EnumWrapper((EnumAttribute<?>) attribute);
        } else {
            throw new IllegalArgumentException("Not supported attribute type: " + attribute);
        }
    }
}
