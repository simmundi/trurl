package pl.edu.icm.trurl.world2d.model.tween;

import net.snowyhollows.bento.annotation.WithFactory;
import net.snowyhollows.bento.category.Category;

public class Easing implements Category {

    public final static Easing LINEAR = new Easing("LINEAR", 0) {
        @Override
        public float compute(float t) {
            return t;
        }
    };
    public final static Easing BACK_IN = new Easing("BACK_IN", 1) {
        @Override
        public final float compute(float t) {
            float s = 1.70158f;
            return t*t*((s+1)*t - s);
        }
    };
    public final static Easing BACK_OUT = new Easing("BACK_OUT", 2) {
        @Override
        public final float compute(float t) {
            float s = 1.70158f;
            return (t -= 1) * t * ((s + 1) * t + s) + 1;
        }
    };
    public final static Easing BACK_INOUT = new Easing("BACK_INOUT", 3) {
        public final float compute(float t) {
            float s = 1.70158f;
            if ((t*=2) < 1) return 0.5f*(t*t*(((s*=(1.525f))+1)*t - s));
            return 0.5f*((t-=2)*t*(((s*=(1.525f))+1)*t + s) + 2);
        }
    };
    public final static Easing BOUNCE_OUT = new Easing("BOUNCE_OUT", 4) {
        @Override
        public final float compute(float t) {
            if (t < (1/2.75)) {
                return 7.5625f*t*t;
            } else if (t < (2/2.75)) {
                return 7.5625f*(t-=(1.5f/2.75f))*t + .75f;
            } else if (t < (2.5/2.75)) {
                return 7.5625f*(t-=(2.25f/2.75f))*t + .9375f;
            } else {
                return 7.5625f*(t-=(2.625f/2.75f))*t + .984375f;
            }
        }
    };
    public final static Easing BOUNCE_IN = new Easing("BOUNCE_IN", 5) {
        @Override
        public final float compute(float t) {
            return 1 - BOUNCE_OUT.compute(1-t);
        }
    };
    public final static Easing BOUNCE_INOUT = new Easing("BOUNCE_INOUT", 6) {
        @Override
        public final float compute(float t) {
            if (t < 0.5f) return BOUNCE_IN.compute(t*2) * .5f;
            return BOUNCE_OUT.compute(t*2-1) * .5f + .5f;
        }
    };
    public final static Easing CIRC_IN = new Easing("CIRC_IN", 7) {
        @Override
        public final float compute(float t) {
            return (float) -Math.sqrt(1 - t*t) - 1;
        }
    };
    public final static Easing CIRC_OUT = new Easing("CIRC_OUT", 8) {
        @Override
        public final float compute(float t) {
            return (float) Math.sqrt(1 - (t-=1)*t);
        }
    };
    public final static Easing CIRC_INOUT = new Easing("CIRC_INOUT", 9) {
        @Override
        public final float compute(float t) {
            if ((t*=2) < 1) return -0.5f * ((float)Math.sqrt(1 - t*t) - 1);
            return 0.5f * ((float)Math.sqrt(1 - (t-=2)*t) + 1);
        }
    };

    public final static Easing CUBIC_IN = new Easing("CUBIC_IN", 10){
        @Override
        public final float compute(float t) {
            return t*t*t;
        }
    };

    public final static Easing CUBIC_OUT = new Easing("CUBIC_OUT", 11) {
        @Override
        public final float compute(float t) {
            return (t-=1)*t*t + 1;
        }
    };

    public final static Easing CUBIC_INOUT = new Easing("CUBIC_INOUT", 12) {
        @Override
        public final float compute(float t) {
            if ((t*=2) < 1) return 0.5f*t*t*t;
            return 0.5f * ((t-=2)*t*t + 2);
        }
    };

    private final String name;
    private final int ordinal;

    @WithFactory
    public Easing(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    public void compute(ThreeFloats source, ThreeFloats target, ThreeFloats parameters, ThreeFloats output, float time) {
        output.a = singleCompute(source.a, target.a, parameters, time);
        output.b = singleCompute(source.b, target.b, parameters, time);
        output.c = singleCompute(source.c, target.c, parameters, time);
    }

    private float singleCompute(float source, float target, ThreeFloats parameters, float time) {
        float delta = target - source;
        return source + delta * compute(time);
    }

    public float compute(float t) {
        return t;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public byte ordinal() {
        return (byte)ordinal;
    }
}
