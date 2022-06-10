package pl.edu.icm.trurl.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionService {

    private final Connection connection;

    public DatabaseConnectionService(String jdbcUrl, String jdbcUser, String jdbcPassword) {
        try {
            connection = DriverManager.getConnection(jdbcUrl, jdbcUser, jdbcPassword);
            connection.setAutoCommit(false);
        } catch (SQLException throwables) {
            throw new RuntimeException("Could not open db connection " + jdbcUrl, throwables);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
