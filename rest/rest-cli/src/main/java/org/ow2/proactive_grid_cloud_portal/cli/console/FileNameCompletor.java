/*
 * Copyright (c) 2002-2012, the original author or authors.
 *
 * This software is distributable under the BSD license. See the terms of the
 * BSD license in the documentation provided with this software.
 *
 * http://www.opensource.org/licenses/bsd-license.php
 */

package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.*;
import java.util.*;

import jline.console.completer.Completer;


/**
 *  A file name completor takes the buffer and issues a list of
 *  potential completions.
 *
 *  <p>
 *  This completor tries to behave as similar as possible to
 *  <i>bash</i>'s file name completion (using GNU readline)
 *  with the following exceptions:
 *
 *  <ul>
 *  <li>Candidates that are directories will end with "/"</li>
 *  <li>Wildcard regular expressions are not evaluated or replaced</li>
 *  <li>The "~" character can be used to represent the user's home,
 *  but it cannot complete to other users' homes, since java does
 *  not provide any way of determining that easily</li>
 *  </ul>
 *
 *  <p>TODO:</p>
 *  <ul>
 *  <li>Handle files with spaces in them</li>
 *  <li>Have an option for file type color highlighting</li>
 *  </ul>
 *
 *  @author  <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public class FileNameCompletor implements Completer {

    // TODO: Handle files with spaces in them

    private static final boolean OS_IS_WINDOWS;

    static {
        String os = System.getProperty("os.name").toLowerCase();
        OS_IS_WINDOWS = os.contains("win");
    }

    public int complete(String buf, final int cursor, final List candidates) {
        if (buf == null) {
            buf = "";
        }

        if (OS_IS_WINDOWS) {
            buf = buf.replace('/', '\\');
        }

        String translated = buf;

        File homeDir = getUserHome();

        // Special character: ~ maps to the user's home directory
        if (translated.startsWith("~" + File.separator)) {
            translated = homeDir.getPath() + translated.substring(1);
        } else if (translated.startsWith("~")) {
            translated = homeDir.getParentFile().getAbsolutePath();
        } else if (!(translated.startsWith(File.separator))) {
            String cwd = getUserDir().getAbsolutePath();
            translated = cwd + File.separator + translated;
        }

        File file = new File(translated);
        final File dir;

        if (translated.endsWith(separator())) {
            dir = file;
        } else {
            dir = file.getParentFile();
        }

        File[] entries = dir == null ? new File[0] : dir.listFiles();

        try {
            return matchFiles(buf, translated, entries, candidates);
        } finally {
            // we want to output a sorted list of files
            sortFileNames(candidates);
        }
    }

    protected String separator() {
        return File.separator;
    }

    protected File getUserHome() {
        return new File(System.getProperty("user.home"));
    }

    protected File getUserDir() {
        return new File(".");
    }

    protected void sortFileNames(final List fileNames) {
        Collections.sort(fileNames);
    }

    /**
     *  Match the specified <i>buffer</i> to the array of <i>entries</i>
     *  and enter the matches into the list of <i>candidates</i>. This method
     *  can be overridden in a subclass that wants to do more
     *  sophisticated file name completion.
     *
     *  @param        buffer                the untranslated buffer
     *  @param        translated        the buffer with common characters replaced
     *  @param        files                the list of files to match
     *  @param        candidates        the list of candidates to populate
     *
     *  @return  the offset of the match
     */
    protected int matchFiles(final String buffer, final String translated, final File[] files,
            final List<CharSequence> candidates) {
        if (files == null) {
            return -1;
        }

        int matches = 0;

        // first pass: just count the matches

        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                matches++;
            }
        }
        for (File file : files) {
            if (file.getAbsolutePath().startsWith(translated)) {
                CharSequence name = file.getName() + (matches == 1 && file.isDirectory() ? "/" : " ");
                candidates.add(render(file, name).toString());
            }
        }

        final int index = buffer.lastIndexOf(separator());

        return index + separator().length();
    }

    protected CharSequence render(final File file, final CharSequence name) {
        return name;
    }
}
