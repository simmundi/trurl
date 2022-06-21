package pl.edu.icm.trurl.exampledata;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.mapper.feature.CanBeNormalized;

import java.util.Locale;

@WithMapper
public class Person implements CanBeNormalized {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void normalize() {
        if (name != null) {
            name = name.trim().toUpperCase(Locale.ROOT);
        }
    }
}
