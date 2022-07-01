package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.annotation.NotMapped;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.mapper.feature.RequiresSetup;

@WithMapper
public class CounterWithSetup implements RequiresSetup {
    private float value;
    @NotMapped
    private float originalValue;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(float originalValue) {
        this.originalValue = originalValue;
    }

    @Override
    public void setup() {
        this.originalValue = value;
    }
}
