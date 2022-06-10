package pl.edu.icm.trurl.ecs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.util.DynamicComponentAccessor;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Stats;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MapperSetTest {

    @Spy
    ComponentAccessor componentAccessor = new DynamicComponentAccessor(Looks.class, Stats.class);

    @InjectMocks
    MapperSet mapperSet;

    @Test
    void classToMapper() {
        // execute
        Looks looks = mapperSet.classToMapper(Looks.class).create();
        Stats stats = mapperSet.classToMapper(Stats.class).create();

        // assert
        assertThat(looks).isExactlyInstanceOf(Looks.class);
        assertThat(stats).isExactlyInstanceOf(Stats.class);
    }

    @Test
    void indexToMapper() {
        // execute
        Object stats = mapperSet.indexToMapper(1).create();
        Object looks = mapperSet.indexToMapper(0).create();

        // assert
        assertThat(looks).isExactlyInstanceOf(Looks.class);
        assertThat(stats).isExactlyInstanceOf(Stats.class);
    }

    @Test
    void classToIndex() {
        // execute
        int statsIndex = mapperSet.classToIndex(Stats.class);
        int looksIndex = mapperSet.classToIndex(Looks.class);

        // assert
        assertThat(looksIndex).isEqualTo(0);
        assertThat(statsIndex).isEqualTo(1);
    }

    @Test
    void componentCount() {
        // execute
        int count = mapperSet.componentCount();

        // assert
        assertThat(count).isEqualTo(2);
    }

    @Test
    void streamMappers() {
        // execute
        Stream<String> results = mapperSet.streamMappers().map(Mapper::name);

        // assert
        assertThat(results).containsExactly("looks", "stats");
    }
}
