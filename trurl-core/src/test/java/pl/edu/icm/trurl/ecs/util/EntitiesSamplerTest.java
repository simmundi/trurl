package pl.edu.icm.trurl.ecs.util;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.ecs.*;
import pl.edu.icm.trurl.ecs.mapper.Mapper;
import pl.edu.icm.trurl.ecs.mapper.Mappers;
import pl.edu.icm.trurl.exampledata.*;
import pl.edu.icm.trurl.exampledata.pizza.*;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.StoreFactory;
import pl.edu.icm.trurl.store.array.ArrayStore;
import pl.edu.icm.trurl.store.array.ArrayStoreFactory;

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
    Pizza pizzaA = new Pizza();
    Pizza pizzaB = new Pizza();
    Flat flat = new Flat();

    Mapper<Person> personMapper;
    Mapper<Looks> looksMapper;
    Mapper<SomePoi> somePoiMapper;
    Mapper<Pizza> pizzaMapper;
    Mapper<Flat> flatMapper;

    @Spy
    Bento bento = Bento.createRoot();
    @Spy
    ComponentAccessorCreator componentAccessorCreator = new ComponentAccessorCreatorImpl(bento);
    @Spy
    StoreFactory storeFactory = new ArrayStoreFactory();
    EngineConfiguration engineConfiguration = new EngineConfiguration(componentAccessorCreator,
            storeFactory,
            20,
            10,
            false,
            bento);
    @Mock
    Session session;

    @BeforeEach
    void setUp() {

        Mappers mappers = new Mappers();
        personMapper = mappers.create(Person.class);
        looksMapper = mappers.create(Looks.class);
        somePoiMapper = mappers.create(SomePoi.class);
        pizzaMapper = mappers.create(Pizza.class);
        flatMapper = mappers.create(Flat.class);

        engineConfiguration.addComponentClasses(Person.class, Looks.class, SomePoi.class, Pizza.class, Flat.class);

        person1.setName("A");
        person2.setName("B");
        person3.setName("C");
        person4.setName("D");

        looks1.setColor(Color.BLUE);
        looks1.setTexture(Texture.SHINY);
        looks2.setColor(Color.SILVER);
        looks2.setTexture(Texture.ROUGH);

        pizzaA.getOlives().add(Olive.of(OliveColor.BLACK, 1));
        pizzaA.getToppings().add(Topping.of(Ingredient.BACON, 1));
        pizzaA.getToppings().add(Topping.of(Ingredient.CHEESE, 10));
        pizzaA.getToppings().add(Topping.of(Ingredient.ANCHOVIS, 1));
        pizzaB.getOlives().add(Olive.of(OliveColor.GREEN, 1));
        pizzaB.getOlives().add(Olive.of(OliveColor.BLACK, 2));
        pizzaB.getOlives().add(Olive.of(OliveColor.BLACK, 3));
        pizzaB.getOlives().add(Olive.of(OliveColor.GREEN, 4));

        Store store = engineConfiguration.getEngine().getStore();
        personMapper.configureAndAttach(store);
        looksMapper.configureAndAttach(store);
        somePoiMapper.configureAndAttach(store);
        pizzaMapper.configureAndAttach(store);
        flatMapper.configureAndAttach(store);

        personMapper.save(person1, 1);
        personMapper.save(person2, 2);
        personMapper.save(person3, 3);
        personMapper.save(person4, 4);

        looksMapper.save(looks1, 0);
        looksMapper.save(looks2, 1);

        somePoiMapper.save(poi, 0);

        pizzaMapper.save(pizzaA, 5);
        pizzaMapper.save(pizzaB, 6);

        MapperSet mapperSet = engineConfiguration.getEngine().getMapperSet();
        Entity person1Entity = new Entity(mapperSet, session, 1);
        person1Entity.getOrCreate(Person.class);
        person1Entity.add(person1);
        Entity person3Entity = new Entity(mapperSet, session, 3);
        person3Entity.getOrCreate(Person.class);
        person3Entity.add(person3);
        Entity person4Entity = new Entity(mapperSet, session, 4);
        person4Entity.getOrCreate(Person.class);
        person4Entity.add(person4);
        flat.setOwner(person1Entity);
        flat.getTenants().add(person3Entity);
        flat.getTenants().add(person4Entity);

        flatMapper.save(flat, 7);

        store.fireUnderlyingDataChanged(0, 8);
    }

    @Test
    void copySelected() {
        EntitiesSampler sampler = new EntitiesSampler(engineConfiguration);
        ArraySelector selector = new ArraySelector();
        selector.addAll(new int[]{1, 4, 5, 6, 7});

        personMapper.configureAndAttach(newStore);
        looksMapper.configureAndAttach(newStore);
        pizzaMapper.configureAndAttach(newStore);
        flatMapper.configureAndAttach(newStore);

        sampler.copySelected(selector, newStore);

        assertThat(newStore.getCount()).isEqualTo(6);
        assertThat(newStore.get("old_id").getString(0)).isEqualTo("1");
        assertThat(newStore.get("old_id").getString(1)).isEqualTo("4");
        assertThat(newStore.get("old_id").getString(2)).isEqualTo("5");
        assertThat(newStore.get("old_id").getString(3)).isEqualTo("6");
        assertThat(newStore.get("old_id").getString(4)).isEqualTo("7");
        assertThat(newStore.get("old_id").getString(5)).isEqualTo("3");
        assertThat(newStore.get("olives_start").getString(2)).isEqualTo("0");
        assertThat(newStore.get("olives_start").getString(3)).isEqualTo("3");
        assertThat(newStore.get("olives_length").getString(2)).isEqualTo("3");
        assertThat(newStore.get("olives_length").getString(3)).isEqualTo("6");
        assertThat(newStore.get("olives.color").getString(3)).isEqualTo("GREEN");
        assertThat(newStore.get("olives.color").getString(4)).isEqualTo("BLACK");
        assertThat(newStore.get("olives.color").getString(5)).isEqualTo("BLACK");
        assertThat(newStore.get("olives.color").getString(6)).isEqualTo("GREEN");
        assertThat(newStore.get("toppings_ids").getString(2)).isEqualTo("1,2,3");
        assertThat(newStore.get("tenants").getString(4)).isEqualTo("5,1");
        assertThat(newStore.get("owner").getString(4)).isEqualTo("0");
    }
}