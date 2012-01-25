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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.resourcemanager.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.resourcemanager.core.properties.PAResourceManagerProperties;

import com.google.gson.JsonParser;


/**
 * 
 * Class executes scripts on the side of a node.
 * Static scripts (marked in the config) are executed once.
 * Dynamic scripts are executed periodically.
 * 
 * Script output must be a valid json format.
 *
 */
public class ScriptExecutor {

    protected static final Logger logger = ProActiveLogger.getLogger(RMLoggers.RMNODE);
    private static final String SCRIPTS_PATH_SUFFIX = "/scripts/nodeinfo/";

    private int scriptTimeout;

    private JsonParser parser = new JsonParser();
    private String scriptsDir;
    private String configFileName;
    private long lastModified = 0;

    private List<Script> scripts = null;

    private Timer timer = new Timer(true);
    private AtomicBoolean newResultsAwailable = new AtomicBoolean(false);
    private HashMap<String, String> hostInfo = new HashMap<String, String>();

    public ScriptExecutor() {

        // script execution status
        boolean scriptExecutionEnabled = RMNodeStarter.SCRIPT_EXCUTION_ENABLED;
        if (System.getProperty(RMNodeStarter.SCRIPT_EXCUTION_ENABLED_PROPERTY) != null) {
            scriptExecutionEnabled = Boolean.parseBoolean(System
                    .getProperty(RMNodeStarter.SCRIPT_EXCUTION_ENABLED_PROPERTY));
        }

        // script execution period
        int period = RMNodeStarter.DYNAMIC_SCRIPTS_DEFAULT_PERIOD;
        if (System.getProperty(RMNodeStarter.DYNAMIC_SCRIPTS_PERIOD_PROPERTY) != null) {
            period = Integer.parseInt(System.getProperty(RMNodeStarter.DYNAMIC_SCRIPTS_PERIOD_PROPERTY));
        }

        // script execution timeout
        scriptTimeout = RMNodeStarter.SCRIPT_EXCUTION_DEFAULT_TIMEOUT;
        if (System.getProperty(RMNodeStarter.DYNAMIC_SCRIPTS_PERIOD_PROPERTY) != null) {
            scriptTimeout = Integer.parseInt(System
                    .getProperty(RMNodeStarter.DYNAMIC_SCRIPTS_PERIOD_PROPERTY));
        }

        if (scriptExecutionEnabled) {

            // RM home and scripts directory
            String rmHome = System.getProperty(PAResourceManagerProperties.RM_HOME.getKey());
            if (rmHome == null) {
                logger.error("Please set " + PAResourceManagerProperties.RM_HOME.getKey() +
                    " property for script execution");
                throw new RuntimeException("Please set pa.rm.home property or"
                    + " disable the script execution by setting proactive.node.script.enabled to false");
            }
            scriptsDir = getOSScriptsDir(rmHome + SCRIPTS_PATH_SUFFIX);
            logger.debug("Scripts directory " + scriptsDir);
            configFileName = scriptsDir + "/config";

            loadConfig(false);
            executeScripts();
            // config will be reloaded only for dynamic scripts
            scripts = null;

            timer.schedule(new ScriptExecutorTask(), period, period);
        } else {
            logger.debug("The script execution is disabled");
        }

    }

