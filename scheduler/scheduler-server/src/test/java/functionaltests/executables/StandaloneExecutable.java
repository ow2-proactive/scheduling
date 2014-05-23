/*
 *  *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2013 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 *  * $$ACTIVEEON_INITIAL_DEV$$
 */
package functionaltests.executables;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaStandaloneExecutable;
import org.ow2.proactive.scheduler.task.utils.RemoteSpaceAdapter;


/**
 * StandaloneExecutable a JavaStandaloneExecutable used to test the various methods of this class
 *
 * @author The ProActive Team
 **/
public class StandaloneExecutable extends JavaStandaloneExecutable {

    private void assertFileExists(File file) {
        if (!file.exists()) {
            throw new RuntimeException("File does not exist : " + file);
        }
    }

    protected String variable;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {

        // verifying variable access
        if (variable == null) {
            throw new RuntimeException("variable should not be null");
        }
        System.out.println(variable);

        // verifying node url access
        List<String> nodesUrl = getNodesURL();

        if (nodesUrl == null || nodesUrl.size() == 0) {
            throw new RuntimeException("Unexpected empty list of urls");
        }

        System.out.println(nodesUrl);

        // simply to verify that it does not throw exceptions
        int index = getIterationIndex();

        // verifying data space access
        Logger.getLogger(RemoteSpaceAdapter.class).setLevel(Level.DEBUG);

        // Clear user space
        getUserSpace().deleteFiles("**/*.txt");

        File file1 = getLocalSpace().getFile("toto.txt");
        file1.createNewFile();
        // push/pull file
        getUserSpace().pushFile(file1, "/");
        File file2 = getUserSpace().pullFile("/toto.txt", getLocalSpace().getFile("titi.txt"));
        assertFileExists(file2);

        // input/output stream
        PrintWriter pw = new PrintWriter(getUserSpace().getOutputStream("tutu.txt"));
        String line1 = "Hello World";
        pw.println(line1);
        pw.close();
        BufferedReader br = new BufferedReader(new InputStreamReader(getUserSpace().getInputStream(
                "/tutu.txt")));
        String line2 = br.readLine();
        if (!line1.equals(line2)) {
            throw new RuntimeException("Wrong line received : " + line2);
        }
        br.close();

        // push/pull/delete patterns
        getUserSpace().deleteFiles("/*.txt");
        getUserSpace().pushFiles("/*.txt", "/");
        getLocalSpace().deleteFiles("/*.txt");
        getUserSpace().pullFiles("/*.txt", "/");
        assertFileExists(file1);
        assertFileExists(file2);

        // push/pull directory
        File dir = getLocalSpace().getFile("dir");
        dir.mkdirs();
        File file3 = new File(dir, "toto.txt");
        File file4 = new File(dir, "titi.txt");
        file1.renameTo(new File(dir, "toto.txt"));
        file2.renameTo(new File(dir, "titi.txt"));

        getUserSpace().deleteFiles("/*.txt");
        getUserSpace().pushFile(dir, "/");
        getLocalSpace().deleteFile(dir);
        File dir2 = getUserSpace().pullFile("/dir", getLocalSpace().getFile("/"));

        assertFileExists(dir2);
        if (!dir2.isDirectory()) {
            throw new RuntimeException("File is not directory : " + file2);
        }
        assertFileExists(file3);
        assertFileExists(file4);

        return true;
    }

}
