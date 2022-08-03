package pl.edu.icm.trurl.visnow;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.snowyhollows.bento.config.WorkDir;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.Color;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VnPointsExporterTest {

    @Mock
    WorkDir workDir;

    VnPointsExporter<Mushroom> vnPointsExporter;


    @Test
    @DisplayName("Should export data")
    void append() throws IOException {

        // given
        ByteArrayOutputStream vnd = new ByteArrayOutputStream();
        ByteArrayOutputStream vnf = new ByteArrayOutputStream();
        Mockito.when(workDir.openForWriting(new File("mushrooms.vnd"))).thenReturn(vnd);
        Mockito.when(workDir.openForWriting(new File("mushrooms.vnf"))).thenReturn(vnf);
        VnPointsExporter exporter = VnPointsExporter.create(
                Mushroom.class,
                workDir,
                "mushrooms");

        // execute
        exporter.append(new Mushroom(1, 2, 0.3, Color.SILVER, (short)4));
        exporter.append(new Mushroom(5, 8, 0.5, Color.BLUE, (short)6));
        exporter.append(new Mushroom(6, 9, 0.8, Color.GOLD, (short)123));
        exporter.close();

        // assert
        ByteArrayDataInput data = ByteStreams.newDataInput(vnd.toByteArray());
        assertThat(data.readFloat()).isEqualTo(1);
        assertThat(data.readFloat()).isEqualTo(2);
        assertThat(data.readFloat()).isEqualTo(0);
        assertThat(data.readDouble()).isEqualTo(0.3);
        assertThat(data.readByte()).isEqualTo((byte)Color.SILVER.ordinal());
        assertThat(data.readShort()).isEqualTo((short)4);
        assertThat(vnd.size()).isEqualTo(23 * 3);

        assertThat(Arrays.stream(vnf.toString().split("\\R"))).containsExactly(
                "#VisNow point field",
                "field \"mushrooms\", nnodes = 3",
                "component height double",
                "component color byte, user:\"map\";\"0: GOLD\";\"1: SILVER\";\"2: BLUE\"",
                "component diameter short",
                "file \"mushrooms.vnd\" binary",
                "coord.0,coord.1,coord.2,height,color,diameter"
        );
    }
}
