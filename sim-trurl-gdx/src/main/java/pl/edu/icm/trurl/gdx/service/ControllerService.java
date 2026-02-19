package pl.edu.icm.trurl.gdx.service;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import net.snowyhollows.bento.annotation.WithFactory;

public class ControllerService {

    private static final Direction[] directions = {
            Direction.NEUTRAL,      // 0000
            Direction.RIGHT,        // 0001
            Direction.LEFT,         // 0010
            Direction.RIGHT,        // 0011 *prefers right
            Direction.DOWN,         // 0100
            Direction.DOWN_RIGHT,   // 0101
            Direction.DOWN_LEFT,    // 0110
            Direction.DOWN_RIGHT,   // 0111 *prefers right
            Direction.UP,           // 1000
            Direction.UP_RIGHT,     // 1001
            Direction.UP_LEFT,      // 1010
            Direction.UP_RIGHT,     // 1011 *prefers right
            Direction.UP,           // 1100 *prefers up
            Direction.NEUTRAL,      // 1101 *confused
            Direction.NEUTRAL,      // 1110
            Direction.NEUTRAL       // 1111
    };

    @WithFactory
    public ControllerService() {
    }

    public Direction currentDirection() {
        int target =
                (Gdx.input.isKeyPressed(Input.Keys.RIGHT) ? 1 : 0)
                        + (Gdx.input.isKeyPressed(Input.Keys.LEFT) ? 2 : 0)
                        + (Gdx.input.isKeyPressed(Input.Keys.DOWN) ? 4 : 0)
                        + (Gdx.input.isKeyPressed(Input.Keys.UP) ? 8 : 0);
        return directions[target];
    }

    public enum Direction {
        NEUTRAL(0, 0),
        UP(0, 1),
        UP_RIGHT(1, 1),
        RIGHT(1, 0),
        DOWN_RIGHT(1, -1),
        DOWN(0, -1),
        DOWN_LEFT(-1, -1),
        LEFT(-1, 0),
        UP_LEFT(-1, 1);
        public final int horizontal;
        public final int vertical;

        Direction(int dx, int dy) {
            this.horizontal = dx;
            this.vertical = dy;
        }

        public boolean isUp() {
            return vertical == 1;
        }

        public boolean isDown() {
            return vertical == -1;
        }

        public boolean isLeft() {
            return horizontal == -1;
        }

        public boolean isRight() {
            return horizontal == 1;
        }
    }

}
