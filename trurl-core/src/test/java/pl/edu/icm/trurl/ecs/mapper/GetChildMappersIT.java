package pl.edu.icm.trurl.ecs.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.edu.icm.trurl.exampledata.PersonMapper;
import pl.edu.icm.trurl.exampledata.pizza.PizzaMapper;

@ExtendWith(MockitoExtension.class)
public class GetChildMappersIT {
    PizzaMapper pizzaMapper = new PizzaMapper();
    PersonMapper personMapper = new PersonMapper();

    @Test
    @DisplayName("Should return child mappers")
    public void getChildMappers() {
        // assert & execute
        Assertions.assertThat(pizzaMapper.getChildMappers()).containsExactlyInAnyOrder(
                pizzaMapper.getOlivesMapper(), pizzaMapper.getToppingsMapper()
        );
    }

    @Test
    @DisplayName("Should return an empty list for childless mapper")
    public void getChildMappers__empty() {
        // assert & execute
        Assertions.assertThat(personMapper.getChildMappers()).isEmpty();
    }

}
