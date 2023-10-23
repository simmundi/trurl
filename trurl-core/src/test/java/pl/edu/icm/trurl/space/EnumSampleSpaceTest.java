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
/*
package pl.edu.icm.trurl.space;

import org.assertj.core.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class EnumSampleSpaceTest {

    private enum Cities {
        WARSZAWA, KATOWICE, LUBLIN, SZCZECIN
    }

    @BeforeEach
    void setUp() {

    }

    @Test
    void addOutcome() {
        EnumSampleSpace<Cities> cities = new EnumSampleSpace<>(Cities.class);

        assertThat(cities.getProbability(Cities.LUBLIN)).isEqualTo(0.0f);

        cities.changeOutcome(Cities.KATOWICE, 0.1f);
        cities.changeOutcome(Cities.SZCZECIN, 0.9f);

        assertThat(cities.getProbability(Cities.KATOWICE)).isEqualTo(0.1f);
        assertThat(cities.getProbability(Cities.SZCZECIN)).isEqualTo(0.9f);
    }

    @Test
    void removeOutcome() {
        EnumSampleSpace<Cities> cities = new EnumSampleSpace<>(Cities.class);

        cities.changeOutcome(Cities.KATOWICE, 0.1f);
        cities.changeOutcome(Cities.SZCZECIN, 0.9f);
        cities.removeOutcome(Cities.KATOWICE);

        assertThat(cities.getProbability(Cities.KATOWICE)).isEqualTo(0.0f);

        cities.removeOutcome(Cities.SZCZECIN);
        cities.removeOutcome(Cities.KATOWICE);

        assertThat(cities.isEmpty()).isEqualTo(true);
    }

    @Test
    void handleDefaultOutcome() {
        EnumSampleSpace<Cities> cities = new EnumSampleSpace<>(Cities.class, Cities.WARSZAWA);

        assertThat(cities.sampleOrDefault(0.9)).isEqualTo(Cities.WARSZAWA);

        cities.changeOutcome(Cities.LUBLIN, 0.1f);
        cities.changeOutcome(Cities.SZCZECIN, 0.4f);

        assertThat(cities.sampleOrDefault(0.7)).isEqualTo(Cities.WARSZAWA);
        assertThat(cities.sampleOrDefault(10)).isEqualTo(Cities.WARSZAWA);
        assertThat(cities.sampleOrDefault(-20)).isEqualTo(Cities.WARSZAWA);
    }

    @Test
    void changeTwoOutcomes() {
        EnumSampleSpace<Cities> cities = new EnumSampleSpace<>(Cities.class);
        cities.changeOutcome(Cities.LUBLIN, 0.01f);
        cities.changeOutcome(Cities.WARSZAWA, 0.1f);
        cities.changeOutcome(Cities.KATOWICE, 0.09f);
        cities.changeOutcome(Cities.SZCZECIN, 0.8f);

        cities.changeTwoOutcomes(Cities.SZCZECIN, 0.125f, Cities.WARSZAWA);

        assertThat(cities.getProbability(Cities.LUBLIN)).isEqualTo(0.01f);
        assertThat(cities.getProbability(Cities.WARSZAWA)).isEqualTo(0.8f);
        assertThat(cities.getProbability(Cities.KATOWICE)).isEqualTo(0.09f);
        assertThat(cities.getProbability(Cities.SZCZECIN)).isEqualTo(0.1f);
    }

    @Test
    void normalize() {
        EnumSampleSpace<Cities> cities = new EnumSampleSpace<>(Cities.class);
        cities.changeOutcome(Cities.LUBLIN, 0.015f);
        cities.changeOutcome(Cities.WARSZAWA, 0.010f);
        cities.increaseOutcome(Cities.WARSZAWA, 0.015f);
        cities.changeOutcome(Cities.KATOWICE, 0.01f);

        cities.normalize();

        assertThat(cities.isNormalized()).isEqualTo(true);
        assertThat((double)Math.round(cities.getProbability(Cities.LUBLIN) * 10d) / 10d).isEqualTo(0.3);
        assertThat((double)Math.round(cities.getProbability(Cities.WARSZAWA) * 10d) / 10d).isEqualTo(0.5);
        assertThat((double)Math.round(cities.getProbability(Cities.KATOWICE) * 10d) / 10d).isEqualTo(0.2);;
    }

    @Test
    void sample() {
        SampleSpace<Cities> cities = new SampleSpace<>();
        cities.addOutcome(Cities.LUBLIN, 0.02f);
        cities.addOutcome(Cities.WARSZAWA, 0.02f);
        cities.addOutcome(Cities.KATOWICE, 0.02f);
        cities.normalize();

        Assertions.assertThat(asList(cities.sample(0.1), cities.sample(0.34), cities.sample(0.67)))
                .containsExactlyInAnyOrder(Cities.KATOWICE, Cities.LUBLIN, Cities.WARSZAWA);
    }

    @Test
    void multiply() {
        EnumSampleSpace<Cities> cities = new EnumSampleSpace<>(Cities.class);
        EnumSampleSpace<Cities> multipliers = new EnumSampleSpace<>(Cities.class);

        cities.changeOutcome(Cities.LUBLIN, 0.01f);
        cities.changeOutcome(Cities.WARSZAWA, 0.01f);
        cities.changeOutcome(Cities.KATOWICE, 0.04f);

        multipliers.changeOutcome(Cities.LUBLIN, 20.0f);
        multipliers.changeOutcome(Cities.WARSZAWA, 40.0f);
        multipliers.changeOutcome(Cities.KATOWICE, 10.0f);

        cities.multiply(multipliers);

        assertThat((double)Math.round(cities.getProbability(Cities.LUBLIN) * 10d) / 10d).isEqualTo(0.2);
        assertThat((double)Math.round(cities.getProbability(Cities.WARSZAWA) * 10d) / 10d).isEqualTo(0.4);
        assertThat((double)Math.round(cities.getProbability(Cities.KATOWICE) * 10d) / 10d).isEqualTo(0.4);
    }
}


 */