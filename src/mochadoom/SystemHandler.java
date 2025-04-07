package mochadoom;

import doom.CVarManager;
import doom.ConfigManager;
import doom.DoomMain;
import doom.event_t;
import s.IMusic;
import s.ISoundDriver;
import v.DoomGraphicSystem;
import v.renderers.RendererFactory;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.util.zip.ZipEntry;

public class SystemHandler {
    public static Impl instance = null;

    public interface Impl {
        boolean allowSaves ();
        int guessResourceType (String uri);
        boolean testReadAccess (String uri);
        boolean testWriteAccess (String uri);
        void systemExit ( int code);
        CVarManager getCvars ();
        ConfigManager getConfig ();
        void updateFrame ();
        IMusic chooseMusicModule (CVarManager CVM);
        ISoundDriver chooseSoundModule (DoomMain<?, ?> DM, CVarManager CVM);
        InputStream createInputStreamFromURI (String resource, ZipEntry entry,int type);
        InputStream getDirectInputStream (String resource);
        InputStream streamSeek (InputStream is,long pos, long size, String uri, ZipEntry entry,int type) throws
        IOException;
        void resetIn (event_t.mouseevent_t mouseeventT, Robot robot, Point windowOffset, int centreX, int centreY);
        GraphicsConfiguration getGraphicsConfiguration ();

        boolean fileExists(String file);

        BufferedReader getFileBufferedReader(String file, Charset charset) throws IOException;

        BufferedWriter getFileBufferedWriter(String file, Charset charset, OpenOption[] options) throws IOException;

        default void mainLoopStart() {};
        default void mainLoopEnd() {};
        default void mainLoopPostTic() {};

        default <T, V> DoomGraphicSystem<T,V> createGraphicsSystem(DoomMain<T,V> doomMain) {
            return RendererFactory.<T, V>newBuilder()
                    .setVideoScale(doomMain.vs).setBppMode(doomMain.bppMode).setWadLoader(doomMain.wadLoader)
                    .build();
        };

        default InputStream getSaveDataInputStream(String name) throws IOException {
            throw new FileNotFoundException("getSaveDataInputStream not implemented!");
        };

        default OutputStream getSaveDataOutputStream(String name) throws IOException {
            throw new IOException("getSaveDataOutputSteam not implemented!");
        }

        default boolean generateAlert(String title, String cause, boolean showCancelButton) {
            return false;
        }

        default Logger getLogger(String className) {
            return Logger.NOOP;
        }
    }

}
