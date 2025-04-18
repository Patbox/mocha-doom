package timing;

import doom.CVarManager;
import doom.CommandVariable;

import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

public class RemoteTicker implements ITicker {
    private final Object tickerLock = new Object();
    private final GameTick[] ticker = new GameTick[32];
    private final ITicker timingTicker;
    private final SleepType sleepType;
    private int current = -1;
    private int currentTime = 0;
    private long tickStart = -1;
    private final int accuracy;

    public RemoteTicker(ITicker timingTicker, CVarManager cvars) {
        this.timingTicker = timingTicker;
        this.accuracy = cvars.get(CommandVariable.TICKERACCURACY, Integer.class, 0).orElse(10_000);
        this.sleepType = cvars.get(CommandVariable.SLEEPTYPE, SleepType.class, 0).orElse(SleepType.DEFAULT);
    }

    public void push(GameTick tick) {
        this.ticker[++current] = tick;
    }

    public void pop() {
        this.ticker[current--] = null;
    }

    public void tick() throws IOException {
         this.ticker[current].tick();
         this.tickTime();
    }

    public boolean hasTick() {
        return this.current != -1;
    }

    public void tickTime() {
        currentTime++;
    }

    @Override
    public int GetTime() {
        return currentTime;
    }

    public void waitForNextTick() {
        switch (this.sleepType) {
            case BUSY -> {
                var currentTick = this.timingTicker.GetTime();
                while (currentTick == this.timingTicker.GetTime()) {
                    Thread.onSpinWait();
                }
            }
            case SLEEP -> {
                var delta = System.nanoTime() - this.tickStart;
                try {
                    var sleepTime = Math.max(28_571_428 - delta, 2_000_000);

                    Thread.sleep(sleepTime / 1_000_000, (int) (sleepTime % 1_000_000));
                } catch (InterruptedException ignored) {}
            }
            default -> {
                var currentTick = this.timingTicker.GetTime();
                while (currentTick == this.timingTicker.GetTime()) {
                    LockSupport.parkNanos(this.tickerLock, this.accuracy);
                }
            }
        }
    }

    public void tickStart() {
        this.tickStart = System.nanoTime();
    }

    public enum SleepType {
        DEFAULT,
        CHECKED_LOCK,
        SLEEP,
        BUSY
    }
}
