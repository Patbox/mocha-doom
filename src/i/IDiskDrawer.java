package i;

public interface IDiskDrawer extends IDrawer {
    public static IDiskDrawer NOOP = new IDiskDrawer() {
        @Override
        public void setReading(int reading) {

        }

        @Override
        public boolean isReading() {
            return false;
        }

        @Override
        public void Init() {

        }

        @Override
        public boolean justDoneReading() {
            return false;
        }

        @Override
        public void Drawer() {

        }
    };


    /**
     * Set a timeout (in tics) for displaying the disk icon
     *
     * @param reading
     */
    void setReading(int reading);

    /**
     * Disk displayer is currently active
     *
     * @return
     */
    boolean isReading();

    /**
     * Only call after the Wadloader is instantiated and initialized itself.
     *
     */
    void Init();

    /**
     * Status only valid after the last tic has been drawn. Use to know when to redraw status bar.
     *
     * @return
     */
    boolean justDoneReading();

}