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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.javadoc.AnnotationDesc;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.DocErrorReporter;
import com.sun.javadoc.RootDoc;


public class PublishedAPIDoclet {
    static final private String OPT_FILE = "-file";

    public static boolean start(RootDoc root) {
        String fileName = readOptions(root.options());
        try {
            FileWriter filewriter = new FileWriter(new File(fileName));
            for (ClassDoc cl : root.classes()) {
                for (AnnotationDesc an : cl.annotations()) {
                    // PublicAPI is not in the Javadoc classpath
                    if ("@org.objectweb.proactive.annotation.PublicAPI".equals(an.toString())) {
                        filewriter.write(cl.qualifiedName().replace('.', '/') + ".java\n");
                        System.out.println(cl.qualifiedName() + " will be published");
                        continue;
                    }
                }
            }
            filewriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static String readOptions(String[][] options) {
        String fileName = null;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals(OPT_FILE)) {
                fileName = opt[1];
            }
        }
        return fileName;
    }

    public static int optionLength(String option) {
        if (option.equals(OPT_FILE)) {
            return 2;
        }
        return 0;
    }

    public static boolean validOptions(String options[][], DocErrorReporter reporter) {
        boolean foundTagOption = false;
        for (int i = 0; i < options.length; i++) {
            String[] opt = options[i];
            if (opt[0].equals(OPT_FILE)) {
                if (foundTagOption) {
                    reporter.printError("Only one " + OPT_FILE + " option allowed.");
                    return false;
                } else {
                    foundTagOption = true;
                }
            }
        }
        if (!foundTagOption) {
            reporter.printError("Usage: javadoc " + OPT_FILE + " mytag -doclet ListTags ...");
        }
        return foundTagOption;
    }
}
