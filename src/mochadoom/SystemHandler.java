package mochadoom;

import doom.CVarManager;
import doom.ConfigManager;
import doom.DoomMain;
import doom.event_t;
import s.IMusic;
import s.ISoundDriver;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
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
    }

}
