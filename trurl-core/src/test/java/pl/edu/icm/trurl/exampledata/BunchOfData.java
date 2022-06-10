package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@WithMapper
public class BunchOfData {
    private boolean booleanProp;
    private byte byteProp;
    private double doubleProp;
    private List<Entity> entitiesProp = new ArrayList<>();
    private Entity entityProp;
    private Color enumProp;
    private float floatProp;
    private int intProp;
    private short shortProp;
    private String stringProp;
    private Looks looksProp;
    private List<Stats> statsProp = new ArrayList<>();

    public int getIntProp() {
        return intProp;
    }

    public void setIntProp(int intProp) {
        this.intProp = intProp;
    }

    public boolean isBooleanProp() {
        return booleanProp;
    }

    public void setBooleanProp(boolean booleanProp) {
        this.booleanProp = booleanProp;
    }

    public byte getByteProp() {
        return byteProp;
    }

    public void setByteProp(byte byteProp) {
        this.byteProp = byteProp;
    }

    public double getDoubleProp() {
        return doubleProp;
    }

    public void setDoubleProp(double doubleProp) {
        this.doubleProp = doubleProp;
    }

    public Color getEnumProp() {
        return enumProp;
    }

    public void setEnumProp(Color enumProp) {
        this.enumProp = enumProp;
    }

    public float getFloatProp() {
        return floatProp;
    }

    public void setFloatProp(float floatProp) {
        this.floatProp = floatProp;
    }

    public short getShortProp() {
        return shortProp;
    }

    public void setShortProp(short shortProp) {
        this.shortProp = shortProp;
    }

    public String getStringProp() {
        return stringProp;
    }

    public void setStringProp(String stringProp) {
        this.stringProp = stringProp;
    }

    public List<Entity> getEntitiesProp() {
        return entitiesProp;
    }

    public Entity getEntityProp() {
        return entityProp;
    }

    public void setEntityProp(Entity entityProp) {
        this.entityProp = entityProp;
    }

    public void setLooksProp(Looks looksProp) {
        this.looksProp = looksProp;
    }

    public Looks getLooksProp() {
        return looksProp;
    }

    public List<Stats> getStatsProp() {
        return statsProp;
    }

    public void setStatsProp(List<Stats> statsProp) {
        this.statsProp = statsProp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BunchOfData that = (BunchOfData) o;
        return booleanProp == that.booleanProp && byteProp == that.byteProp && Double.compare(that.doubleProp, doubleProp) == 0 && Float.compare(that.floatProp, floatProp) == 0 && intProp == that.intProp && shortProp == that.shortProp && Objects.equals(entitiesProp, that.entitiesProp) && Objects.equals(entityProp, that.entityProp) && enumProp == that.enumProp && Objects.equals(stringProp, that.stringProp) && Objects.equals(looksProp, that.looksProp) && statsProp.equals(that.statsProp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(booleanProp, byteProp, doubleProp, entitiesProp, entityProp, enumProp, floatProp, intProp, shortProp, stringProp, looksProp, statsProp);
    }
}
