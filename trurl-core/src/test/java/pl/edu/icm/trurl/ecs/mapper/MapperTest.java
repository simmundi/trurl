package pl.edu.icm.trurl.ecs.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.Color;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Texture;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.attribute.EnumAttribute;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapperTest {

    @Mock
    Store store;

    @Mock
    EnumAttribute colorAttribute;

    @Mock
    EnumAttribute textureAttribute;

    @Mock
    MapperListener<Looks> mapperListener;

    Mapper<Looks> mapper;

    @BeforeEach
    void before() {
        mapper = Mappers.create(Looks.class);
        when(store.get("color")).thenReturn(colorAttribute);
        when(store.get("texture")).thenReturn(textureAttribute);
        mapper.configureStore(store);
        mapper.attachStore(store);
    }

    @Test
    void construct() {
        // assert
        verify(store).addEnum("color", Color.class);
        verify(store).addEnum("texture", Texture.class);

        verify(store).get("color");
        verify(store).get("texture");
    }

    @Test
    void load() {
        // given
        Looks looks = new Looks();
        when(colorAttribute.getEnum(4)).thenReturn(Color.BLUE);
        when(textureAttribute.getEnum(4)).thenReturn(Texture.ROUGH);
        mapper.ensureCapacity(100);

        // execute
        mapper.load(null, looks, 4);

        // assert
        assertThat(looks.getColor()).isEqualTo(Color.BLUE);
        assertThat(looks.getTexture()).isEqualTo(Texture.ROUGH);
    }

    @Test
    void load__out_of_bounds() {
        // execute & assert
        assertThatThrownBy(() -> mapper.load(null, null, 1234))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void save() {
        // given
        Looks looks = new Looks(Color.BLUE, Texture.SHINY);
        mapper.getMapperListeners().addSavingListener(mapperListener);

        // execute
        mapper.save(null, looks, 99);

        // assert
        verify(colorAttribute).setEnum(99, Color.BLUE);
        verify(textureAttribute).setEnum(99, Texture.SHINY);
        verify(mapperListener).savingComponent(99, looks);
    }

    @Test
    void setCount() {
        // execute
        mapper.setCount(100);

        // assert
        verify(colorAttribute).ensureCapacity(100);
        verify(colorAttribute).ensureCapacity(100);
    }

    @Test
    void save__no_modification() {
        // given
        mapper.setCount(100);
        Looks looks = new Looks(Color.BLUE, Texture.SHINY);
        when(colorAttribute.getEnum(99)).thenReturn(Color.BLUE);
        when(textureAttribute.getEnum(99)).thenReturn(Texture.SHINY);
        mapper.getMapperListeners().addSavingListener(mapperListener);

        // execute
        mapper.save(null, looks, 99);

        // assert
        verify(mapperListener, times(0)).savingComponent(anyInt(), notNull());
    }

    @Test
    void isPresent() {
        // given
        mapper.ensureCapacity(100);
        IntStream.of(10, 36, 50, 99).forEach(i ->
                lenient().when(colorAttribute.isEmpty(i)).thenReturn(true));
        IntStream.of(10, 37, 50, 98).forEach(i ->
                lenient().when(textureAttribute.isEmpty(i)).thenReturn(true));

        // execute
        Set<Integer> emptyRows = IntStream.range(0, 100)
                .filter(i -> !mapper.isPresent(i))
                .boxed()
                .collect(Collectors.toSet());

        // assert
        assertThat(emptyRows).containsExactlyInAnyOrder(10, 50);
    }

    @Test
    void create() {
        // execute
        Looks looks = mapper.create();

        // assert
        assertThat(looks).isNotNull();
    }
}
