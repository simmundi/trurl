package pl.edu.icm.trurl.exampledata.pizza;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.Random;

@WithMapper
public class Olive {
    private OliveColor color;
    private float size;

    public Olive() {
    }

    public Olive(OliveColor color, float size) {
        this.color = color;
        this.size = size;
    }

    public OliveColor getColor() {
        return color;
    }

    public void setColor(OliveColor color) {
        this.color = color;
    }

    public float getSize() {
        return size;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public static Olive of(OliveColor color, float size) {
        return new Olive(color, size);
    }

    public static Olive random() {
        OliveColor[] colors = OliveColor.values();
        Random random = new Random();
        return of(colors[random.nextInt(colors.length)], random.nextFloat() * 10 + 1);
    }

}
