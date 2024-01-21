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

package pl.edu.icm.trurl.stats.space;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains Map<Label,Float> representing possible outcomes with probability of picking.
 *
 * @param <Label>
 */
public class SampleSpace<Label> {
    private final Map<Label, Float> outcomes;
    private boolean normalized;
    private Label defaultOutcome;

    public SampleSpace() {
        outcomes = new HashMap<>();
        normalized = false;
    }

    public SampleSpace(Label defaultOutcome) {
        outcomes = new HashMap<>();
        normalized = false;
        this.defaultOutcome = defaultOutcome;
    }

    /**
     * Adds a possible outcome.
     *
     * @param label
     * @param probability
     */
    public void addOutcome(Label label, float probability) {
        outcomes.put(label, probability);
        normalized = false;
    }

    /**
     * Removes an outcome for a given label if it exists (optional operation).
     *
     * @param label
     */
    public void removeOutcome(Label label) {
        outcomes.remove(label);
        normalized = false;
    }

    /**
     * Sets a default outcome
     *
     * @param defaultOutcome
     */
    public void setDefaultOutcome(Label defaultOutcome) {
        this.defaultOutcome = defaultOutcome;
    }

    /**
     * Reduces probability of the first outcome (according to a given multiplier)
     * and increases probability of the second one, so that SampleSpace is still normalized.
     * If SampleSpace does not contain given outcomes, returns false.
     *
     * @param multiplier float from range <0..1>
     * @return true if SampleSpace contains values for given outcomes, false if not
     */
    public boolean changeTwoOutcomes(Label toBeReduced, float multiplier, Label toBeIncreased) {
        if (multiplier < 0 || multiplier > 1) {
            throw new IllegalArgumentException("Parameter multiplier = " + multiplier + " out of range <0..1>");
        }
        if (outcomes.containsKey(toBeReduced) && outcomes.containsKey(toBeIncreased)) {
            float oldValue = outcomes.get(toBeReduced);
            float newValue = oldValue * multiplier;
            outcomes.replace(toBeReduced, newValue);
            float oldValueToBeIncreased = outcomes.get(toBeIncreased);
            outcomes.replace(toBeIncreased, oldValueToBeIncreased + oldValue - newValue);
            return true;
        } else return false;
    }

    /**
     * Normalizes probabilities of outcomes, so that sum of probabilities = 1
     */
    public void normalize() {
        float sum = sumOfProbabilities();
        if (sum == 0.0f) throw new IllegalStateException("Cannot normalize a SampleSpace with 0 probability");
        sum = (float) Math.round(sum * 1000000f) / 1000000f;
        if (sum == 1.0f) {
            normalized = true;
            return;
        }
        float factor = 1.0f / sum;
        for (Label key : outcomes.keySet()) {
            outcomes.compute(key, (k, v) -> v *= factor);
        }
        normalized = true;
    }

    /**
     * Returns sum of probabilities.
     *
     * @return Label of an outcome
     */
    public float sumOfProbabilities() {
        float sum = 0.0f;
        for (Float value : outcomes.values()) {
            sum += value;
        }
        return sum;
    }


    /**
     * Samples an outcome from SampleSpace.
     * SampleSpace should be explicitly normalized before using normalize() or checked using isNormalized().
     *
     * @param random value from range <0..1)
     * @return Label of an outcome
     */
    public Label sample(double random) {
        if (!normalized) {
            throw new IllegalStateException("Cannot sample without normalization");
        }
        if (random >= 1 || random < 0) throw new IllegalArgumentException("Random should be a value from range <0..1)");
        float cumulativeProbability = 0.0f;
        Label outcome = null;
        for (Label key : outcomes.keySet()) {
            outcome = key;
            cumulativeProbability += outcomes.get(key);
            if (random < cumulativeProbability) break;
        }
        return outcome;
    }

    /**
     * Samples an outcome from SampleSpace.
     * SampleSpace does not have to be normalized before.
     * Returns defaultOutcome when nothing else matches given random value.
     *
     * @param random value
     * @return Label of an outcome
     */
    public Label sampleOrDefault(double random) {
        float cumulativeProbability = 0.0f;
        for (Label key : outcomes.keySet()) {
            cumulativeProbability += outcomes.get(key);
            if (random < cumulativeProbability && random > 0) return key;
        }
        return defaultOutcome;
    }

    /**
     * Checks if a SampleSpace is normalized.
     *
     * @return true if a SampleSpace is normalized
     */
    public boolean isNormalized() {
        float sum = sumOfProbabilities();
        sum = (float) Math.round(sum * 1000000f) / 1000000f;
        if (sum == 1.0f) {
            normalized = true;
        }
        return normalized;
    }

    /**
     * Checks if a SampleSpace is empty.
     *
     * @return true if a SampleSpace is empty
     */
    public boolean isEmpty() {
        return outcomes.isEmpty();
    }

    /**
     * Checks if a SampleSpace contains probability value for a given outcome.
     *
     * @return true if a SampleSpace contains probability value for a given outcome
     */
    public boolean contains(Label outcome) {
        return outcomes.containsKey(outcome);
    }

    /**
     * Returns probability of a given outcome.
     * If SampleSpace does not contain given outcome, returns -1
     *
     * @param outcome
     * @return probability
     */
    public float getProbability(Label outcome) {
        if (!outcomes.containsKey(outcome)) return -1.0f;
        return outcomes.get(outcome);
    }

    public Map<Label, Float> toMap() {
        return outcomes;
    }

    @Override
    public String toString() {
        return "SampleSpace{" +
                "outcomes=" + outcomes +
                '}';
    }
}
