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
package trywithcatch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

import trywithcatch.java_cup.runtime.Symbol;


public class TryWithCatch {

    /* Whether to backup the original file */
    private static boolean backup = true;

    /* Helper */
    private static void close(OutputStream os) {
        if (os == null) {
            return;
        }

        try {
            os.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private static List getParseData(String filename) {
        FileInputStream fis = null;
        Symbol s;

        try {
            fis = new FileInputStream(filename);
        } catch (IOException ioe) {
            System.out.println(filename + ": " + ioe);
            return null;
        }

        try {
            s = new parser(new Yylex(fis)).parse();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        List parsed = (List) s.value;

        /*
        java.util.Iterator i = parsed.iterator();
        while (i.hasNext()) {
            Anything a = (Anything) i.next();
            a.prettyPrint();
        }

        //*/
        return parsed;
    }

    private static void catcher(String filename) {
        List parsed = getParseData(filename);
        if (parsed == null) {
            return;
        }

        File tmp;
        OutputStream tmpOut = null;
        Catcher ca;

        try {
            tmp = File.createTempFile("trywithcatch_",
                    "_" + new File(filename).getName());
            tmpOut = new BufferedOutputStream(new FileOutputStream(tmp));
            ca = new Catcher(filename, tmpOut, parsed);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return;
        }

        ca.work();
        close(tmpOut);

        if (backup) {
            if (!new File(filename).renameTo(new File(filename + "~"))) {
                System.out.println("Cannot backup " + filename);
                return;
            }
        }

        try {
            FileChannel srcChannel = new FileInputStream(tmp).getChannel();
            FileChannel dstChannel = new FileOutputStream(filename).getChannel();

            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

            srcChannel.close();
            dstChannel.close();

            tmp.delete();
        } catch (IOException e) {
            System.out.println("Cannot move " + tmp + " to " + filename);
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(
                "Usage: TryWithCatch [-fullname] [-nobackup] FILES");
            System.out.println("With the -fullname option every added call");
            System.out.println("is prefixed with the full package:");
            System.out.println(TryCatch.PACKAGE);
            return;
        }

        int firstArg = 0;
        for (int i = 0; i < args.length; i++) {
            if ("-fullname".equals(args[i])) {
                TryCatch.setAddPackageName(true);
            } else if ("-nobackup".equals(args[i])) {
                backup = false;
            } else {
                firstArg = i;
                break;
            }
        }

        for (int i = firstArg; i < args.length; i++) {
            catcher(args[i]);
        }
    }
}
