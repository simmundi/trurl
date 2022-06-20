package pl.edu.icm.trurl.ecs.parallel.domain;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class Counter implements HasAAndB {
    private int a;
    private int b;

    @Override
    public int getA() {
        return a;
    }

    @Override
    public void setA(int a) {
        this.a = a;
    }

    @Override
    public int getB() {
        return b;
    }

    @Override
    public void setB(int b) {
        this.b = b;
    }
}
