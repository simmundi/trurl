package pl.edu.icm.trurl.ecs.mapper;

import org.assertj.core.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.Stats;

@ExtendWith(MockitoExtension.class)
class MapperListenersTest {

    @Mock
    MapperListener<Stats> mapperListener;

    @InjectMocks
    MapperListeners<Stats> mapperListeners;

    @InjectMocks
    MapperListeners<Stats> otherMapperListeners;

    @Test
    void fireSavingListener() {
        // given
        mapperListeners.addSavingListener(mapperListener);
        Stats component = new Stats();

        // execute
        mapperListeners.fireSavingComponent(component, 123);

        // assert
        Mockito.verify(mapperListener, Mockito.times(1)).savingComponent(123, component);
    }

    @Test
    void isEmpty() {
        mapperListeners.addSavingListener(mapperListener);

        // execute
        boolean shouldNotBeEmpty = mapperListeners.isEmpty();
        boolean shouldBeEmpty = otherMapperListeners.isEmpty();

        // assert
        Assertions.assertThat(shouldBeEmpty).isTrue();
        Assertions.assertThat(shouldNotBeEmpty).isFalse();
    }
}
