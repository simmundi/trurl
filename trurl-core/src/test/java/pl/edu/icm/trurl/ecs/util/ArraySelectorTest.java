package pl.edu.icm.trurl.ecs.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.selector.Chunk;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class ArraySelectorTest {

    @Test
    @DisplayName("Temporary test")
    void chunks() {
        ArraySelector arraySelector = new ArraySelector(2048, 1024);
        int[] ids = IntStream.range(0, 2047).toArray();
        arraySelector.addAll(ids);
        List<Chunk> chunkList = arraySelector.chunks().collect(Collectors.toList());
        assertThat(chunkList.get(0).ids().count()).isEqualTo(1024);
        assertThat(chunkList.get(1).ids().count()).isEqualTo(1023);

    }
}