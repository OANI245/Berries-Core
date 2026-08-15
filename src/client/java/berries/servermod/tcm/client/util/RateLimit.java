package berries.servermod.tcm.client.util;

@SuppressWarnings("unused")
public class RateLimit {
    private long lastTime = 0;
    private final long interval;

    public RateLimit(double interval) {
        this.interval = (long) (interval * 1000);
    }

    public boolean shouldUpdate(long now) {
        if (now - lastTime > interval) {
            lastTime = now;
            return true;
        }
        return false;
    }

    public void resetCoolDown() {
        lastTime = 0;
    }
}