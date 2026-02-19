package pl.edu.icm.trurl.gdx.action;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import net.snowyhollows.bento.annotation.WithFactory;
import pl.edu.icm.trurl.gdx.action.render2d.RenderInstruction;
import pl.edu.icm.trurl.gdx.action.render2d.RenderInstructionDao;
import pl.edu.icm.trurl.gdx.managed.ManagedOrthographicCamera;
import pl.edu.icm.trurl.gdx.managed.ManagedSpriteBatch;
import pl.edu.icm.trurl.store.Store;
import pl.edu.icm.trurl.store.basic.BasicAttributeFactory;

public class DefaultRenderer implements Renderer {
    private final OrthographicCamera orthographicCamera;
    private final SpriteBatch spriteBatch;
    private final Store store;
    private final RenderInstructionDao renderInstructionDao;
    private int counter = 0;
    private RenderInstruction renderInstruction = new RenderInstruction();

    @WithFactory
    public DefaultRenderer(ManagedOrthographicCamera orthographicCamera, ManagedSpriteBatch spriteBatch) {
        this.orthographicCamera = orthographicCamera;
        this.spriteBatch = spriteBatch;
        store = new Store(new BasicAttributeFactory(), 100000);
        renderInstructionDao = new RenderInstructionDao("");
        renderInstructionDao.configureAndAttach(store);
    }

    @Override
    public boolean isInFrustum(float centerX, float centerY, float width, float height) {
        return orthographicCamera.frustum.boundsInFrustum(centerX, centerY, 0, width / 2, height / 2, 0);
    }

    @Override
    public void clear() {
        counter = 0;
    }

    @Override
    public void render() {
        spriteBatch.setProjectionMatrix(orthographicCamera.combined);
        spriteBatch.begin();

        for (int i = 0; i < counter; i++) {
            renderInstructionDao.load(null, renderInstruction, i);
            Color color = spriteBatch.getColor();
            spriteBatch.enableBlending();
            spriteBatch.setColor(color.r, color.g, color.b, renderInstruction.getAlpha());
            TextureRegion textureRegion = renderInstruction.getTextureRegion();
            boolean regionIsFlipped = textureRegion.isFlipX();
            if (renderInstruction.isHorizontalFlip()) {
                if (!regionIsFlipped) {
                    textureRegion.flip(true, false);
                }
            } else {
                if (regionIsFlipped) {
                    textureRegion.flip(true, false);
                }
            }
            if (textureRegion != null) {
                spriteBatch.draw(textureRegion,
                        renderInstruction.getX(),
                        renderInstruction.getY(),
                        renderInstruction.getWidth() / 2,
                        renderInstruction.getHeight() / 2,
                        renderInstruction.getWidth(),
                        renderInstruction.getHeight(),
                        renderInstruction.getScale(),
                        renderInstruction.getScale(),
                        90 + renderInstruction.getRotation(),
                        true);
            }
        }
        spriteBatch.end();
    }

    @Override
    public void addTextureRegion(TextureRegion textureRegion,
                                 float x,
                                 float y,
                                 float width,
                                 float height,
                                 float scale,
                                 float rotation,
                                 float alpha,
                                 boolean horizontalFlip) {

        renderInstruction.setTextureRegion(textureRegion);
        renderInstruction.setX(x);
        renderInstruction.setY(y);
        renderInstruction.setWidth(width);
        renderInstruction.setHeight(height);
        renderInstruction.setRotation(rotation);
        renderInstruction.setAlpha(alpha);
        renderInstruction.setScale(scale);
        renderInstruction.setHorizontalFlip(horizontalFlip);

        renderInstructionDao.save(renderInstruction, counter++);
    }
}
