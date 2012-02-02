/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $ACTIVEEON_INITIAL_DEV$
 */
package org.ow2.proactive.tests.performance.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class TestFileUtils {

    public static File getWritableFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IllegalArgumentException("Failed to create file " + file.getAbsolutePath());
            }
        }
        if (!file.canWrite()) {
            throw new IllegalArgumentException("Can't write to the " + file.getAbsolutePath());
        }
        return file;
    }

    public static List<String> listDirectoryJars(String dirPath) {
        File file = new File(dirPath);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Can't find dir " + file.getAbsolutePath());
        }
        String names[] = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        });
        List<String> result = new ArrayList<String>();
        for (String name : names) {
            result.add(dirPath + "/" + name);
        }
        return result;
    }

    public static void writeStringToFile(File file, String string) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
        try {
            out.write(string.getBytes());
        } finally {
            out.close();
        }
    }

    public static String readStreamToString(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
        } finally {
            reader.close();
        }

        return builder.toString();
    }

}
