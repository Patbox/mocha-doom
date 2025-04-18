/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import mochadoom.Logger;
import mochadoom.SystemHandler;

/**
 * Resource IO to automate read/write on configuration/resources
 *
 * @author Good Sign
 */
public class ResourceIO {

    private static final Logger LOGGER = Logger.getLogger(ResourceIO.class.getName());

    private final String file;
    private final Charset charset = Charset.forName("US-ASCII");

    public ResourceIO(final String path) {
        this.file = path;
    }

    public boolean exists() {
        return SystemHandler.instance.fileExists(file);
    }

    public boolean readLines(final Consumer<String> lineConsumer) {
        if (SystemHandler.instance.fileExists(file)) {
            try ( BufferedReader reader = SystemHandler.instance.getFileBufferedReader(file, charset)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineConsumer.accept(line);
                }

                return true;
            } catch (IOException x) {
                LOGGER.log(Level.WARNING, "ResourceIO read failure", x);
                return false;
            }
        }

        return false;
    }

    public boolean writeLines(final Supplier<String> lineSupplier, final OpenOption... options) {
        if (!SystemHandler.instance.allowSaves()) {
            return true;
        }

        try ( BufferedWriter writer = SystemHandler.instance.getFileBufferedWriter(file, charset, options)) {
            String line;
            while ((line = lineSupplier.get()) != null) {
                writer.write(line, 0, line.length());
                writer.newLine();
            }

            return true;
        } catch (IOException x) {
            LOGGER.log(Level.WARNING, "ResourceIO write failure", x);
            return false;
        }
    }

    public String getFileame() {
        return file.toString();
    }
}