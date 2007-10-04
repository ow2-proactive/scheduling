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
package sources;

import java.io.File;

import java.net.URI;


public class UpdateCopyrightAndVersion {
    private static String LGPLcopyright = "/*\n" +
        " * ################################################################\n" +
        " *\n" +
        " * ProActive: The Java(TM) library for Parallel, Distributed,\n" +
        " *            Concurrent computing with Security and Mobility\n" +
        " *\n" +
        " * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis\n" +
        " * Contact: proactive@objectweb.org" + "\n" + 
        " *\n" +
        " * This library is free software; you can redistribute it and/or\n" +
        " * modify it under the terms of the GNU Lesser General Public\n" +
        " * License as published by the Free Software Foundation; either\n" +
        " * version 2.1 of the License, or any later version.\n" +
        " *\n" +
        " * This library is distributed in the hope that it will be useful,\n" +
        " * but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
        " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n" +
        " * Lesser General Public License for more details.\n" +
        " *\n" +
        " * You should have received a copy of the GNU Lesser General Public\n" +
        " * License along with this library; if not, write to the Free Software\n" +
        " * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307\n" +
        " * USA\n" + 
	" *\n" +
        " *  Initial developer(s):               The ProActive Team\n" +
        " *                        http://proactive.inria.fr/team_members.htm\n" +
        " *  Contributor(s):\n" + 
        " *\n" +
        " * ################################################################\n" +
        " */\n";
    
    
    private static String GPLcopyright = "/*\n" +
    " * ################################################################\n" +
    " *\n" +
    " * ProActive: The Java(TM) library for Parallel, Distributed,\n" +
    " *            Concurrent computing with Security and Mobility\n" +
    " *\n" +
    " * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis\n" +
    " * Contact: proactive@objectweb.org" + "\n" + 
    " *\n" +
    " * This library is free software; you can redistribute it and/or\n" +
    " * modify it under the terms of the GNU General Public License\n" +
    " * as published by the Free Software Foundation; either version\n" +
    " * 2 of the License, or any later version.\n" +
    " *\n" +
    " * This library is distributed in the hope that it will be useful,\n" +
    " * but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
    " * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n" +
    " * General Public License for more details.\n" +
    " *\n" +
    " * You should have received a copy of the GNU General Public License\n" +
    " * along with this library; if not, write to the Free Software\n" +
    " * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307\n" +
    " * USA\n" + 
" *\n" +
    " *  Initial developer(s):               The ProActive Team\n" +
    " *                        http://proactive.inria.fr/team_members.htm\n" +
    " *  Contributor(s):\n" + 
    " *\n" +
    " * ################################################################\n" +
    " */\n";
    
    private static String PatternBegin = "$$%%";
    private static String PatternEnd = "%%$$";
    private static String VersionPattern = "ProActiveVersion";
    private static String VersionUnderscorePattern = "ProActiveUnderscoreVersion";
    private static String CopyrightYearsPattern = "CopyrightYears";
    private static String CopyrightYearsShortPattern = "CopyrightYearsShort";
    private static String LastYearCopyrightPattern = "CopyrightLastYear";
    private static String LastYearShortCopyrightPattern = "CopyrightLastYearShort";
    private static String FirstYearCopyrightPattern = "CopyrightFirstYear";
    private static String FirstYearShortCopyrightPattern = "CopyrightFirstYearShort";
    private static String CurrentVersion = "3.2.1";
    private static String CurrentVersionUnderscore = "3_2_1";
    private static String CopyrightYears = "1997-2007";
    private static String CopyrightYearsShort = "97-07";
    private static String LastYearCopyright = "2007";
    private static String LastYearShortCopyright = "07";
    private static String FirstYearCopyright = "1997";
    private static String FirstYearShortCopyright = "97";
    private static String[][] replacements = {
            { VersionPattern, CurrentVersion },
            { VersionUnderscorePattern, CurrentVersionUnderscore },
            { CopyrightYearsPattern, CopyrightYears },
            { CopyrightYearsShortPattern, CopyrightYearsShort },
            { LastYearCopyrightPattern, LastYearCopyright },
            { LastYearShortCopyrightPattern, LastYearShortCopyright },
            { FirstYearCopyrightPattern, FirstYearCopyright },
            { FirstYearShortCopyrightPattern, FirstYearShortCopyright }
        };
    private static URI rootDir;
    private static File[] excludeDirs;

