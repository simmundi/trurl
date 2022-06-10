package pl.edu.icm.trurl.util.grid;

public interface FloatPointVisitor<Result> {
    Result visit(float x, float y);
}
