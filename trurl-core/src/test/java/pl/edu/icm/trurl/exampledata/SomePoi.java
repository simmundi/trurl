package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.visnow.VnCoords;

@WithMapper
public class SomePoi implements VnCoords {
    private float x;
    private float y;
    private int intAttr;
    private float floatAttr;
    private double doubleAttr;
    private short shortAttribute;
    private boolean booleanAttribute;
    private Color enumAttribute;

    public int getIntAttr() {
        return intAttr;
    }

    public void setIntAttr(int intAttr) {
        this.intAttr = intAttr;
    }

    public float getFloatAttr() {
        return floatAttr;
    }

    public void setFloatAttr(float floatAttr) {
        this.floatAttr = floatAttr;
    }

    public double getDoubleAttr() {
        return doubleAttr;
    }

    public void setDoubleAttr(double doubleAttr) {
        this.doubleAttr = doubleAttr;
    }

    public short getShortAttribute() {
        return shortAttribute;
    }

    public void setShortAttribute(short shortAttribute) {
        this.shortAttribute = shortAttribute;
    }

    public boolean isBooleanAttribute() {
        return booleanAttribute;
    }

    public void setBooleanAttribute(boolean booleanAttribute) {
        this.booleanAttribute = booleanAttribute;
    }

    public Color getEnumAttribute() {
        return enumAttribute;
    }

    public void setEnumAttribute(Color enumAttribute) {
        this.enumAttribute = enumAttribute;
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
}
