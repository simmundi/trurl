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

import java.util.Arrays;

/**
 * Contains float[] representing possible outcomes (enum labels) with probability of picking.
 *
 * @param <Label>
 */
public class EnumSampleSpace<Label extends Enum<Label>> {
    private final float[] outcomes;
    private boolean normalized;
    private Label defaultOutcome;
    private final Class<Label> keyType;

    public EnumSampleSpace(Class<Label> keyType) {
        outcomes = new float[keyType.getEnumConstants().length];
        normalized = false;
        this.keyType = keyType;
    }

    public EnumSampleSpace(Class<Label> keyType, Label defaultOutcome) {
        outcomes = new float[keyType.getEnumConstants().length];
        normalized = false;
        this.defaultOutcome = defaultOutcome;
        this.keyType = keyType;
    }

    /**
     * Changes possible outcome.
     *
     * @param label
     * @param probability
     */
    public void changeOutcome(Label label, float probability) {
        outcomes[label.ordinal()] = probability;
        normalized = false;
    }

    /**
     * Changes possible outcome, so that:
     * new value = old value + delta.
     *
     * @param label
     * @param delta
     */
    public void increaseOutcome(Label label, float delta) {
        outcomes[label.ordinal()] += delta;
        normalized = false;
    }

    /**
     * Removes an outcome for a given label if it exists (optional operation).
     *
     * @param label
     */
    public void removeOutcome(Label label) {
        outcomes[label.ordinal()] = 0.0f;
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
     * and increases probability of the second one, so that EnumSampleSpace is still normalized.
     * If EnumSampleSpace does not contain given outcomes, returns false.
     *
     * @param multiplier float from range <0..1>
     * @return true if SampleSpace contains values for given outcomes, false if not
     */
    public boolean changeTwoOutcomes(Label toBeReduced, float multiplier, Label toBeIncreased) {
        if (multiplier < 0 || multiplier > 1) {
            throw new IllegalArgumentException("Parameter multiplier = " + multiplier + " out of range <0..1>");
        }
        if (outcomes[toBeReduced.ordinal()] != 0.0f && outcomes[toBeIncreased.ordinal()] != 0.0f) {
            float oldValue = outcomes[toBeReduced.ordinal()];
            float newValue = oldValue * multiplier;
            outcomes[toBeReduced.ordinal()] = newValue;
            outcomes[toBeIncreased.ordinal()] += (oldValue - newValue);
            return true;
        } else {
            return false;
        }
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
        for (int i = 0; i < outcomes.length; i++) {
            outcomes[i] *= factor;
        }
        normalized = true;
    }

    /**
     * Returns sum of probabilities.
     *
     * @return sum of probabilities
     */
    public float sumOfProbabilities() {
        float sum = 0.0f;
        for (int i = 0; i < outcomes.length; i++) {
            sum += outcomes[i];
        }
        return sum;
    }


    /**
     * Samples an outcome from EnumSampleSpace.
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
        for (int i = 0; i < outcomes.length; i++) {
            if (outcomes[i] != 0.0f) {
                outcome = keyType.getEnumConstants()[i];
                cumulativeProbability += outcomes[i];
                if (random < cumulativeProbability) break;
            }
        }
        return outcome;
    }

    /**
     * Samples an outcome from EnumSampleSpace.
     * EnumSampleSpace does not have to be normalized before.
     * Returns defaultOutcome when nothing else matches given random value.
     *
     * @param random value
     * @return Label of an outcome
     */
    public Label sampleOrDefault(double random) {
        float cumulativeProbability = 0.0f;
        for (int i = 0; i < outcomes.length; i++) {
            if (outcomes[i] != 0.0f) {
                cumulativeProbability += outcomes[i];
                if (random < cumulativeProbability && random > 0) return keyType.getEnumConstants()[i];
            }
        }
        return defaultOutcome;
    }

    /**
     * Checks if EnumSampleSpace is normalized.
     *
     * @return true if EnumSampleSpace is normalized
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
     * Checks if EnumSampleSpace is empty.
     *
     * @return true if EnumSampleSpace is empty
     */
    public boolean isEmpty() {
        for (int i = 0; i < outcomes.length; i++) {
            if (outcomes[i] != 0.0f) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if EnumSampleSpace contains non-zero probability value for a given outcome.
     *
     * @return true if EnumSampleSpace contains non-zero probability value for a given outcome
     */
    public boolean hasNonZeroProbability(Label outcome) {
        return outcomes[outcome.ordinal()] != 0.0f;
    }

    /**
     * Returns probability of a given outcome.
     * If EnumSampleSpace does not contain given outcome, returns -1
     *
     * @param outcome
     * @return probability
     */
    public float getProbability(Label outcome) {
        return outcomes[outcome.ordinal()];
    }

    /**
     * Performs multiplication of outcome values in EnumSampleSpaces for each label.
     *
     * @param sampleSpace
     */
    public void multiply(EnumSampleSpace<Label> sampleSpace) {
        for (int i = 0; i < outcomes.length; i++) {
            outcomes[i] *= sampleSpace.outcomes[i];
        }
        normalized = false;
    }

    @Override
    public String toString() {
        return "SampleSpace{" +
                "outcomes=" + Arrays.toString(outcomes) +
                '}';
    }
}
