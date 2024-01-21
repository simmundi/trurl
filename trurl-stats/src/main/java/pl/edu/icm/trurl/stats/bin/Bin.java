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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single bin in a histogram, labeled LABEL.
 * <p>
 * Can also be imagined as:
 * <ul>
 * <li>representation of N identical objects of type LABEL (e.g. 3 000 000 identical apples</li>
 * <li>representation of N identical slots associated with the LABEL</li>
 * </ul>
 * Bin stores one instance of LABEL, an integer representing the count and the initial count.
 * <p>
 * Count can be decremented by picking the value, or it can be reset to the original count.
 * <p>
 * Bins are useful when they are part of BinPools, and can be further aggregated into BinPoolsByShape.
 */
public class Bin<Label> {
    private final Label label;
    private final int initialCount;
    private int count;
    private final List<BinListener<Label>> listeners = new ArrayList<>(4);

    /**
     * Creates a bin with initial count. The initial count and the label cannot ever changed.
     *
     * @param label to associate with the Bin
     * @param count to be set as current and initial
     */
    Bin(Label label, int count) {
        this.label = label;
        this.count = count;
        this.initialCount = count;
    }

    /**
     * Decrements the bin counter by one (it CAN drop beneath 0).
     *
     * @return label (useful when the call is chained through BinPool#sample)
     */
    public Label pick() {
        return pick(1);
    }

    /**
     * Decrements the bin counter by any value (it CAN drop beneath 0).
     *
     * @return label (useful when the call is chained through BinPool#sample)
     */
    public Label pick(int n) {
        count -= n;
        notifyListeners(-n);
        return label;
    }

    /**
     * Sugar for Increments the bin counter by any value.
     *
     * @return label (useful when the call is chained through BinPool#sample)
     */
    public Label add(int n) {
        return pick(-n);
    }

    /**
     * Resets the counter to the initial value;
     */
    public void reset() {
        int diff = initialCount - count;
        count = initialCount;
        notifyListeners(diff);
    }

    public Label getLabel() {
        return label;
    }

    public int getCount() {
        return count;
    }

    public int getInitialCount() {
        return initialCount;
    }

    void addListener(BinListener<Label> binListener) {
        listeners.add(binListener);
    }

    private void notifyListeners(int diff) {
        int size = listeners.size();
        for (int j = 0; j < size; j++) {
            listeners.get(j).counterChanged(diff);
        }
    }

    @Override
    public String toString() {
        return "Bin{" +
                "label=" + label +
                ", count=" + count +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bin that = (Bin) o;
        return label == that.label && count == that.count;
    }
}
