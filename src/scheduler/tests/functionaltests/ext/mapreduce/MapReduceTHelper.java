/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */

package functionaltests.ext.mapreduce;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJob;
import org.ow2.proactive.scheduler.ext.mapreduce.PAMapReduceJobConfiguration;
import org.ow2.proactive.scheduler.ext.mapreduce.exception.PAJobConfigurationException;

import functionaltests.SchedulerTHelper;

public class MapReduceTHelper {

    private static FileSystem localFs; 

    static {
        try {
            localFs = FileSystem.getLocal(new Configuration());
        } catch (IOException io) {
            throw new RuntimeException("Problem getting local fs", io);
        }
    } 

    public static PAMapReduceJobConfiguration getConfiguration() {
        PAMapReduceJobConfiguration pamrjc = new PAMapReduceJobConfiguration();
        return pamrjc;
    }

    public static JobResult submit(Job job, PAMapReduceJobConfiguration pamrjc) throws Exception {
        PAMapReduceJob pamrj = null;
        try {
            pamrj = new PAMapReduceJob(job, pamrjc);
        } catch (PAJobConfigurationException e) {
            e.printStackTrace();
        }
        TaskFlowJob taskflow = pamrj.getMapReduceWorkflow();
        JobResult result = waitForCompletion(taskflow);
        return result;
    }
    
    public static JobResult waitForCompletion(TaskFlowJob taskflow) throws Exception {
        JobId id;
        JobResult result;
        id = SchedulerTHelper.submitJob(taskflow);
        System.out.println("-------------Job submitted------------------------");
        SchedulerTHelper.waitForFinishedJob(id);
        result = SchedulerTHelper.getJobResult(id);
        System.out.println("Job name: " + result.getName());
        System.out.println("Job result: " + result);
        System.out.println("Had exception: " + result.hadException());
        Map<String, TaskResult> ex = result.getExceptionResults();
        for (Map.Entry<String, TaskResult> e : ex.entrySet()) {
            System.out.println("Exception in task " + e.getKey());
            e.getValue().getException().printStackTrace();
        }
        System.out.println("-------------Job finished------------------------");
        return result;
    }
    
    private String testRootDir;

    public MapReduceTHelper(String name) {
        testRootDir = System.getProperty("java.io.tmpdir")+ File.separator + name;
    }

    /**
     * Delete the directory under test root directory.
     * 
     * @param dir
     *            relative path to directory to delete
     * @return true if delete is successful else false
     * @throws IOException
     */
    public boolean delete(String dir) throws IOException {
        return localFs.delete(new Path(testRootDir + File.separator + dir), true);
    }

    /**
     * Delete the test root directory.
     * 
     * @return true if delete is successful else false
     * @throws IOException
     */
    public boolean cleanup() throws IOException {
        return localFs.delete(new Path(testRootDir), true);
    }
    
    public String getRootDir() {
        return testRootDir;
    }

    /**
     * Read (text) files from directory dirname under testRootDir, optionally
     * sort the lines, concatenate them into a string and return it.
     * 
     * @param dirname
     *            directory where to look for files
     * @param sort
     *            sort lines if true
     * @return
     * @throws IOException
     */
    public String readFiles(String dirname, boolean sort) throws IOException {
        File dir = new File(testRootDir + File.separator + dirname);
        String[] children = dir.list();
        List<String> lines = new ArrayList<String>();
        for (String filename : children) {
            File f = new File(testRootDir + File.separator + dirname + File.separator + filename);
            BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String line = b.readLine();
            while (line != null) {
                lines.add(line);
                line = b.readLine();
            }
            b.close();
        }
        if (sort && lines.size() > 1) {
            Collections.sort(lines);
        }
        StringBuilder result = new StringBuilder();
        for (String line: lines) {
            result.append(line);
            result.append('\n');
        }
        return result.toString();
    }

    /**
     * Write a file under testRootDir
     * 
     * @param name
     *            relative path to the file
     * @param data
     *            data to write
     * @throws IOException
     */
    public void writeFile(String name, String data) throws IOException {
        Path file = new Path(testRootDir + File.separator + name);
        localFs.delete(file, false);
        DataOutputStream f = localFs.create(file);
        f.write(data.getBytes());
        f.close();
    }
    
}