    /**
     * Loads config from RM_HOME/scripts/nodeproperties/OS_NAME/config.
     * Config has the following syntax: 
     * 
     * "script relative path" "top level property name" "script type"
     * i.e.
     * processes.sh processes dynamic
     * 
     * @param onlyDynamic when only dynamic script must be loaded
     */
    private void loadConfig(boolean onlyDynamic) {
        File config = new File(configFileName);
        if (config.exists()) {
            if (scripts == null || lastModified < config.lastModified()) {
                lastModified = config.lastModified();

                logger.debug("Reading the script execution config file");
                scripts = new LinkedList<Script>();

                try {
                    BufferedReader br = new BufferedReader(new FileReader(config));
                    String line;
                    while ((line = br.readLine()) != null) {

                        if (line.startsWith("#")) {
                            // ignore comment
                            continue;
                        }

                        if (line.trim().length() == 0) {
                            // ignore empty lines
                            continue;
                        }

                        String[] values = line.split(" ");

                        if (values.length >= 3) {
                            String scriptType = values[2];

                            Script script = new Script(values[0], values[1]);
                            if (onlyDynamic && !scriptType.equals("dynamic")) {
                                continue;
                            } else {
                                scripts.add(script);
                            }
                        } else {
                            logger.error("Incorrect line format: " + line);
                        }
                    }
                    br.close();
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        } else {
            throw new IllegalArgumentException("Config file does not exist");
        }
    }

    /**
     * Gets scripts results in JSON format
     * @return scripts results in JSON format
     */
    public String getScriptsResults() {
        StringBuilder hInfo = new StringBuilder();
        synchronized (hostInfo) {
            boolean first = true;
            for (String scriptProperty : hostInfo.keySet()) {
                if (!first) {
                    hInfo.append(",");
                } else {
                    first = false;
                }
                hInfo.append("'" + scriptProperty + "': " + hostInfo.get(scriptProperty));
            }
            newResultsAwailable.set(false);
        }

        return hInfo.toString();
    }

    public boolean scriptInfoUpdated() {
        return newResultsAwailable.get();
    }

    /**
     * Execute a set of scripts according to the config
     */
    private void executeScripts() {

        if (scripts == null) {
            logger.debug("Nothing to execute");
        }

        boolean newResults = false;
        for (Script script : scripts) {
            String output = executeScript(script.getFileName());
            try {
                parser.parse("{ 'must_be_valid_json' : " + output + "}");
            } catch (RuntimeException e) {
                logger.error(e.getMessage() + ":\n{ 'must_be_valid_json' : " + output + "}");
                continue;
            }

            synchronized (hostInfo) {
                String info = hostInfo.get(script.getPropertyName());
                if (info == null || (info != null && !info.equals(output))) {
                    newResults = true;
                }
                hostInfo.put(script.getPropertyName(), output);
            }
        }

        if (newResults) {
            // notifying the node
            newResultsAwailable.set(true);
        }
    }

    /**
     * Executes a single script.
     * 
     * @param scriptName of a script to execute
     * @return the script output
     */
    private String executeScript(String scriptName) {
        String fileName = scriptsDir + scriptName;
        logger.debug("Executing script " + fileName);

        // stops the script execution after timeout
        ProcessLauncher watcher = new ProcessLauncher(fileName);
        try {
            watcher.join(scriptTimeout);
        } catch (InterruptedException e) {
            logger.error("", e);
        } finally {
            if (watcher.isAlive()) {
                logger.error("Script execution timeout " + fileName);
                watcher.interrupt();
            }
        }

        String output = watcher.getProcessOutput();

        logger.debug("Script output:\n" + output);
        return output.toString();
    }

    /**
     * These rules reflects the folder structure
     * depending of the operating system.
     * 
     * @param baseScriptDir 
     * @return OS dependent path to script directory
     */
    private String getOSScriptsDir(String baseScriptDir) {

        String os = System.getProperty("os.name").toLowerCase();
        if ((os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0)) {
            return baseScriptDir + "unix/";
        } else if (os.indexOf("mac") >= 0) {
            return baseScriptDir + "mac/";
        } else if (os.indexOf("win") >= 0) {
            return baseScriptDir + "windows/";
        }

        return "";
    }

    /**
     * Class responsible for periodical execution of dynamic scripts.
     */
    class ScriptExecutorTask extends TimerTask {
        @Override
        public void run() {
            loadConfig(true);
            executeScripts();
        }
    }

    /**
     * Class launches a new process and read its output.
     */
    class ProcessLauncher extends Thread {

        private String scriptName;
        private StringBuffer output = new StringBuffer();

        public ProcessLauncher(String scriptName) {
            this.scriptName = scriptName;
            start();
        }

        public void run() {
            ProcessBuilder pb = new ProcessBuilder(scriptName);
            pb.redirectErrorStream(true);
            Process proc = null;

            InputStreamReader in = null;
            try {
                proc = pb.start();
                InputStream is = proc.getInputStream();
                in = new InputStreamReader(is);

                int b = -1;
                while ((b = in.read()) > -1) {
                    output.append((char) b);
                }
            } catch (IOException e) {
                logger.error("", e);
            } finally {

                try {
                    in.close();
                } catch (IOException e) {
                    logger.error("", e);
                }

                try {
                    int exit = proc.exitValue();
                    if (exit == 0) {
                        logger.debug("The script execution finished successfully " + scriptName);
                    } else {
                        logger.debug("The script finished with errors " + scriptName);
                    }
                } catch (IllegalThreadStateException t) {
                    proc.destroy();
                }
            }
        }

        public String getProcessOutput() {
            return output.toString();
        }

    }

    /**
     * Class containing the script name and the property name defined by script output
     */
    class Script {

        private String fileName;
        private String propertyName;

        public Script(String fileName, String propertyName) {
            this.fileName = fileName;
            this.propertyName = propertyName;
        }

        public String getFileName() {
            return fileName;
        }

        public String getPropertyName() {
            return propertyName;
        }
    }
}
