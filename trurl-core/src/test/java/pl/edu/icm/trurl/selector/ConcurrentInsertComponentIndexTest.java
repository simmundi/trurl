package pl.edu.icm.trurl.selector;

import org.assertj.core.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.MapperListeners;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.exampledata.Stats;

import java.util.stream.IntStream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcurrentInsertComponentIndexTest {
    @Mock
    Engine engine;

    @Mock
    MapperSet mapperSet;

    @Mock
    Mapper<Stats> statsMapper;

    @Mock
    MapperListeners<Stats> mapperListeners;

    @Mock
    EngineConfiguration engineConfiguration;

    StatsConcurrentInsertIndex statsBitmapIndex;

    @BeforeEach
    void before() {
        statsBitmapIndex = Mockito.spy(new StatsConcurrentInsertIndex(engineConfiguration));
    }

    @Test
    void onEngineCreated() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);

        // execute
        statsBitmapIndex.onEngineCreated(engine);

        // assert
        verify(mapperListeners).addSavingListener(statsBitmapIndex);
    }

    @Test
    void contains() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);
        statsBitmapIndex.onEngineCreated(engine);
        statsBitmapIndex.savingComponent(88888, new Stats());
        statsBitmapIndex.savingComponent(888, new Stats());

        // execute
        boolean yes1 = statsBitmapIndex.contains(88888);
        boolean yes2 = statsBitmapIndex.contains(888);
        boolean no = statsBitmapIndex.contains(666);

        // assert
        Assertions.assertThat(yes1).isTrue();
        Assertions.assertThat(yes2).isTrue();
        Assertions.assertThat(no).isFalse();
    }

    @Test
    void ids() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);
        statsBitmapIndex.onEngineCreated(engine);
        statsBitmapIndex.savingComponent(88888, new Stats());
        statsBitmapIndex.savingComponent(888, new Stats());

        // execute
        IntStream results = statsBitmapIndex.chunks().flatMapToInt(Chunk::ids);

        // assert
        Assertions.assertThat(results).containsExactlyInAnyOrder(888, 88888);
    }

}
