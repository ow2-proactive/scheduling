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
package org.ow2.proactive.scripting.helper.filetransfer.driver;

import java.util.ArrayList;
import java.util.List;

import org.ow2.proactive.scripting.helper.filetransfer.initializer.FileTransfertInitializer;


/**
 * FileTransfertDriver...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public interface FileTransfertDriver {

    void init(FileTransfertInitializer myInit);

    void getFile(String remoteFilePath, String localFolder) throws Exception;

    void getFiles(List<String> files, String localFolder) throws Exception;;

    void putFile(String localFilePath, String remoteFolder) throws Exception;

    void putFiles(List<String> localFilePaths, String remoteFolder) throws Exception;;

    public void getFolder(String remoteFolderPath, String localFolderPath) throws Exception;

    public void putFolder(String localFolderPath, String remoteFolderPath) throws Exception;

    ArrayList<String> list(String remoteFolder) throws Exception;

}
