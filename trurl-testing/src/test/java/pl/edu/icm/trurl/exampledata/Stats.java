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

package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.dao.annotation.WithDao;

import java.util.Objects;

@WithDao
public class Stats {
    private int str;
    private int dex;
    private int wis;

    public Stats() {
    }

    public Stats(int str, int dex, int wis) {
        this.str = str;
        this.dex = dex;
        this.wis = wis;
    }

    public int getStr() {
        return str;
    }

    public void setStr(int str) {
        this.str = str;
    }

    public int getDex() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex = dex;
    }

    public int getWis() {
        return wis;
    }

    public void setWis(int wis) {
        this.wis = wis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stats stats = (Stats) o;
        return str == stats.str &&
                dex == stats.dex &&
                wis == stats.wis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(str, dex, wis);
    }

    @Override
    public String toString() {
        return "Stats{" +
                "str=" + str +
                ", dex=" + dex +
                ", wis=" + wis +
                '}';
    }
}
