package awt;

import doom.CommandVariable;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import static java.awt.RenderingHints.KEY_ALPHA_INTERPOLATION;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.KEY_COLOR_RENDERING;
import static java.awt.RenderingHints.KEY_RENDERING;
import static java.awt.RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_OFF;
import static java.awt.RenderingHints.VALUE_COLOR_RENDER_SPEED;
import static java.awt.RenderingHints.VALUE_RENDER_SPEED;
import java.util.function.Supplier;
import java.util.logging.Level;
import mochadoom.Logger;
import javax.swing.JFrame;
import mochadoom.Engine;
import mochadoom.SystemHandler;

/**
 * Common code for Doom's video frames
 */
public class DoomFrame<Window extends Component & DoomWindow<Window>> extends JFrame implements FullscreenOptions {

    private static final Logger LOGGER = Logger.getLogger(DoomFrame.class.getName());

    private static final long serialVersionUID = -4130528877723831825L;

    /**
     * Canvas or JPanel
     */
    private final Window content;

    /**
     * Graphics to draw image on
     */
    private volatile Graphics2D g2d;

    /**
     * Provider of video content to display
     */
    final Supplier<? extends Image> imageSupplier;

    /**
     * Default window size. It might change upon entering full screen, so don't consider it absolute. Due to letter
     * boxing and screen doubling, stretching etc. it might be different that the screen buffer (typically, larger).
     */
    final Dimension dim;

    /**
     * Very generic JFrame. Along that it only initializes various properties of Doom Frame.
     */
    DoomFrame(Dimension dim, Window content, Supplier<? extends Image> imageSupplier) throws HeadlessException {
        this.dim = dim;
        this.content = content;
        this.imageSupplier = imageSupplier;
        init();
    }

    /**
     * Initialize properties
     */
    private void init() {
        /**
         * This should fix Tab key
         *  - Good Sign 2017/04/21
         */
        setFocusTraversalKeysEnabled(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle(Engine.getEngine().getWindowTitle(0));
    }

    public void turnOn() {
        add(content);
        content.setFocusTraversalKeysEnabled(false);
        if (content instanceof Container) {
            setContentPane((Container) content);
        } else {
            getContentPane().setPreferredSize(content.getPreferredSize());
        }

        setResizable(false);

        /**
         * Set it to be later then setResizable to avoid extra space on right and bottom
         *  - Good Sign 2017/04/09
         *
         * JFrame's size is auto-set here.
         */
        pack();

        // center frame
        setLocationRelativeTo(null);

        setVisible(true);

        // Gently tell the eventhandler to wake up and set itself.
        requestFocus();
        content.requestFocusInWindow();
    }

    /**
     * Uninitialize graphics, so it can be reset on the next repaint
     */
    public void renewGraphics() {
        final Graphics2D localG2d = g2d;
        g2d = null;
        if (localG2d != null) {
            localG2d.dispose();
        }
    }

    /**
     * Modified update method: no context needs to passed.
     * Will render only internal screens.
     */
    public void update() {
        if (!content.isDisplayable()) {
            return;
        }

        /**
         * Work on a local copy of the stack - global one can become null at any moment
         */
        final Graphics2D localG2d = getGraphics2D();

        /**
         * If the game starts too fast, it is possible to raise an exception there
         * We don't want to bother player with "something bad happened"
         * but we wouldn't just be quiet either in case of "something really bad happened"
         * - Good Sign 2017/04/09
         */
        if (localG2d == null) {
            LOGGER.log(Level.INFO, "Starting or switching fullscreen, have no Graphics2d yet, skipping paint");
        } else {
            draw(g2d, imageSupplier.get(), dim, this);
            //if (showFPS) {
                ++frames;
                final long now = System.currentTimeMillis();
                final long lambda = now - lastTime;
                if (lambda >= 100L) {
                    setTitle(Engine.getEngine().getWindowTitle(frames * 1000.0 / lambda));
                    frames = 0;
                    lastTime = now;
                }
            //}
        }
    }

    /**
     * Techdemo v1.3: Mac OSX fix, compatible with Windows and Linux.
     * Should probably run just once. Overhead is minimal
     * compared to actually DRAWING the stuff.
     */
    private Graphics2D getGraphics2D() {
        Graphics2D localG2d;
        if ((localG2d = g2d) == null) {
            // add double-checked locking
            synchronized (DoomFrame.class) {
                if ((localG2d = g2d) == null) {
                    g2d = localG2d = (Graphics2D) content.getGraphics();
                    localG2d.setRenderingHint(KEY_ALPHA_INTERPOLATION, VALUE_ALPHA_INTERPOLATION_SPEED);
                    localG2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_OFF);
                    localG2d.setRenderingHint(KEY_RENDERING, VALUE_RENDER_SPEED);
                    localG2d.setRenderingHint(KEY_COLOR_RENDERING, VALUE_COLOR_RENDER_SPEED);

                    // add fullscreen interpolation options
                    applyFullscreenOptions(localG2d);
                }
            }
        }

        return localG2d;
    }

    private final boolean showFPS = SystemHandler.instance.getCvars().bool(CommandVariable.SHOWFPS);
    private long lastTime = System.currentTimeMillis();
    private int frames = 0;
}