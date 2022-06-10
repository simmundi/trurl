package pl.edu.icm.trurl.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.exampledata.Stats;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SessionTest {

    public static final int ID = 123;

    @Mock
    Engine engine;

    @Mock
    Mapper statsMapper;

    @Mock
    ComponentAccessor componentAccessor;

    @Mock
    MapperSet mapperSet;

    @BeforeEach
    void before() {
        lenient().when(engine.getMapperSet()).thenReturn(mapperSet);
        lenient().when(mapperSet.indexToMapper(0)).thenReturn(statsMapper);
        lenient().when(mapperSet.componentCount()).thenReturn(1);
    }

    @Test
    @DisplayName("Should create attached entities in NORMAL mode")
    void getEntity() {
        // given
        Session session = new Session(engine, 10, Session.Mode.NORMAL);

        // execute
        Entity entity1 = session.getEntity(ID);
        Entity entity2 = session.getEntity(ID);
        Entity entity3 = session.getEntity(ID + 1);

        // assert
        assertThat(entity1).isSameAs(entity2);
        assertThat(entity1).isNotSameAs(entity3);
        verify(mapperSet, times(2)).componentCount();
    }

    @Test
    @DisplayName("Should create detached entities in DETACHED mode")
    void getEntity__detached() {
        // given
        Session session = new Session(engine, 10, Session.Mode.DETACHED_ENTITIES);
        // execute
        Entity entity1 = session.getEntity(ID);
        Entity entity2 = session.getEntity(ID);

        // assert
        assertThat(entity1).isNotSameAs(entity2);
        verify(mapperSet, times(2)).componentCount();
    }

    @Test
    @DisplayName("Should create stub entities in STUB mode")
    void getEntity__stubs() {
        // given
        Session session = new Session(engine, 10, Session.Mode.STUB_ENTITIES);
        // execute
        Entity entity1 = session.getEntity(ID);
        Entity entity2 = session.getEntity(ID);

        // assert
        assertThat(entity1).isNotSameAs(entity2);
        verify(mapperSet, never()).componentCount();
    }

    @Test
    @DisplayName("Should persist entities using mapper")
    void persist() {
        // given
        Session session = new Session(engine, 10, Session.Mode.NORMAL);
        Entity entity1 = session.getEntity(ID);
        Entity entity2 = session.getEntity(ID + 1);
        entity1.add(new Stats());
        entity2.add(new Stats());

        // execute
        session.close();

        // assert
        verify(statsMapper, times(2)).save(any(), anyInt());
    }

    @Test
    @DisplayName("Should return the engine")
    void getEngine() {
        // given
        Session session = new Session(engine, 10, Session.Mode.NORMAL);

        // execute
        Engine engine = session.getEngine();

        // assert
        assertThat(engine).isSameAs(this.engine);
    }
}
