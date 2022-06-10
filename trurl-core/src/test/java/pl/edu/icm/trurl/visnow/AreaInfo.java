package pl.edu.icm.trurl.visnow;

import pl.edu.icm.trurl.ecs.annotation.WithMapper;

@WithMapper
public class AreaInfo {
    private int countOfTrees;

    public AreaInfo() {
    }

    public AreaInfo(int countOfTrees) {
        this.countOfTrees = countOfTrees;
    }

    public int getCountOfTrees() {
        return countOfTrees;
    }

    public void setCountOfTrees(int countOfTrees) {
        this.countOfTrees = countOfTrees;
    }
}
