package pl.edu.icm.trurl.ecs.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.EngineConfiguration;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.ecs.util.ArraySelector;
import pl.edu.icm.trurl.ecs.util.EntitiesSampler;
import pl.edu.icm.trurl.exampledata.*;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.array.ArrayStore;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class EntitiesSamplerTest {

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

    @Spy
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

        Store store = engineConfiguration.getEngine().getStore();
        personMapper.configureAndAttach(store);
        looksMapper.configureAndAttach(store);
        somePoiMapper.configureAndAttach(store);

        personMapper.save(person1, 1);
        personMapper.save(person2, 2);
        personMapper.save(person3, 3);
        personMapper.save(person4, 4);

        looksMapper.save(looks1, 0);
        looksMapper.save(looks2, 1);

        somePoiMapper.save(poi, 0);
    }

    @Test
    void copySelected() {
        EntitiesSampler sampler = new EntitiesSampler(engineConfiguration);
        ArraySelector selector = new ArraySelector();
        selector.addAll(new int[]{0, 1, 4});

        personMapper.configureAndAttach(newStore);
        looksMapper.configureAndAttach(newStore);

        sampler.copySelected(selector, newStore);

        assertThat(newStore.getCount()).isEqualTo(3);
        assertThat(newStore.attributes().count()).isEqualTo(4);
    }
}