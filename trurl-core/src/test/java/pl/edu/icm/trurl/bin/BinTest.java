package pl.edu.icm.trurl.bin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class BinTest {
    Bin<String> bin;

    @BeforeEach
    void before() {
        bin = new Bin<>("a", 100);
    }

    @Test
    void add() {
        // execute
        bin.add(100);

        // assert
        assertThat(bin.getCount()).isEqualTo(200);
    }




}
