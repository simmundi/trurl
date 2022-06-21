package pl.edu.icm.trurl.ecs.mapper;

import org.assertj.core.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.Person;
import pl.edu.icm.trurl.exampledata.PersonMapper;
import pl.edu.icm.trurl.store.array.ArrayStore;

@ExtendWith(MockitoExtension.class)
public class MapperFeaturesIT {
    ArrayStore store = new ArrayStore(1);
    PersonMapper personMapper = new PersonMapper();

    @BeforeEach
    public void before() {
        personMapper.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should save Person instance with normalized name")
    public void normalize() {
        // given
        Person person = personMapper.create();
        person.setName("  jan KoWaLskI   ");

        // execute
        personMapper.save(person, 0);

        // assert
        Assertions.assertThat(personMapper.getName(0)).isEqualTo("JAN KOWALSKI");
    }
}
