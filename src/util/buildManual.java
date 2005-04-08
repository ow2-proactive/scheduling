/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive-support@inria.fr
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package util;

import java.io.File;
import java.io.FileOutputStream;


/**
 * @author ProActiveTeam
 * @version 1.0, 21 mars 2005
 * @since ProActive 2.2
 *
 */
public class buildManual {
    static FileOutputStream all;
    static String parent;

    public static void main(String[] args) throws java.io.IOException {
        if (args.length == 0) {
            System.out.println("Missing target files");
            System.exit(-1);
        }
        parent = new File(args[0]).getParent();
        File allFile;
        if (System.getProperty("proactive.guidedtour") != null) {
            allFile = new File(parent, "GuidedTour.html");
        } else {
            allFile = new File(parent, "Manual.html");
        }
        allFile.delete();
        all = new FileOutputStream(allFile, true);

        for (int i = 0; i < args.length; i++) {
            filterFile(new File(args[i]));
        }
        all.close();
    }

    private static void filterFile(java.io.File file)
        throws java.io.IOException {
        System.out.println("Processing " + file);
        byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
        String html = new String(b);

        html = removeMarkedLine(html);
        html = html + ("\n <p class=\"print\"/>");
        html = changeImagesPath(html, file.getParent());

        b = html.getBytes();

        all.write(b, 0, b.length);
        //all.flush();
    }

    /**
     * Returns an array of bytes containing the bytecodes for
     * the class represented by the InputStream
     * @param in the inputstream of the class file
     * @return the bytecodes for the class
     * @exception java.io.IOException if the class cannot be read
     */
    private static byte[] getBytesFromInputStream(java.io.InputStream in)
        throws java.io.IOException {
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

    private static String removeMarkedLine(String html) {
        StringBuffer newHtml = new StringBuffer(html.length());
        int currentIndex = 0;
        while (true) {
            int markerIndex = html.indexOf("~~~", currentIndex);
            if (markerIndex == -1) {
                break;
            }
            int endIndex = html.indexOf("-->", markerIndex);
            if (endIndex == -1) {
                break;
            }
            newHtml.append(html.substring(currentIndex, markerIndex));
            currentIndex = endIndex + 3;
        }
        if (currentIndex < html.length()) {
            newHtml.append(html.substring(currentIndex, html.length()));
        }
        return newHtml.toString();
    }

    private static String changeImagesPath(String html, String path) {
        StringBuffer newHtml = new StringBuffer(html.length());
        int currentIndex = 0;
        while (true) {
            int srcimgIndex = html.indexOf("src=\"", currentIndex);

            if (srcimgIndex == -1) {
                break;
            }
            String image_path = html.substring(srcimgIndex + 5,
                    html.indexOf("\"", srcimgIndex + 5));
            if (!new File(parent, image_path).exists() &&
                    !image_path.startsWith("http")) {
                //the path might have changed since the global file is created in the parent dir
                if (path.length() <= parent.length()) {
                    //we guess that just one level below can occur
                    image_path = "../"+image_path;
                } else {
                    String subpath = path.substring(parent.length() + 1);
//                    String subdir = subpath.substring(0,
//                            subpath.lastIndexOf("/"));
                    image_path = subpath + "/" + image_path;
                    //System.out.println(image_path);
                }
            }
            String pathWithoutExtension = image_path.substring(0,
                    image_path.lastIndexOf("."));

            String pdf_image_path = image_path.replaceFirst(pathWithoutExtension,
                    pathWithoutExtension + "_pdf");

            //System.out.println(pdf_image_path);
            //System.out.println(new File(parent, pdf_image_path).exists());
            if (new File(parent, pdf_image_path).exists()) {
                newHtml.append(html.substring(currentIndex, srcimgIndex + 5));
                newHtml.append(pdf_image_path);
            } else {
                newHtml.append(html.substring(currentIndex, srcimgIndex + 5));
                newHtml.append(image_path);
            }

            currentIndex = html.indexOf("\"", srcimgIndex + 5);
        }
        if (currentIndex < html.length()) {
            newHtml.append(html.substring(currentIndex, html.length()));
        }
        return newHtml.toString();
    }
}
