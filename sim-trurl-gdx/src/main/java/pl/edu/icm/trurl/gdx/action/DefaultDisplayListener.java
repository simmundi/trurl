package pl.edu.icm.trurl.gdx.action;

import net.snowyhollows.bento.annotation.WithFactory;

public class DefaultDisplayListener implements DisplayListener {

    @WithFactory
    public DefaultDisplayListener() {
    }

    @Override
    public void onRepresentationDrawn(int idx, float centerX, float centerY, float drawWidth, float drawHeight, float rotation, float scale, float alpha) {
        // do nothing
    }
}
