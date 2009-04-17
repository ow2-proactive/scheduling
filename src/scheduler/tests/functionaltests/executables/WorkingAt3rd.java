/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
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

    /**
     *
     */
    private static final long serialVersionUID = 10L;
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
    public void init(Map<String, String> args) throws Exception {
        prefix = args.get("prefix");
        suffix = args.get("suffix");
    }

}
