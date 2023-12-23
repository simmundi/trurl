/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
/*
package pl.edu.icm.trurl.sql;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import pl.edu.icm.trurl.ecs.DetachedEntity;
import pl.edu.icm.trurl.ecs.dao.Dao;
import pl.edu.icm.trurl.store.attribute.*;
import pl.edu.icm.trurl.store.attribute.CategoricalStaticAttribute;
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

    public void export(String name, Dao<?> dao) throws SQLException {
        Connection connection = databaseConnectionService.getConnection();

        List<Attribute> attributes = dao.attributes();
        int count = dao.getCounter().getCount();

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
                    .filter(a -> !(a instanceof EntityListAttribute)).map(Attribute::name).collect(joining(", "))
                    + ") values (?, "
                    + attributes.stream()
                    .filter(a -> !(a instanceof EntityListAttribute)).map(attribute -> "?").collect(joining(", "))
                    + ")";

            PreparedStatement preparedStatement = connection.prepareStatement(dml);

            int transactionCounter = 0;

            AtomicInteger idCounter = new AtomicInteger();

            Status status = Status.of("inserting " + name, 1000000);
            for (int i = 0; i < count; i++) {
                if (!dao.isPresent(i)) {
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

            String tableName = name + "_" + attribute.name();

            String ddl = "create table if not exists " + tableName
                    + "( " + name + "_id int, " + attribute.name() + "_id int primary key )";

            Statement statement = connection.createStatement();
            statement.execute(ddl);
            connection.commit();

            String dml = "insert into " + tableName
                    + "( " + name + "_id, " + attribute.name() + "_id )" + "values (?,?)";

            PreparedStatement preparedStatement = connection.prepareStatement(dml);

            int transactionCounter = 0;

            IntList ints = new IntArrayList();

            Status status = Status.of("inserting " + tableName, 1000000);
            for (int i = 0; i < count; i++) {
                if (!dao.isPresent(i)) {
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
        } else if (attribute instanceof BooleanAttribute) {
            preparedStatement.setBoolean(index, ((BooleanAttribute) attribute).getBoolean(row));
        } else if (attribute instanceof StringAttribute) {
            preparedStatement.setString(index, attribute.getString(row));
        } else if (attribute instanceof IdentifierAttribute) {
            int id = ((IdentifierAttribute) attribute).getId(row);
            if (id != DetachedEntity.NULL_ID) {
                preparedStatement.setInt(index, id);
            } else {
                preparedStatement.setNull(index, Types.INTEGER);
            }
        } else if (attribute instanceof CategoricalStaticAttribute) {
            preparedStatement.setString(index, attribute.getString(row));
        } else {
            throw new IllegalArgumentException("Not supported column type: " + attribute);
        }
    }


    private String sqlType(Attribute attribute) {
        if (attribute instanceof IntAttribute) {
            return attribute.name() + " int";
        } else if (attribute instanceof FloatAttribute) {
            return attribute.name() + " float";
        } else if (attribute instanceof DoubleAttribute) {
            return attribute.name() + " float";
        } else if (attribute instanceof ShortAttribute) {
            return attribute.name() + " int";
        } else if (attribute instanceof BooleanAttribute) {
            return attribute.name() + " boolean";
        } else if (attribute instanceof StringAttribute) {
            return attribute.name() + " text";
        } else if (attribute instanceof IdentifierAttribute) {
            return attribute.name() + " int";
        } else if (attribute instanceof CategoricalStaticAttribute) {
            return attribute.name() + " text";
        }
        throw new IllegalArgumentException("Not supported column type: " + attribute);
    }
}


 */