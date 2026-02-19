package pl.edu.icm.trurl.io.tiled;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.io.tiled.model.TileMetadata;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TsxServiceTest {

    @Mock
    XmlLoader xmlLoader;

    @Mock
    RepresentationEntitiesCreator representationEntitiesCreator;

    @Captor
    ArgumentCaptor<TileMetadata> tileMetadataArgumentCaptor;

    @Test
    void test() throws XMLStreamException, IOException {
        // given
//        when(xmlLoader.load(anyString())).thenReturn(XMLInputFactory.newFactory().createXMLEventReader(new FileReader("examples/basictiles.tsx")));
//        TsxService tsxService = new TsxService(xmlLoader, representationEntitiesCreator, engineBuilder);
//        Mockito.when(representationEntitiesCreator.createOrGetRepresentation(tileMetadataArgumentCaptor.capture())).thenReturn(null);

        // execute
//        tsxService.getTileset("examples/basictiles.tsx");

        // assert
//        assertThat(tileMetadataArgumentCaptor.getAllValues()).hasSize(120);
//        assertThat(tileMetadataArgumentCaptor.getValue().getImagePath()).isEqualTo("basictiles.png");
//        assertThat(tileMetadataArgumentCaptor.getValue().getLocalId()).isEqualTo(119);
    }
}
