package pl.edu.icm.trurl.bin;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BinPoolsByShapeTest {

    @Test
    void group() {
        // execute

        // a pool of letter "a"
        BinPoolsByShape<Integer, String> binPoolsByShape = BinPoolsByShape.group(
                Stream.of("ala", "ola", "bela", "rurka", "watażka", "ważka", "penicylina"),
                string -> (int)string.chars().filter(ch -> ch == 'a').count(),
                string -> Stream.of(string.length()));

        // assert
        int totalCountOfLetterA = binPoolsByShape.getAllBins().getTotalCount();
        BinPool<String> aIn5LetterWords = binPoolsByShape.getGroupedBins().get(5);

        assertThat(totalCountOfLetterA).isEqualTo(11);
        assertThat(aIn5LetterWords.getTotalCount()).isEqualTo(3);
        assertThat(binPoolsByShape.getGroupedBins().keySet()).contains(3,4,5,7,10);

    }

}
