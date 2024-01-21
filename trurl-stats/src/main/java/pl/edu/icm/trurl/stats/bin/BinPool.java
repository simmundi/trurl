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
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Collection of Bins; can be imagined as representation of a histogram.
 * <p>
 * The bins can be accessed by a uniform value <0..1), weighted by the current count
 * of the bins, which allows us to quickly sample the histogram (i.e. pick a semi-random
 * LABEL value based on the height of the LABEL's bin) and trivially implement picking
 * without returning (because we can sample a bin and then call pick on it).
 *
 * <p>
 * For example if we want to emulate picking of 100 random pieces of fruit from a fruit basket,
 * we can use BinPool like so:
 *
 * <pre>{@code
 *     BinPool<String> basket = new BinPool<>();
 *     pool.add("Banana", 50);
 *     pool.add("Apple", 50);
 *
 *     while (pool.getTotalCount() > 0) {
 *         var f = pool.sample(Math.random());
 *         String result = (i++) + " " + f.pick();
 *         System.out.println(result);
 *     }
 * }
 * </pre>
 *
 * @param <Label>
 */
public class BinPool<Label> {
    private final Shelf<Label> shelf;
    private int totalCount;
    private int initialCount;

    public BinPool() {
        this(500);
    }

    public BinPool(int shelfSize) {
        shelf = new Shelf<>(shelfSize);
    }

    /**
     * Creates and adds a Bin
     *
     * @param label
     * @param count
     * @return the freshly created bin
     */
    public Bin<Label> add(Label label, int count) {
        Bin<Label> bin = new Bin<>(label, count);
        addBin(bin);
        return bin;
    }

    /**
     * Adds any given Bin to the BinPool
     *
     * @param bin
     */
    public void addBin(Bin<Label> bin) {
        shelf.add(bin);
        bin.addListener((difference) -> totalCount += difference);
        totalCount += bin.getCount();
        initialCount += bin.getInitialCount();
    }

    /**
     * Returns the bin which would contain the argument if all
     * the bins of the were represented by segments of length <i>count</i>,
     * joined one to another on the same line and their sum of lengths normalized to one.
     * <p>
     * e.g. if we have a pool containing bins: 51 apples, 39 bananas and 10 grapes,
     * value 0 will return the bin representing apples, 0.5 will return the bin
     * representing bananas, and values above 0.9 will return the grapes.
     *
     * @param random double from range <0..1)
     * @return Bin
     */
    public Bin<Label> sample(double random) {
        if (totalCount == 0) {
            shelf.reset();
        }

        int index = (int) Math.floor(totalCount * random);
        return shelf.find(index);
    }

    /**
     * Works like {@see sample}, but the number given is not normalized
     * to one.
     * <p>
     * e.g. if we have a pool containing bins: 51 apples, 39 bananas and 10 grapes,
     * values between 0 and 50 will the bin representing apples, 51-89 will return the bin
     * representing bananas, and values between 90 and 99 will return the grapes.
     *
     * @param index
     * @return
     */
    public Bin<Label> sampleNth(int index) {
        if (initialCount == 0) {
            throw new IllegalStateException("no bins in pool");
        }

        if (totalCount == 0) {
            shelf.reset();
        }
        return shelf.find(index);
    }

    /**
     * Creates a pool containing only the bin instances
     * with matching labels. The bins in the new pool will be the same
     * bins, not merely identical ones.
     *
     * @param labels - collection of labels
     * @return the new BinPool
     */
    public BinPool<Label> createSubPool(Collection<Label> labels) {
        BinPool<Label> subPool = new BinPool<>();

        shelf.streamBins()
                .filter(bin -> labels.contains(bin.getLabel()))
                .forEach(bin -> subPool.addBin(bin));

        return subPool;
    }

    /**
     * Creates a pool containing only the bin instances
     * with matching labels. The bins in the new pool will be the same
     * bins, not merely identical ones.
     *
     * @param labels - collection of labels
     * @return the new BinPool
     */
    public BinPool<Label> createSubPool(Label... labels) {
        return createSubPool(Arrays.asList(labels));
    }

    /**
     * returns sum of current counts of all bins
     */
    public int getTotalCount() {
        return totalCount;
    }

    /**
     * Streams all the bins
     *
     * @return
     */
    public Stream<Bin<Label>> streamBins() {
        return this.shelf.streamBins();
    }
}