    public static void main(String[] arg) throws java.io.IOException {
    	
    	
        java.io.File sourceDir = new java.io.File(arg[0]);
        rootDir = sourceDir.toURI();

       	excludeDirs = new File[0];
        if (arg.length > 1) {
        	excludeDirs = new File[arg.length - 1];

        	// we retrieve the exclusion patterns
        	for (int i = 1; i < arg.length; i++) {

        		URI uriexclude = new File(arg[i]).toURI();
        		if (!uriexclude.isAbsolute()) {
        			excludeDirs[i - 1] = new File(rootDir.resolve(uriexclude));
        		}
        		else {
        			excludeDirs[i - 1] = new File(arg[i]);
        		}
        	}
        }

        addCopyrightToDir(sourceDir);
    }

    private static void addCopyrightToFile(java.io.File file)
        throws java.io.IOException {
        String name = file.getName();

        if (!name.endsWith(".java")) {
            return;
        }

        byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
        String program = new String(b);

        //        if(program.indexOf("Copyright (C)")!= -1){
        //        	return;
        //        }
        int packageStart = program.indexOf("package");

        if (packageStart == -1) {
            return;
        }
        String copyrightInFile = program.substring(0, packageStart);
        if (copyrightInFile.contains("Copyright") &&
                !copyrightInFile.contains("ProActive")) {
        	System.out.println("Skipping " + file + ", other copyright exists.");
            return;
        }
        System.out.println("Processing " + file);

        String uncopyrightedProgram = program.substring(packageStart);
        String copyrightedProgram = LGPLcopyright + uncopyrightedProgram;
        b = copyrightedProgram.getBytes();
        file.delete();

        java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(
                    file));
        out.write(b, 0, b.length);
        out.flush();
        out.close();
    }

    private static void patternReplacementsInFile(java.io.File file)
        throws java.io.IOException {
        String name = file.getName();

        // selection of files where patterns will be found
        if (name.endsWith(".java") || name.endsWith(".xml") ||
                name.endsWith(".xsl") || name.endsWith(".xslt") ||
                name.endsWith(".html") || name.endsWith(".htm")) {
            byte[] b = getBytesFromInputStream(new java.io.FileInputStream(file));
            String filetext = new String(b);

            file.delete();

            java.io.OutputStream out = new java.io.BufferedOutputStream(new java.io.FileOutputStream(
                        file));

            String currentText = filetext;
            filetext = null;
            int indexoffirstword = -1;
            boolean anyReplacement = false;

            do {
                indexoffirstword = -1;

                // index of the first pattern which has been found or the end of the file
                int lowestindex = currentText.length();

                // we try to locate each pattern in the replacements list 
                for (int i = 0; i < replacements.length; i++) {
                    int indexfound = currentText.indexOf(PatternBegin +
                            replacements[i][0] + PatternEnd);

                    if ((indexfound != -1) && (indexfound < lowestindex)) {
                    	// we save the pattern which happens first
                        indexoffirstword = i;
                        lowestindex = indexfound;
                        anyReplacement = true;
                    }
                }

                String toWrite = currentText.substring(0, lowestindex);
                
                // we writes what's before the first pattern
                b = toWrite.getBytes();
                out.write(b, 0, b.length);
                out.flush();

                // we replace the pattern found
                if (indexoffirstword != -1) {
                    b = replacements[indexoffirstword][1].getBytes();
                    out.write(b, 0, b.length);
                    out.flush();
                    
                    // we skip the pattern in the source
                    currentText = currentText.substring(lowestindex+PatternBegin.length() +
                            replacements[indexoffirstword][0].length() + PatternEnd.length());
                }
                // if there are still more patterns, go on
            } while (indexoffirstword != -1);

            out.close();
            if (anyReplacement) {
            System.out.println("Patterns replaced in " + file);
            }
        }
    }

    private static void addCopyrightToDir(java.io.File file)
        throws java.io.IOException {
        for (File exclude : excludeDirs) {
            if (file.equals(exclude)) {
                return;
            }
        }

        java.io.File[] listFiles = file.listFiles();

        if (listFiles == null) {
            return;
        }

        for (int i = 0; i < listFiles.length; i++) {
            java.io.File fileItem = listFiles[i];

            if (fileItem.isDirectory()) {
                if (!fileItem.getName().equals(".svn")) {
                    addCopyrightToDir(fileItem);
                }
            } else {
                addCopyrightToFile(fileItem);
                patternReplacementsInFile(fileItem);
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
}
