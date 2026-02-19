package pl.edu.icm.trurl.gdx.action;

import net.snowyhollows.bento.annotation.ImplementationSwitch;
import net.snowyhollows.bento.annotation.ImplementationSwitch.When;

@ImplementationSwitch(cases = {
        @When(name = "default", implementation = DefaultDisplayListener.class, useByDefault = true)
})
public interface DisplayListener {
    void onRepresentationDrawn(int idx,
                               float centerX,
                               float centerY,
                               float drawWidth,
                               float drawHeight,
                               float rotation,
                               float scale,
                               float alpha);
}
