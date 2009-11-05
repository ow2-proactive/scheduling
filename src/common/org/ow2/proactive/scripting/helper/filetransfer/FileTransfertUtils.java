/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of 
 * 						   Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2. 
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.filetransfer;

/**
 * FileTransfertUtils...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class FileTransfertUtils {

    /** Extract filename from pathfile (ex: C:\myFld\myFile.txt => myFile.txt)
     * @param pathFile the path of the file
     */
    public static String getFilenameFromPathfile(String pathFile) {
        //--Check parameters
        if (pathFile == null)
            return "";

        String filename = pathFile;
        int index = pathFile.lastIndexOf("/");
        // --Unix
        if (index != -1) {
            filename = pathFile.substring(pathFile.lastIndexOf("/") + 1, pathFile.length());
        }
        // --Windows
        else {
            filename = pathFile.substring(pathFile.lastIndexOf("\\") + 1, pathFile.length());
        }
        return filename;
    }

    /** Extract path from pathfile (ex: C:\myFld\myFile.txt => C:\myFld)
     * @param pathFile the path of the file
     */
    public static String getFolderFromPathfile(String pathFile) {
        //--Check parameters
        if (pathFile == null)
            return "";

        String path = pathFile;
        int index = pathFile.lastIndexOf("/");
        // --Unix
        if (index != -1) {
            path = pathFile.substring(0, pathFile.lastIndexOf("/"));
        }
        // --Windows
        else {
            path = pathFile.substring(0, pathFile.lastIndexOf("\\"));
        }

        return path;
    }
}
