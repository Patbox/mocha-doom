package mochadoom;

import doom.*;
import s.*;
import utils.C2JUtils;
import w.InputStreamSugar;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public record DefaultSystemHandler() implements SystemHandler.Impl {
    @Override
    public boolean allowSaves() {
        return true;
    }

    @Override
    public int guessResourceType(String uri) {
        int result = 0;
        InputStream in;

        // This is bullshit.
        if (uri == null || uri.length() == 0) {
            return InputStreamSugar.BAD_URI;
        }

        try {
            in = new FileInputStream(new File(uri));
            // It's a file
            result |= InputStreamSugar.FILE;
        } catch (FileNotFoundException e) {
            // Not a file...
            URL u;
            try {
                u = new URI(uri).toURL();
            } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e1) {
                return InputStreamSugar.BAD_URI;
            }
            try {
                in = u.openConnection().getInputStream();
                result |= InputStreamSugar.NETWORK_FILE;
            } catch (IOException e1) {
                return InputStreamSugar.BAD_URI;
            }

        }

        // Try guessing if it's a ZIP file. A bit lame, really
        // TODO: add proper validation, and maybe MIME type checking
        // for network streams, for cases that we can't really
        // tell from extension alone.
        if (C2JUtils.checkForExtension(uri, "zip")) {
            result |= InputStreamSugar.ZIP_FILE;

        }

        try {
            in.close();
        } catch (IOException e) {

        }

        // All is well. Go on...
        return result;
    }

    @Override
    public boolean testReadAccess(String uri) {
        InputStream in;

        // This is bullshit.
        if (uri == null) {
            return false;
        }
        if (uri.length() == 0) {
            return false;
        }

        try {
            in = new FileInputStream(uri);
        } catch (FileNotFoundException e) {
            // Not a file...
            URL u;
            try {
                u = new URI(uri).toURL();
            } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e1) {
                return false;
            }
            try {
                in = u.openConnection().getInputStream();
            } catch (IOException e1) {
                return false;
            }

        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {

            }
            return true;
        }
        // All is well. Go on...
        return true;
    }

    @Override
    public boolean testWriteAccess(String uri) {
        OutputStream out;

        // This is bullshit.
        if (uri == null) {
            return false;
        }
        if (uri.length() == 0) {
            return false;
        }

        try {
            out = new FileOutputStream(uri);
        } catch (FileNotFoundException e) {
            // Not a file...
            URL u;
            try {
                u = new URI(uri).toURL();
            } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e1) {
                return false;
            }
            try {
                out = u.openConnection().getOutputStream();
            } catch (IOException e1) {
                return false;
            }

        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {

            }
            return true;
        }
        // All is well. Go on...
        return true;
    }

    @Override
    public void systemExit(int code) {
        System.exit(code);
    }

    @Override
    public CVarManager getCvars() {
        return Engine._getCVM();
    }

    @Override
    public ConfigManager getConfig() {
        return Engine._getConfig();
    }

    @Override
    public void updateFrame() {
        Engine.updateFrame();
    }

    @Override
    public IMusic chooseMusicModule(CVarManager CVM) {
        if (CVM.bool(CommandVariable.NOMUSIC) || CVM.bool(CommandVariable.NOSOUND)) {
            return new DummyMusic();
        } else {
            return new DavidMusicModule();
        }
    }

    @Override
    public ISoundDriver chooseSoundModule(DoomMain<?, ?> DM, CVarManager CVM) {
        final ISoundDriver driver;
        if (CVM.bool(CommandVariable.NOSFX) || CVM.bool(CommandVariable.NOSOUND)) {
            driver = new DummySFX();
        } else {
            // Switch between possible sound drivers.
            if (CVM.bool(CommandVariable.AUDIOLINES)) { // Crudish.
                driver = new DavidSFXModule(DM, DM.numChannels);
            } else if (CVM.bool(CommandVariable.SPEAKERSOUND)) { // PC Speaker emulation
                driver = new SpeakerDoomSoundDriver(DM, DM.numChannels);
            } else if (CVM.bool(CommandVariable.CLIPSOUND)) {
                driver = new ClipSFXModule(DM, DM.numChannels);
            } else if (CVM.bool(CommandVariable.CLASSICSOUND)) { // This is the default
                driver = new ClassicDoomSoundDriver(DM, DM.numChannels);
            } else { // This is the default
                driver = new SuperDoomSoundDriver(DM, DM.numChannels);
            }
        }
        // Check for sound init failure and revert to dummy
        if (!driver.InitSound()) {
            ISoundDriver.LOGGER.log(Level.WARNING, "S_InitSound: failed. Reverting to dummy...");
            return new DummySFX();
        }
        return driver;
    }

    public InputStream createInputStreamFromURI(String resource, ZipEntry entry, int type) {

        InputStream is = null;
        URL u;

        // No entry specified or no zip type, try everything BUT zip.
        if (entry == null || !C2JUtils.flags(type, InputStreamSugar.ZIP_FILE)) {
            is = getDirectInputStream(resource);
        } else {
            // Entry specified AND type specified to be zip
            // We might want to open even a zip file without looking
            // for any particular entry.
            if (entry != null && C2JUtils.flags(type, InputStreamSugar.ZIP_FILE)) {

                ZipInputStream zis;
                // Try it as a NET zip file
                try {
                    u = new URI(resource).toURL();
                    zis = new ZipInputStream(u.openStream());
                } catch (Exception e) {
                    // Local zip file?
                    try {
                        // Open resource as local file-backed zip input stream,
                        // and search proper entry.
                        zis = new ZipInputStream(new FileInputStream(resource));
                    } catch (Exception e1) {
                        // Well, it's not that either.
                        // At this point we almost ran out of options
                        // Try a local file and that's it.
                        is = getDirectInputStream(resource);
                        return is;
                    }
                }

                // All OK?
                is = InputStreamSugar.getZipEntryStream(zis, entry.getName());
                if (is != null) {
                    return is;
                }
            }
        }

        // At this point, you'll either get a stream or jack.
        return getDirectInputStream(resource);
    }

    public InputStream getDirectInputStream(String resource) {
        InputStream is = null;
        URL u;

        try { // Is it a net resource?
            u = new URI(resource).toURL();
            is = u.openStream();
        } catch (Exception e) {
            // OK, not a valid URL or no network. We don't care.
            // Try opening as a local file.
            try {
                is = new FileInputStream(resource);
            } catch (FileNotFoundException e1) {
                // Well, it's not that either.
                // At this point we really ran out of options
                // and you'll get null
            }
        }

        return is;    }

    public InputStream streamSeek(InputStream is, long pos, long size, String uri, ZipEntry entry, int type) throws IOException {
        if (is == null) {
            return is;
        }

        // If we know our actual position in the stream, we can aid seeking
        // forward

        /*
         * Too buggy :-/ pity if (knownpos>=0 && knownpos<=pos){ if
         * (pos==knownpos) return is; try{ final long mustskip=pos-knownpos;
         * long skipped=0; while (skipped<mustskip){
         * skipped+=is.skip(mustskip-skipped);
         * System.out.printf("Must skip %d skipped %d\n",mustskip,skipped); }
         * return is; } catch (Exception e){ // We couldn't skip cleanly.
         * Swallow up and try normally. System.err.println("Couldn't skip"); } }
         */
        // This is a more reliable method, although it's less than impressive in
        // results.
        if (size > 0) {
            try {
                long available = is.available();
                long guesspos = size - available;
                // The stream is at a position before or equal to
                // our desired one. We can attempt skipping forward.
                if (guesspos > 0 && guesspos <= pos) {
                    long skipped = 0;
                    long mustskip = pos - guesspos;
                    // Repeat skipping until proper amount reached
                    while (skipped < mustskip) {
                        skipped += is.skip(mustskip - skipped);
                    }
                    return is;
                }
            } catch (Exception e) {
                // We couldn't skip cleanly. Swallow up and try normally.
            }
        }

        // Cast succeeded
        if (is instanceof FileInputStream) {
            try {
                ((FileInputStream) is).getChannel().position(pos);
                return is;
            } catch (IOException e) {
                // Ouch. Do a dumb close & reopening.
                is.close();
                is = createInputStreamFromURI(uri, null, 1);
                is.skip(pos);
                return is;
            }
        }

        // Cast succeeded
        if (is instanceof ZipInputStream) {
            // ZipInputStreams are VERY dumb. so...
            is.close();
            is = createInputStreamFromURI(uri, entry, type);
            is.skip(pos);
            return is;

        }

        try { // Is it a net resource? We have to reopen it :-/
            // long a=System.nanoTime();
            URL u = new URI(uri).toURL();
            InputStream nis = u.openStream();
            nis.skip(pos);
            is.close();
            // long b=System.nanoTime();
            // System.out.printf("Network stream seeked WITH closing %d\n",(b-a)/1000);
            return nis;
        } catch (Exception e) {

        }

        // TODO: zip handling?
        return is;
    }

    @Override
    public void resetIn(event_t.mouseevent_t mouseeventT, Robot robot, Point windowOffset, int centreX, int centreY) {
        // Mark that the next event will be from robot
        mouseeventT.robotMove = true;

        // Move the mouse to the window center
        robot.mouseMove(windowOffset.x + centreX, windowOffset.y + centreY);
    }

    @Override
    public GraphicsConfiguration getGraphicsConfiguration() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice().getDefaultConfiguration();
    }

    @Override
    public boolean fileExists(String file) {
        return Files.exists(FileSystems.getDefault().getPath(file));
    }

    @Override
    public BufferedReader getFileBufferedReader(String file, Charset charset) throws IOException {
        return Files.newBufferedReader(FileSystems.getDefault().getPath(file), charset);
    }

    @Override
    public BufferedWriter getFileBufferedWriter(String file, Charset charset, OpenOption[] options) throws IOException {
        return Files.newBufferedWriter(FileSystems.getDefault().getPath(file), charset, options);
    }
}
