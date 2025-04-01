package timing;

public class FastTicker implements ITicker {

    /**
     * I_GetTime
     * returns time in 1/70th second tics
     */
    @Override
    public int GetTime() {
        return fasttic++;
    }

    @Override
    public int getNanosUntilNextTickCheck(int target) {
        return 0;
    }

    protected volatile int fasttic = 0;
}