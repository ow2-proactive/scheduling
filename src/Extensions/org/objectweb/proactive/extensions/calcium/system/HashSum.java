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
package org.objectweb.proactive.extensions.calcium.system;

import java.io.*;
import java.security.*;


public class HashSum {
    static String DEFAULT_HASH_ALGO = "MD5";

    public static String md5sum(File file) throws NoSuchAlgorithmException, IOException {
        return hashsum(file, "MD5");
    }

    public static String sha1sum(File file) throws NoSuchAlgorithmException, IOException {
        return hashsum(file, "SHA-1");
    }

    public static String hashsum(File file) throws NoSuchAlgorithmException, IOException {
        return hashsum(file, DEFAULT_HASH_ALGO);
    }

    public static String hashsum(File file, String encoding) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(file);
        MessageDigest md = MessageDigest.getInstance(encoding);

        try {
            DigestInputStream dis = new DigestInputStream(fis, md);
            byte[] buffer = new byte[8192];
            while (dis.read(buffer) != -1)
                ;
        } finally {
            fis.close();
        }

        byte[] bDigest = md.digest();
        String res = "";
        for (byte b : bDigest) {
            res += Integer.toHexString(b & 0xFF);
        }

        return res;
    }

    public static void main(String[] arg) throws Exception {
        String a = sha1sum(new File("/home/mleyton/Tutorial-ProActive-SkeletonForMarioToImprove.ppt"));
        System.out.println(a);
    }
}
