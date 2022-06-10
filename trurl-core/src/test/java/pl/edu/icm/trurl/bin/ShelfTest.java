package pl.edu.icm.trurl.bin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ShelfTest {

    @Test
    public void test() {
        Shelf<String> shelf = new Shelf<>(5);

        for (int i = 0; i < 25; i++) {
            shelf.add(bin("bin " + i, 1));
        }

        int total = shelf.getTotal();

        assertThat(total).isEqualTo(25);
    }

    private Bin<String> bin(String label, int count) {
        return new Bin<>(label, count);
    }
}
