package pl.edu.icm.trurl.ecs.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.CounterWithSetup;
import pl.edu.icm.trurl.exampledata.CounterWithSetupMapper;
import pl.edu.icm.trurl.store.array.ArrayStore;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MapperSetupFeatureIT {
    ArrayStore store = new ArrayStore(1);
    CounterWithSetupMapper counterMapper = new CounterWithSetupMapper();

    @BeforeEach
    public void before() {
        counterMapper.configureAndAttach(store);
    }

    @Test
    @DisplayName("Should save Counter and load it with ")
    public void normalize() {
        // given
        CounterWithSetup counter = counterMapper.create();
        counter.setValue(17f);
        counterMapper.save(counter, 0);

        // execute
        CounterWithSetup loaded = counterMapper.createAndLoad(0);

        // assert
        assertThat(store.attributes().collect(Collectors.toList())).hasSize(1);
        assertThat(loaded.getOriginalValue()).isEqualTo(17f);
        assertThat(loaded.getValue()).isEqualTo(17f);
    }
}
