package pl.edu.icm.trurl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.store.array.IntArrayAttribute;
import pl.edu.icm.trurl.store.array.StringArrayAttribute;
import pl.edu.icm.trurl.store.attribute.Attribute;
import pl.edu.icm.trurl.store.attribute.IntAttribute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqlExporterTest {

    @Mock
    private Statement statement;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseConnectionService connectionService;

    @Mock
    private Mapper mapper;

    @Mock
    private List<Attribute> attributes;

    @Mock
    private PreparedStatement preparedStatement;

    @BeforeEach
    private void before() throws SQLException {
        when(connectionService.getConnection())
                .thenReturn(connection);
        when(connection.createStatement())
                .thenReturn(statement);
        attributes = Arrays.asList(
                new IntArrayAttribute("calkowite", 10),
                new StringArrayAttribute("napisy", 10));

        List<String> listOfStrings = Arrays.asList("napisy", "filip", "dreger", "kot");

        for (int i = 0; i < 3; i++) {
            ((IntAttribute) attributes.get(0)).setInt(i, 12 + i);
            attributes.get(1).setString(i, listOfStrings.get(i));
        }
        when(mapper.attributes()).thenReturn(attributes);
        when(connection.prepareStatement("insert into tabelka(id, calkowite, napisy) values (?, ?, ?)"))
                .thenReturn(preparedStatement);
    }

    @Test
    @DisplayName("Should execute statements per row")
    void export__iterate_over_rows() throws SQLException {
        // given
        when(mapper.getCount()).thenReturn(3);
        when(mapper.isPresent(0)).thenReturn(false);
        when(mapper.isPresent(1)).thenReturn(true);
        when(mapper.isPresent(2)).thenReturn(true);
        SqlExporter sqlExporter = new SqlExporter(connectionService, 1);

        // execute
        sqlExporter.export("tabelka", mapper);

        // assert
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 13);
        verify(preparedStatement).setString(3, "dreger");
        verify(preparedStatement, times(2)).addBatch();
        verify(preparedStatement, times(3)).executeBatch();
        verify(connection, times(4)).commit();
    }

    @Test
    @DisplayName("Should create 'create table...' and 'insert into' statements")
    void export__create_sql() throws SQLException {

        // given
        when(mapper.getCount()).thenReturn(0);
        SqlExporter sqlExporter = new SqlExporter(connectionService, 100);

        // execute
        sqlExporter.export("tabelka", mapper);

        // assert
        verify(statement).execute("create table if not exists tabelka (\n" +
                "    id int primary key,\n" +
                "    calkowite int,\n" +
                "    napisy text\n" +
                ")");
        verify(preparedStatement, times(1)).executeBatch();
        verify(connection, times(2)).commit();
    }


}
