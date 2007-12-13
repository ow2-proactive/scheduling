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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


/** An implementation of JavaToDocBook, calling an external program;
 * here the GNU source-highlight program is used.
 * Program available at www.gnu.org/software/src-highlite.
 * Run './configure && make && make install' to get the correct installation. */
public class JavaToDocBookExternal implements LanguageToDocBook {
    protected String path = "/usr/local/bin/";
    protected String exec = "source-highlight";

    /** Convert a code String into a <b>decorated</b> code String, using an external program.
     * Can only transform java code into highlighted docbook.
     * @param codeString a long String of code to convert
     * @return a long string containing the input code plus docbook tags highlighting it. */
    public String convert(String codeString) {
        try {
            // First copy the input code into an independent file
            File temp = File.createTempFile("db_tmp_", ".dbz");
            BufferedWriter tmpBuffer = new BufferedWriter(new FileWriter(temp));
            tmpBuffer.write(codeString);
            tmpBuffer.close();

            // Do the highlighting, which creates the File outputFileName
            String fileToConvert = temp.getPath();
            String outputFileName = fileToConvert + ".xml";
            String execString = path + exec + " -f docbook -i " + fileToConvert + " -o " + outputFileName;
            Process converter = Runtime.getRuntime().exec(execString);

            try {
                converter.waitFor();
            } catch (InterruptedException e) {
                System.err.println("Problem with program " + exec + " used to convert " + fileToConvert +
                    ": " + e.getMessage());

                return codeString;
            }

            temp.delete();

            // read from the newly created file, and turn that into one single string. 
            BufferedReader in = new BufferedReader(new FileReader(outputFileName));
            String str;
            String result = "";

            while ((str = in.readLine()) != null) {
                result += (str + "\n");
            }

            in.close();
            new File(outputFileName).delete();

            return result;
        } catch (IOException e) {
            System.err.println("Problem writing temp files used with converter " + exec + ": " +
                e.getMessage());

            return codeString;
        }
    }

    /** Using this converter can only work if the external executable is found.
     * @return true only if the external executable is available on filesystem. */
    public boolean willWork() {
        File executableFile = new File(this.path + this.exec);

        return executableFile.exists();
    }
}
