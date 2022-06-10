package pl.edu.icm.trurl.visnow;

import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.BooleanAttribute;
import pl.edu.icm.trurl.store.attribute.DoubleAttribute;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;
import pl.edu.icm.trurl.store.attribute.FloatAttribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;
import pl.edu.icm.trurl.store.attribute.ShortAttribute;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

public interface ColumnWrapper {

    void writeData(DataOutput dataOutput, int row) throws IOException;

    String headerDefinition();

    String name();

    static ColumnWrapper from(Attribute attribute) {
        if (attribute instanceof IntAttribute) {
            return new ColumnWrapper() {
                @Override
                public void writeData(DataOutput dataOutput, int row) throws IOException {
                    int datum = ((IntAttribute) attribute).getInt(row);
                    dataOutput.writeInt(datum);
                }

                @Override
                public String headerDefinition() {
                    return String.format("component %s int", attribute.name());
                }

                @Override
                public String name() {
                    return attribute.name();
                }
            };
        } else if (attribute instanceof FloatAttribute) {
            return new ColumnWrapper() {
                @Override
                public void writeData(DataOutput dataOutput, int row) throws IOException {
                    float datum = ((FloatAttribute) attribute).getFloat(row);
                    dataOutput.writeFloat(datum);
                }

                @Override
                public String headerDefinition() {
                    return String.format("component %s float", attribute.name());
                }

                @Override
                public String name() {
                    return attribute.name();
                }
            };
        } else if (attribute instanceof DoubleAttribute) {
            return new ColumnWrapper() {
                @Override
                public void writeData(DataOutput dataOutput, int row) throws IOException {
                    double datum = ((DoubleAttribute) attribute).getDouble(0);
                    dataOutput.writeDouble(datum);
                }

                @Override
                public String headerDefinition() {
                    return String.format("component %s double", attribute.name());
                }

                @Override
                public String name() {
                    return attribute.name();
                }
            };
        } else if (attribute instanceof ShortAttribute) {
            return new ColumnWrapper() {
                @Override
                public void writeData(DataOutput dataOutput, int row) throws IOException {
                    short datum = ((ShortAttribute) attribute).getShort(row);
                    dataOutput.writeShort(datum);
                }

                @Override
                public String headerDefinition() {
                    return String.format("component %s short", attribute.name());
                }

                @Override
                public String name() {
                    return attribute.name();
                }
            };
        } else if (attribute instanceof BooleanAttribute) {
            return new ColumnWrapper() {
                @Override
                public void writeData(DataOutput dataOutput, int row) throws IOException {
                    boolean datum = ((BooleanAttribute) attribute).getBoolean(row);
                    dataOutput.writeByte(datum ? 1 : 0);
                }

                @Override
                public String headerDefinition() {
                    return String.format("component %s byte", attribute.name());
                }

                @Override
                public String name() {
                    return attribute.name();
                }
            };
        } else if (attribute instanceof EnumAttribute) {
            return new ColumnWrapper() {
                @Override
                public void writeData(DataOutput dataOutput, int row) throws IOException {
                    Enum datum = ((EnumAttribute) attribute).getEnum(row);
                    dataOutput.writeByte(datum == null ? Byte.MIN_VALUE : datum.ordinal());
                }

                @Override
                public String headerDefinition() {
                    return
                            String.format("component %s byte, user:\"map\";", attribute.name())
                            + Arrays.stream(((EnumAttribute<?>) attribute).values())
                                    .map(e -> "\"" + e.ordinal() + ": " + e + "\"")
                                    .collect(Collectors.joining(";"));
                }

                @Override
                public String name() {
                    return attribute.name();
                }
            };
        }
        throw new IllegalArgumentException("Not supported attribute type: " + attribute);
    }
}
