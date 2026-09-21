package me.shvaich.moremwe.utils;

public class TimerUtil {

    private final long period;
    private long lastUpdate;

    /**
     * @param period in milliseconds
     */
    public TimerUtil(long period) {
        if (period <= 0) throw new IllegalArgumentException();
        this.period = period;
        reset();
    }

    /**
     * @return {@code true} if the {@link #period} has elapsed since the last update
     */
    public boolean update() {
        final long time = System.currentTimeMillis();
        if (time - lastUpdate > period) {
            lastUpdate = time;
            return true;
        }
        return false;
    }

    public void reset() {
        this.lastUpdate = 0;
    }

    /** Can be used safely after {@link #update}, otherwise check not null */
    public String getTimeLeftText() {
        final long timePassed = System.currentTimeMillis() - lastUpdate;
        return timePassed > period ? null : getTimeText(period - timePassed);
    }

    public static String getTimeText(long time, boolean recursive, int maxUnits, String separator) {
        if (time < 0) return "N/A";
        if (time < 1000) return "0s";
        maxUnits = Math.max(maxUnits, 1);
        recursive = recursive && maxUnits != 1;
        final Object[][] data = {
                { 's', 1000L },
                { 'm', 60_000L },
                { 'h', 3_600_000L },
                { 'd', 86_400_000L },
                { 'w', 604_800_000L },
                { 'm', 2_592_000_000L }
        };

        final StringBuilder text = new StringBuilder();
        int appendCount = 0;
        long timeLeft = time;
        for (int i = data.length-1; i >= 0; i--) {
            final long unitMs = (long) data[i][1];
            if (timeLeft < unitMs) continue;
            final long n = timeLeft / unitMs;
            if (appendCount != 0) text.append(separator);
            text.append(n).append((char) data[i][0]);
            if (!recursive || ((++appendCount) == maxUnits)) break;
            timeLeft -= n * unitMs;
        }
        return text.toString();
    }

    public static String getTimeText(long time) {
        return getTimeText(time, true, Integer.MAX_VALUE, " ");
    }
}