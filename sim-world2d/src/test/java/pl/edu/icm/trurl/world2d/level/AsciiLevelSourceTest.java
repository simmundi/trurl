package pl.edu.icm.trurl.world2d.level;

import net.snowyhollows.bento.Bento;
import org.junit.jupiter.api.Test;
import pl.edu.icm.trurl.ecs.Engine;
import pl.edu.icm.trurl.ecs.EngineBuilder;
import pl.edu.icm.trurl.ecs.EngineBuilderFactory;
import pl.edu.icm.trurl.ecs.Entity;
import pl.edu.icm.trurl.world2d.model.display.Displayable;
import pl.edu.icm.trurl.world2d.model.space.BoundingBox;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AsciiLevelSourceTest {

    @Test
    public void testExecute() throws IOException {
        // given
        Bento bento = Bento.createRoot();
        EngineBuilder engineBuilder = bento.get(EngineBuilderFactory.IT);
        engineBuilder.addComponentClasses(BoundingBox.class, Displayable.class);
        Engine engine = engineBuilder.getEngine();
        LevelExecutor executor = new LevelExecutor(engine);
        
        InputStream is = getClass().getResourceAsStream("/level.txt");
        Assertions.assertThat(is).isNotNull();
        AsciiLevelSource source = new AsciiLevelSource(is, 10, 10);
        
        List<String> createdTypes = new ArrayList<>();
        
        // execute
        executor.execute(source, 
            prototype -> !prototype.type().equals(" "), // Filter: skip spaces
            (entity, prototype) -> createdTypes.add(prototype.type()) // Customizer: just track types
        );
        
        // assert
        // Provided level.txt has 19x10 = 190 characters. 
        // Rows 0 and 9 are all # (19 * 2 = 38).
        // Middle rows have some #, O, X, p, +.
        Assertions.assertThat(createdTypes).contains("#", "O", "X", "p", "+");
        Assertions.assertThat(createdTypes).doesNotContain(" ");
        
        // Check geometry of one entity
        Entity firstHash = engine.getSession().findEntitiesInSession().stream()
                .filter(e -> e.get(BoundingBox.class).getCenterX() == 5 && e.get(BoundingBox.class).getCenterY() == 5)
                .findFirst().orElseThrow(AssertionError::new);
        
        BoundingBox box = firstHash.get(BoundingBox.class);
        Assertions.assertThat(box.getWidth()).isEqualTo(10);
        Assertions.assertThat(box.getHeight()).isEqualTo(10);
    }
}
