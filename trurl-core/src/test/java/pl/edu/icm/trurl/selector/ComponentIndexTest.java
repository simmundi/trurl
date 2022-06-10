package pl.edu.icm.trurl.selector;

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
import pl.edu.icm.trurl.exampledata.Stats;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ComponentIndexTest {
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

    StatsIndex statsIndex;

    @BeforeEach
    void before() {
        statsIndex = Mockito.spy(new StatsIndex(engineConfiguration));
    }

    @Test
    void onEngineCreated() {
        // given
        when(engine.getMapperSet()).thenReturn(mapperSet);
        when(mapperSet.classToMapper(Stats.class)).thenReturn(statsMapper);
        when(statsMapper.getMapperListeners()).thenReturn(mapperListeners);

        // execute
        statsIndex.onEngineCreated(engine);

        // assert
        verify(mapperListeners).addSavingListener(statsIndex);
    }



}
