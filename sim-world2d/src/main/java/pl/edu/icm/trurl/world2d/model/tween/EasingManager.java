package pl.edu.icm.trurl.world2d.model.tween;

import net.snowyhollows.bento.Bento;
import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.CategoryManager;

import java.util.Arrays;
import java.util.List;

/**
 * This code is mostly pasted from Aurelien Ribon's (http://www.aurelienribon.com/)
 * Universal Tween Engine, and the math comes from Robert Penner's easing equations (http://robertpenner.com/easing/)
 */
public class EasingManager extends CategoryManager<Easing> {

    @WithFactory
    public EasingManager(Bento bento) {
        super(bento, "world2d.model.tween.easing", EasingFactory.IT);
    }


    @Override
    protected List<Easing> getBuiltIns() {
        return Arrays.asList(Easing.LINEAR, Easing.BACK_IN, Easing.BACK_OUT, Easing.BACK_INOUT, Easing.BOUNCE_OUT, Easing.BOUNCE_IN, Easing.BOUNCE_INOUT);
    }

    public Easing[] emptyArray() {
        return new Easing[0];
    }
}
