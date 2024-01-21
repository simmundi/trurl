/*
 * Copyright (c) 2022 ICM Epidemiological Model Team at Interdisciplinary Centre for Mathematical and Computational Modelling, University of Warsaw.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */

package pl.edu.icm.trurl.stats.bin;

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
            return Stream.concat(myBins, next.streamBins());
        }
    }
}
