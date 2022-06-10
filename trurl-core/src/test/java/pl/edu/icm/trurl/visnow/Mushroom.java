package pl.edu.icm.trurl.visnow;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.exampledata.Color;

@WithMapper
public class Mushroom implements VnCoords {
    private float x;
    private float y;
    private double height;
    private Color color;
    private short diameter;

    public Mushroom() {
    }

    public Mushroom(float x, float y, double height, Color color, short diameter) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.color = color;
        this.diameter = diameter;
    }

    @Override
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public short getDiameter() {
        return diameter;
    }

    public void setDiameter(short diameter) {
        this.diameter = diameter;
    }
}
