package pl.edu.icm.trurl.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.exampledata.Looks;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityTest {

    public static final int ID = 123;

    Looks looks = new Looks();

    @Mock
    Mapper mapper;

    @Mock
    MapperSet mapperSet;

    @Mock
    Session session;

    @Mock
    Entity entity;

    @BeforeEach
    void before() {
        lenient().when(mapperSet.classToMapper(Looks.class)).thenReturn(mapper);
        lenient().when(mapperSet.indexToMapper(0)).thenReturn(mapper);
        lenient().when(mapperSet.classToIndex(Looks.class)).thenReturn(0);
        when(mapperSet.componentCount()).thenReturn(1);
        entity = new Entity(mapperSet, session, ID);
    }

    @Test
    void get() {
        // when
        when(mapper.isPresent(ID)).thenReturn(true);
        when(mapper.createAndLoad(session, ID)).thenReturn(looks);

        // execute
        Looks component = entity.get(Looks.class);

        // assert
        assertThat(component).isSameAs(looks);
    }

    @Test
    void get__not_present() {
        // when
        when(mapper.isPresent(ID)).thenReturn(false);

        // execute
        Looks component = entity.get(Looks.class);

        // assert
        verify(mapper, never()).load(any(), any(), anyInt());
        assertThat(component).isNull();
    }

    @Test
    void add() {
        // when
        Looks newLooks = new Looks();

        // execute
        entity.add(newLooks);

        // assert
        verify(mapperSet).classToIndex(Looks.class);
        assertThat(entity.get(Looks.class)).isSameAs(newLooks);
    }

    @Test
    void persist() {
        // when
        Looks newLooks = new Looks();
        entity.add(newLooks);

        // execute
        entity.persist();

        // assert
        verify(mapper).save(newLooks, ID);
    }

    @Test
    void getId() {
        // execute
        int id = entity.getId();

        // assert
        assertThat(id).isEqualTo(ID);
    }

    @Test
    void getSession() {
        // execute
        Session session = entity.getSession();

        // assert
        assertThat(session).isEqualTo(session);
    }

    @Test
    void optional() {
        // when
        Looks newLooks = new Looks();
        entity.add(newLooks);

        // execute
        Optional<Looks> result = entity.optional(Looks.class);

        // assert
        assertThat(result).contains(newLooks);
    }

    @Test
    void optional__empty() {
        // execute
        Optional<Looks> result = entity.optional(Looks.class);

        // assert
        assertThat(result).isEmpty();
    }
}
