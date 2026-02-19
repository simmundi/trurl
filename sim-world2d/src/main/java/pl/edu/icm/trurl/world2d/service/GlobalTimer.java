package pl.edu.icm.trurl.world2d.service;


import net.snowyhollows.bento.annotation.ImplementationSwitch;

@ImplementationSwitch(
        configKey = "trurl.world2d.globalTimer",
        cases = {
                @ImplementationSwitch.When(name = "default", implementation = DefaultGlobalTimer.class, useByDefault = true)
        }
)
public interface GlobalTimer {
    double getTotalTimePassed();
}
