package pl.edu.icm.trurl.bin;

import com.google.common.collect.Streams;

import java.util.Arrays;
import java.util.stream.Stream;

class Shelf<Label> {
    private int currentTotalInShelf;
    private int lastIndex = 0;
    private final Bin<Label>[] bins;
    private final Shelf<Label> previous;
    private Shelf<Label> next;

    public Shelf(int size) {
        this(null, size);
    }

    private Shelf(Shelf<Label> previous, int size) {
        this.previous = previous;
        this.bins = new Bin[size];
    }

    public Shelf add(Bin<Label> bin) {
        if (lastIndex < bins.length) {
            bins[lastIndex++] = bin;
            currentTotalInShelf += bin.getCount();
            bin.addListener( difference -> {
                currentTotalInShelf += difference;
            });
            return this;
        } else {
            if (next == null) {
                next = new Shelf<>(this, bins.length);
            }
            next.add(bin);
            return next;
        }
    }

    public Bin<Label> find(int count) {
        if (count >= currentTotalInShelf) {
            return next.find(count - currentTotalInShelf);
        }
        int acc = 0;
        for (Bin<?> bin : bins) {
            acc += bin.getCount();
            if (count < acc) {
                return (Bin<Label>) bin;
            }
        }
        throw new IllegalStateException();
    }

    public int getTotal() {
        int nextTotal = next == null ? 0 : next.getTotal();
        return nextTotal + currentTotalInShelf;
    }

    public void reset() {
        for (int i = 0; i < lastIndex; i++) {
            bins[i].reset();
        }
        if (next != null) {
            next.reset();
        }
    }

    public Stream<Bin<Label>> streamBins() {
        Stream<Bin<Label>> myBins = Arrays.stream(bins, 0, lastIndex);
        if (next == null) {
            return myBins;
        } else {
            return Streams.concat(myBins, next.streamBins());
        }
    }
}
