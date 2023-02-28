package pl.edu.icm.trurl.sql;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.store.array.ByteArrayAttribute;
import pl.edu.icm.trurl.store.attribute.*;
import pl.edu.icm.trurl.util.Status;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class SqlExporter {

    private final DatabaseConnectionService databaseConnectionService;
    private final int batchSize;

    public SqlExporter(DatabaseConnectionService databaseConnectionService, int batchSize) {
        this.databaseConnectionService = databaseConnectionService;
        this.batchSize = batchSize;
    }

    public void export(String name, Mapper<?> mapper) throws SQLException {
        Connection connection = databaseConnectionService.getConnection();

        List<Attribute> attributes = mapper.attributes();
        int count = mapper.getCount();

        if (attributes.stream().anyMatch(a -> !(a instanceof EntityListAttribute))) {
            String ddl = "create table if not exists " + name + " (\n"
                    + "    id int primary key,\n"
                    + attributes.stream()
                    .filter(a -> !(a instanceof EntityListAttribute)).map(attribute -> "    " + sqlType(attribute)).collect(joining(",\n"))
                    + "\n)";

            Statement statement = connection.createStatement();
            statement.execute(ddl);
            connection.commit();

            String dml = "insert into " + name + "(id, "
                    + attributes.stream()
                    .filter(a -> !(a instanceof EntityListAttribute)).map(attr -> escape(attr.name())).collect(joining(", "))
                    + ") values (?, "
                    + attributes.stream()
                    .filter(a -> !(a instanceof EntityListAttribute)).map(attribute -> "?").collect(joining(", "))
                    + ")";

            PreparedStatement preparedStatement = connection.prepareStatement(dml);

            int transactionCounter = 0;

            AtomicInteger idCounter = new AtomicInteger();

            Status status = Status.of("inserting " + name, 1000000);
            for (int i = 0; i < count; i++) {
                if (!mapper.isPresent(i)) {
                    continue;
                }

                preparedStatement.setInt(1, i);
                status.tick();

                idCounter.set(2);


                for (Attribute attribute : attributes.stream()
                        .filter(a -> !(a instanceof EntityListAttribute)).collect(toList())) {
                    prepareParam(idCounter.getAndIncrement(), attribute, i, preparedStatement);
                }

                preparedStatement.addBatch();

                if (++transactionCounter >= batchSize) {
                    preparedStatement.executeBatch();
                    transactionCounter = 0;
                    connection.commit();
                }

            }
            preparedStatement.executeBatch();
            connection.commit();
            status.done();
        }
        for (Attribute attribute : attributes.stream()
                .filter(EntityListAttribute.class::isInstance).collect(toList())) {

            String attributeName = escape(attribute.name());

            String tableName = name + "_" + attributeName;

            String ddl = "create table if not exists " + tableName
                    + "( " + name + "_id int, " + attributeName + "_id int, primary key(" + name + "_id, " + attributeName + "_id))";

            Statement statement = connection.createStatement();
            statement.execute(ddl);
            connection.commit();

            String dml = "insert into " + tableName
                    + "( " + name + "_id, " + attributeName + "_id )" + "values (?,?)";

            PreparedStatement preparedStatement = connection.prepareStatement(dml);

            int transactionCounter = 0;

            IntList ints = new IntArrayList();

            Status status = Status.of("inserting " + tableName, 1000000);
            for (int i = 0; i < count; i++) {
                if (!mapper.isPresent(i)) {
                    continue;
                }
                ((EntityListAttribute) attribute).loadIds(i, ints::add);

                for (Integer rowId : ints) {
                    preparedStatement.setInt(1, i);
                    status.tick();
                    preparedStatement.setInt(2, rowId);
                    preparedStatement.addBatch();
                    if (++transactionCounter >= batchSize) {
                        preparedStatement.executeBatch();
                        transactionCounter = 0;
                        connection.commit();
                    }
                }
            }
            preparedStatement.executeBatch();
            connection.commit();
            status.done();
        }
    }

    private void prepareParam(int index, Attribute attribute, int row, PreparedStatement preparedStatement) throws SQLException {
        if (attribute instanceof IntAttribute) {
            preparedStatement.setInt(index, ((IntAttribute) attribute).getInt(row));
        } else if (attribute instanceof FloatAttribute) {
            preparedStatement.setFloat(index, ((FloatAttribute) attribute).getFloat(row));
        } else if (attribute instanceof DoubleAttribute) {
            preparedStatement.setDouble(index, ((DoubleAttribute) attribute).getDouble(row));
        } else if (attribute instanceof ShortAttribute) {
            preparedStatement.setShort(index, ((ShortAttribute) attribute).getShort(row));
        } else if (attribute instanceof ByteAttribute) {
            preparedStatement.setByte(index, ((ByteAttribute) attribute).getByte(row));
        } else if (attribute instanceof BooleanAttribute) {
            preparedStatement.setBoolean(index, ((BooleanAttribute) attribute).getBoolean(row));
        } else if (attribute instanceof StringAttribute) {
            preparedStatement.setString(index, attribute.getString(row));
        } else if (attribute instanceof EntityAttribute) {
            int id = ((EntityAttribute) attribute).getId(row);
            if (id != Entity.NULL_ID) {
                preparedStatement.setInt(index, id);
            } else {
                preparedStatement.setNull(index, Types.INTEGER);
            }
        } else if (attribute instanceof EnumAttribute) {
            preparedStatement.setString(index, attribute.getString(row));
        } else {
            throw new IllegalArgumentException("Not supported column type: " + attribute);
        }
    }


    private String sqlType(Attribute attribute) {
        String attributeName = escape(attribute.name());
        if (attribute instanceof IntAttribute) {
            return attributeName + " int";
        } else if (attribute instanceof FloatAttribute) {
            return attributeName + " float";
        } else if (attribute instanceof DoubleAttribute) {
            return attributeName + " float";
        } else if (attribute instanceof ShortAttribute) {
            return attributeName + " smallint";
        } else if (attribute instanceof BooleanAttribute) {
            return attributeName + " boolean";
        } else if (attribute instanceof StringAttribute) {
            return attributeName + " text";
        } else if (attribute instanceof EntityAttribute) {
            return attributeName + " int";
        } else if (attribute instanceof ByteAttribute) {
            return attributeName + " smallint";
        } else if (attribute instanceof EnumAttribute) {
            return attributeName + " text";
        }
        throw new IllegalArgumentException("Not supported column type: " + attribute);
    }

    private String escape(String name) {
        return name.replaceAll("\\.", "_");
    }
}
