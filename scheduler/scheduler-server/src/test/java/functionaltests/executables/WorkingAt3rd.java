/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package functionaltests.executables;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;


public class WorkingAt3rd extends JavaExecutable {

    private String prefix = null;

    private String suffix = null;

    @Override
    public Serializable execute(TaskResult... results) throws Throwable {
        String fileName = System.getProperty("java.io.tmpdir") + File.separator + prefix + suffix + ".tmp";
        File f = new File(fileName);
        //file does not exist
        if (!f.exists()) {
            f.createNewFile();
            PrintWriter pw = new PrintWriter(f);
            pw.write("1");
            pw.close();
            throw new RuntimeException("WorkingAt3rd - Status : File not found");
        }
        //file exist
        BufferedReader br = new BufferedReader(new FileReader(f));
        int n = Integer.parseInt(br.readLine());
        br.close();
        //file number is less that 2
        if (n < 2) {
            PrintWriter pw = new PrintWriter(f);
            pw.write("" + (n + 1));
            pw.close();
            if (prefix.equals("WorkingAt3rdT2_")) {
                boolean b = f.delete();
                throw new RuntimeException("WorkingAt3rd - Status : Number is " + n + " File deleted : " + b);
            } else {
                throw new RuntimeException("WorkingAt3rd - Status : Number is " + n);
            }
        }
        //file number is 2 or more
        boolean b = f.delete();
        return "WorkingAt3rd - Status : OK / File deleted : " + b;
    }

    @Override
    public void init(Map<String, Serializable> args) throws Exception {
        prefix = (String) args.get("prefix");
        suffix = (String) args.get("suffix");
    }

}
