package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Objects;

@WithMapper
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
