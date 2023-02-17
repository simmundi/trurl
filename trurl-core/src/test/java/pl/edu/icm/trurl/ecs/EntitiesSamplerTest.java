package pl.edu.icm.trurl.ecs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.MapperSet;
import pl.edu.icm.trurl.ecs.Session;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.ecs.selector.Chunk;
import pl.edu.icm.trurl.ecs.selector.ChunkInfo;
import pl.edu.icm.trurl.ecs.selector.Selector;
import pl.edu.icm.trurl.ecs.util.*;
import pl.edu.icm.trurl.exampledata.*;
import pl.edu.icm.trurl.exampledata.pizza.Olive;
import pl.edu.icm.trurl.exampledata.pizza.Pizza;
import pl.edu.icm.trurl.exampledata.pizza.PizzaMapper;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitiesSamplerTest {

    Store store = new ArrayStore(20);
    Store newStore = new ArrayStore(20);

    Person person1 = new Person();
    Person person2 = new Person();
    Person person3 = new Person();
    Person person4 = new Person();
    Looks looks1 = new Looks();
    Looks looks2 = new Looks();
    SomePoi poi = new SomePoi();

    Mapper<Person> personMapper;
    Mapper<Looks> looksMapper;
    Mapper<SomePoi> somePoiMapper;

    @Mock
    EngineConfiguration engineConfiguration;

    @BeforeEach
    void setUp() {
        person1.setName("A");
        person2.setName("B");
        person3.setName("C");
        person4.setName("D");

        looks1.setColor(Color.BLUE);
        looks1.setTexture(Texture.SHINY);
        looks2.setColor(Color.SILVER);
        looks2.setTexture(Texture.ROUGH);

        personMapper = Mappers.create(Person.class);
        looksMapper = Mappers.create(Looks.class);
        somePoiMapper = Mappers.create(SomePoi.class);

        personMapper.configureAndAttach(store);
        looksMapper.configureAndAttach(store);
        somePoiMapper.configureAndAttach(store);

        when(engineConfiguration.getEngine()).thenReturn(new Engine(store, 5,
                new MapperSet(new DynamicComponentAccessor(Person.class, Looks.class, SomePoi.class)), false));
    }

    @Test
    void copySelected() {

        personMapper.save(person1, 1);
        personMapper.save(person2, 2);
        personMapper.save(person3, 3);
        personMapper.save(person4, 4);

        looksMapper.save(looks1, 0);
        looksMapper.save(looks2, 1);

        somePoiMapper.save(poi, 0);

        //execute
        EntitiesSampler sampler = new EntitiesSampler(engineConfiguration);
        ArraySelector selector = new ArraySelector();
        selector.addAll(new int[]{0, 1, 4});

        personMapper.configureAndAttach(newStore);
        looksMapper.configureAndAttach(newStore);

        sampler.copySelected(selector, newStore);

        assertThat(newStore.getCount()).isEqualTo(2);
    }
}