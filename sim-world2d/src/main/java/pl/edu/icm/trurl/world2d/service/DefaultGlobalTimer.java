package pl.edu.icm.trurl.world2d.service;

import net.snowyhollows.bento.annotation.WithFactory;

public class DefaultGlobalTimer implements GlobalTimer {

    private long timeInMillis;

    @WithFactory
    public DefaultGlobalTimer() {
        this.timeInMillis = System.currentTimeMillis();
    }

    @Override
    public double getTotalTimePassed() {
        long currentTimeMillis = System.currentTimeMillis();
        long diff = currentTimeMillis - timeInMillis;
        return diff / 1000.0;
    }
}
