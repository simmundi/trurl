package pl.edu.icm.trurl.ecs.util;

import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.exampledata.Looks;
import pl.edu.icm.trurl.exampledata.Stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DynamicComponentAccessorTest {

    @Test
    void classToIndex() {
        // given
        DynamicComponentAccessor ci = new DynamicComponentAccessor(Looks.class, Stats.class);

        // execute
        int looksIdx = ci.classToIndex(Looks.class);
        int statsIdx = ci.classToIndex(Stats.class);

        // assert
        assertThat(looksIdx).isEqualTo(0);
        assertThat(statsIdx).isEqualTo(1);
    }

    @Test
    void indexToClass() {
        // given
        DynamicComponentAccessor ci = new DynamicComponentAccessor(Looks.class, Stats.class);

        // execute
        Class<?> statsClass = ci.indexToClass(1);
        Class<?> looksClass = ci.indexToClass(0);

        // assert
        assertThat(statsClass).isEqualTo(Stats.class);
        assertThat(looksClass).isEqualTo(Looks.class);
    }

    @Test
    void componentCount() {
        // given
        DynamicComponentAccessor ci = new DynamicComponentAccessor(Looks.class, Stats.class);

        // execute
        int count = ci.componentCount();

        // assert
        assertThat(count).isEqualTo(2);
    }
}
