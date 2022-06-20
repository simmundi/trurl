package pl.edu.icm.trurl.ecs.parallel.domain;

import pl.edu.icm.trurl.ecs.annotation.NotMapped;
import pl.edu.icm.trurl.ecs.annotation.WithMapper;
import pl.edu.icm.trurl.ecs.mapper.feature.CanResolveConflicts;
import pl.edu.icm.trurl.ecs.mapper.feature.IsDirtyMarked;
import pl.edu.icm.trurl.ecs.mapper.feature.RequiresOriginalCopy;

@WithMapper
public class ParallelCounter implements RequiresOriginalCopy<ParallelCounter>, CanResolveConflicts<ParallelCounter>, IsDirtyMarked, HasAAndB {
    private int a;
    private int b;
    private @NotMapped int ownerId;
    private ParallelCounter originalCopy;

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

    @Override
    public void setOriginalCopy(ParallelCounter originalCopy) {
        this.originalCopy = originalCopy;
    }

    @Override
    public ParallelCounter resolve(ParallelCounter other) {
        other.a = this.a - originalCopy.a + other.a;
        other.b = this.b - originalCopy.b + other.b;
        return other;
    }

    @Override
    public int getOwnerId() {
        return ownerId;
    }

    @Override
    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public boolean isDirty() {
        return true;
    }
}
