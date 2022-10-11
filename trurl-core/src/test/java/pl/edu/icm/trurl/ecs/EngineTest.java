package pl.edu.icm.trurl.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.MapperListeners;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EngineTest {

    public static final int INITIAL_CAPACITY = 100;
    public static final int CAPACITY_HEADROOM = 50;
    @Mock
    Store store;

    @Mock
    StoreFactory storeFactory;

    @Mock
    MapperSet mapperSet;

    @Mock
    Session session;

    @Mock
    SessionFactory sessionFactory;

    @Mock
    EntitySystem system;

    @Mock
    Mapper mapperA;

    @Mock
    Mapper mapperB;

    @Mock
    MapperListeners mapperListenersA;

    @Mock
    MapperListeners mapperListenersB;

    @BeforeEach
    void before() {
        lenient().when(mapperA.getCount()).thenReturn(300);
        lenient().when(mapperSet.streamMappers()).thenAnswer(params -> Stream.of(
                mapperA,
                mapperB
        ));
        lenient().when(storeFactory.create(anyInt())).thenReturn(store);
        lenient().when(sessionFactory.create()).thenReturn(session);
    }

    @Test
    void construct() {
        // execute
        new Engine(storeFactory, INITIAL_CAPACITY, CAPACITY_HEADROOM, mapperSet, false);

        // assert
        verify(mapperA).configureStore(store);
        verify(mapperA).attachStore(store);
        verify(mapperB).configureStore(store);
        verify(mapperB).attachStore(store);
    }

    @Test
    void execute() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        engine.execute(system);

        // assert
        verify(system, times(1)).execute(any());
    }

    @Test
    void streamDetached() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        List<Integer> result = engine.streamDetached().map(Entity::getId).collect(Collectors.toList());

        // assertThat
        assertThat(result).isEqualTo(IntStream.range(0, 300).boxed().collect(Collectors.toList()));
    }

    @Test
    void getComponentStore() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        Store result = engine.getStore();

        // assert
        assertThat(result).isSameAs(store);
    }

    @Test
    void getMapperSet() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        MapperSet result = engine.getMapperSet();

        // assert
        assertThat(result).isSameAs(mapperSet);
    }

    @Test
    void getCount() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        int count = engine.getCount();

        // assert
        assertThat(count).isEqualTo(300);
    }

    @Test
    void nextId() {
        // given
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);

        // execute
        int nextId = engine.nextId();
        int nextNextId = engine.nextId();

        // assert
        assertThat(nextId).isEqualTo(300);
        assertThat(nextNextId).isEqualTo(301);
    }

    @Test
    void onUnderlyingDataChanged() {
        Object component = new Object();
        Engine engine = new Engine(store, CAPACITY_HEADROOM, mapperSet, false);
        when(mapperA.isPresent(anyInt())).thenAnswer(params -> params.getArgument(0, Integer.class) % 2 == 0);
        when(mapperA.getMapperListeners()).thenReturn(mapperListenersA);
        when(mapperB.getMapperListeners()).thenReturn(mapperListenersB);
        when(mapperListenersB.isEmpty()).thenReturn(true);
        when(mapperA.create()).thenReturn(component);

        // execute
        engine.onUnderlyingDataChanged(0, INITIAL_CAPACITY);

        verify(mapperA).create();
        verify(mapperA, times(CAPACITY_HEADROOM)).load(any(), any(), anyInt());
        verify(mapperListenersA, times(CAPACITY_HEADROOM)).fireSavingComponent(eq(component), anyInt());
        verify(mapperB, never()).create();
        verify(mapperListenersB, never()).fireSavingComponent(any(), anyInt());
    }
}
