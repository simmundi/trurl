package pl.edu.icm.trurl.gdx;

import com.badlogic.gdx.ApplicationListener;
import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.BentoFactory;

import java.util.function.Consumer;

public final class BentoApplicationAdapter implements ApplicationListener {

    private final BentoFactory<? extends ApplicationListener> factory;
    private final Bento bento;
    ApplicationListener applicationListener;

    public BentoApplicationAdapter(BentoFactory<? extends ApplicationListener> factory) {
        this(factory, bento -> {});
    }

    public BentoApplicationAdapter(BentoFactory<? extends ApplicationListener> factory, Consumer<Bento> configurer) {
        this(Bento.createRoot(), factory);
        configurer.accept(bento);
    }

    public BentoApplicationAdapter(Bento bento, BentoFactory<? extends ApplicationListener> factory) {
        this.bento = bento;
        this.factory = factory;
    }

    @Override
    public void create() {
        applicationListener = bento.get(factory);
        applicationListener.create();
    }

    @Override
    public void resize(int width, int height) {
        applicationListener.resize(width, height);
    }

    @Override
    public void render() {
        applicationListener.render();
    }

    @Override
    public void pause() {
        applicationListener.pause();
    }

    @Override
    public void resume() {
        applicationListener.resume();
    }

    @Override
    public void dispose() {
        applicationListener.dispose();
    }
}
