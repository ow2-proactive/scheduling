/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package doc;

/**
 * @author The ProActive Team
 * @version 1.0
 * @since   ProActive 2.2
 */
public class ReplaceStringInHTML {
    String name;
    static String replace;
    static String replace_with;

    public static void main(String[] args) throws java.io.IOException {
        if (args.length < 3) {
            System.out
                    .println("there should be 3 args: directory, string_to_replace, string_to_replace_with");
            System.exit(-1);
        }
        replace = args[1];
        replace_with = args[2];
        java.io.File sourceDir = new java.io.File(args[0]);
        filter(sourceDir);
    }

    private static void filterFile(java.io.File file) throws java.io.IOException {
        String name = file.getName();
        if (!name.endsWith(".html")) {
            return;
        }
        System.out.println("Processing " + file);
        byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
        String html = new String(b);
        if (html.indexOf(replace) >= 0) {
            System.out.println("Replacing " + replace + " by " + replace_with);
            html = html.replaceAll(replace, replace_with);

            b = html.getBytes();
            java.io.File newFile = new java.io.File(file.getParentFile(), name);
            newFile.delete();
            java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(newFile));
            out.write(b, 0, b.length);
            out.flush();
            out.close();
        }
    }

    /**
     * change img src="... .gif" to img src="..._pdf.gif"
     */
    private static void filter(java.io.File file) throws java.io.IOException {
        java.io.File[] listFiles = file.listFiles();
        if (listFiles == null) {
            return;
        }
        for (int i = 0; i < listFiles.length; i++) {
            java.io.File fileItem = listFiles[i];
            if (fileItem.isFile()) {
                filterFile(fileItem);
            }
        }
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in) throws java.io.IOException {
        java.io.DataInputStream din = new java.io.DataInputStream(in);
        byte[] bytecodes = new byte[in.available()];
        try {
            din.readFully(bytecodes);
        } finally {
            if (din != null) {
                din.close();
            }
        }
        return bytecodes;
    }
}
