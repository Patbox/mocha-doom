package w;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mochadoom.SystemHandler;
import utils.C2JUtils;

/**
 * As we know, Java can be a bit awkward when handling streams e.g. you can't
 * really skip at will without doing some nasty crud. This class helps doing
 * such crud. E.g. if we are dealing with a stream that has an underlying file,
 * we can try and skip directly by using the file channel, otherwise we can try
 * (eww) closing the stream, reopening it (ASSUMING WE KNOW THE SOURCE'S URI AND
 * TYPE), and then skipping.
 *
 * @author Maes
 */
public class InputStreamSugar {

    public static final int UNKNOWN_TYPE = 0x0;

    public static final int FILE = 0x1; // Local file. Easiest case

    public static final int NETWORK_FILE = 0x2;

    public static final int ZIP_FILE = 0x4; // Zipped file

    public static final int BAD_URI = -1; // Bad or unparseable

    /**
     * Creates an inputstream from a local file, network resource, or zipped
     * file (also over a network). If an entry name is specifid AND the type is
     * specified to be zip, then a zipentry with that name will be sought.
     *
     * @param resource
     * @param contained
     * @param type
     * @return
     */
    public static final InputStream createInputStreamFromURI(String resource,
            ZipEntry entry, int type) {
        return SystemHandler.instance.createInputStreamFromURI(resource, entry, type);
    }

    /** Match zip entries in a ZipInputStream based only on their name.
     * Luckily (?) ZipEntries do not keep references to their originating
     * streams, so opening/closing ZipInputStreams all the time won't result
     * in a garbage hell...I hope.
     *
     * @param zis
     * @param entryname
     * @return
     */
    public static InputStream getZipEntryStream(ZipInputStream zis, String entryname) {

        ZipEntry ze = null;
        try {
            while ((ze = zis.getNextEntry()) != null) {
                // Directories cannot be opened
                if (ze.isDirectory()) {
                    continue;
                }

                if (ze.getName().equals(entryname)) {
                    return zis;
                }
            }
        } catch (IOException e) {
            // Get jack
            return null;
        }

        // Get jack
        return null;
    }

    private final static InputStream getDirectInputStream(String resource) {
        return SystemHandler.instance.getDirectInputStream(resource);
    }

    /**
     * Attempt to do the Holy Grail of Java Streams, aka seek to a particular
     * position. With some types of stream, this is possible if you poke deep
     * enough. With others, it's not, and you can only close & reopen them
     * (provided you know how to do that) and then skip to a particular position
     *
     * @param is
     * @param pos
     *        The desired position
     * @param uri
     *        Information which can help reopen a stream, e.g. a filename, URL,
     *        or zip file.
     * @peram entry If we must look into a zipfile entry
     * @return the skipped stream. Might be a totally different object.
     * @throws IOException
     */
    public static final InputStream streamSeek(InputStream is, long pos,
            long size, String uri, ZipEntry entry, int type)
            throws IOException {
        return SystemHandler.instance.streamSeek(is, pos, size, uri, entry, type);
    }

    public static List<ZipEntry> getAllEntries(ZipInputStream zis)
            throws IOException {
        ArrayList<ZipEntry> zes = new ArrayList<>();

        ZipEntry z;

        while ((z = zis.getNextEntry()) != null) {
            zes.add(z);
        }

        return zes;
    }

    /** Attempts to return a stream size estimate. Only guaranteed to work 100%
     * for streams representing local files, and zips (if you have the entry).
     *
     * @param is
     * @param z
     * @return
     */
    public static long getSizeEstimate(InputStream is, ZipEntry z) {
        if (is instanceof FileInputStream) {
            try {
                return ((FileInputStream) is).getChannel().size();
            } catch (IOException e) {

            }
        }

        if (is instanceof FileInputStream) {
            if (z != null) {
                return z.getSize();
            }
        }

        // Last ditch
        try {
            return is.available();
        } catch (IOException e) {
            try {
                return is.available();
            } catch (IOException e1) {
                return -1;
            }
        }
    }

}