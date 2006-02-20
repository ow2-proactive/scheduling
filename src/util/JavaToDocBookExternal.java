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
import java.io.IOException;

/** An implementation of JavaToDocBook, by calling an external program.; 
 * here is used the GNU source-highlight program.
 * Program available at www.gnu.org/software/src-highlite.
 * Run './configure && make && make install' to get the correct installation.   
 * */
public class JavaToDocBookExternal implements JavaToDocBook {

    private String path="/usr/local/bin/" ;
    private String exec="source-highlight";

    
    /** Convert a code file into a decorated code file, using an external program.
     * Transform some java code into some nicely highlighted docbook.
     * @param fileToConvert the name of the file to convert 
     * @return convertedFile : the name of the file which has been created (it contains decorated code)  */   
    public String convert(String file)  throws IOException {
        Process converter = Runtime.getRuntime().exec(path + exec + " -f docbook -i " + file + " -o " + file + ".xml");
        try {
            converter.waitFor();
        } catch (InterruptedException e) {
            throw new IOException ("Problem with conversion to docbook of "  + file + ". " + e);
        }
        return file + ".xml";
    }

    /** Using this converter can only work if the external executable is found 
     * @return true only if the external executable is available on filesystem. */
    public boolean willWork() {
        File executableFile = new File (this.path + this.exec);
        return executableFile.exists();
    }

}
