package pl.edu.icm.trurl.util.grid;

public class RectangularGridUtil {
    private final int width;
    private final int height;
    private final int offsetX;
    private final int offsetY;
    private final VisitInDisorder visitInDisorder;

    public RectangularGridUtil(int width, int height) {
        this.offsetX = 0;
        this.offsetY = 0;
        this.width = width;
        this.height = height;
        visitInDisorder = new VisitInDisorder(width * height);
    }

    public RectangularGridUtil(int cx, int cy, int rx, int ry) {
        offsetX = cx - rx;
        offsetY = cy - ry;
        width = 1 + rx * 2;
        height = 1 + ry * 2;
        visitInDisorder = new VisitInDisorder(width * height);
    }

    public void visit(int cx, int cy, FloatPointVisitor visitor) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < height; x++) {
                visitor.visit(x + offsetX + cx, y + offsetY + cy);
            }
        }
    }

    public boolean isWithin(int x, int y) {
        return x >= offsetX && x < width && x >= offsetY && y <= height;
    }

    public <T> T visitNth(int idx, FloatPointVisitor<T> visitor) {
        int tx = idx % width;
        int ty = idx / width;
        return visitor.visit(tx + offsetX, ty + offsetY);
    }

    public boolean visitRandomly(double rnd, FloatPointVisitor<Boolean> visitor) {
        int total = width * height;
        int startIdx = (int) (rnd * total);
        int idx = startIdx;
        for (int i = 0; i < total; i++) {
            if (visitNth(idx, visitor)) {
                return true;
            } else {
                idx = (startIdx + visitInDisorder.next()) % total;
            }
        }
        return false;
    }

    public int getArea() {
        return width * height;
    }
}
