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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.ext.matlab.worker.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciConfigurationParser;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;


/**
 * MatlabConfigurationParser
 *
 * @author The ProActive Team
 */
public class MatlabConfigurationParser extends MatSciConfigurationParser {

    static final String DEFAULT_CONFIG_PATH = "extensions/matlab/config/worker/MatlabConfiguration.xml";

    static Document document;
    static Element racine;

    public static ArrayList<MatSciEngineConfig> getConfigs() throws Exception {
        String homestr = null;

        try {
            homestr = ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();
        } catch (Exception e) {
            // Try to locate dynamically for the location of the current class file
            final String path = MatlabConfigurationParser.class.getProtectionDomain().getCodeSource()
                    .getLocation().getPath();
            final File f = new File(path);
            File schedulerHome = null;

            // If the path contains 'classes' the scheduler home 2 parent dirs
            if (path.contains("classes")) {
                schedulerHome = f.getParentFile().getParentFile();
            } else { // means its in dist
                schedulerHome = f.getParentFile();
            }

            homestr = schedulerHome.getAbsolutePath();

            // Unable to locate dynamically the location of the scheduler home throw
            // exception to inform about the initial problem
            if (!(new File(homestr)).exists()) {
                throw e;
            }
        }

        File homesched = new File(homestr);

        // To find the config file path :
        // 1 - Check the scheduler config
        // 2 - If not found check for property
        // 3 - If not found use default config path

        // 1 - Check the scheduler config
        String configFilePath = PASchedulerProperties.MATLAB_WORKER_CONFIGURATION_FILE.getValueAsString();
        if (configFilePath == null || "".equals(configFilePath)) {
            // 2 - If not found check for property
            configFilePath = System.getProperty(PASchedulerProperties.MATLAB_WORKER_CONFIGURATION_FILE
                    .getKey());
            if (configFilePath == null || "".equals(configFilePath)) {
                // 3 - If not defined use default config path relative to scheduler home
                configFilePath = homestr + File.separator + DEFAULT_CONFIG_PATH;
            }
        }
        File configFile = null;
        try {
            // Check if the config file exists at the specified path
            configFile = new File(configFilePath);
        } catch (Exception e) {
            System.out.println("MatlabConfigurationParser.getConfigs() --> path " + configFilePath);
            e.printStackTrace();
        }
        if (!configFile.exists() || !configFile.canRead()) {
            throw new FileNotFoundException(configFile + " not found, aborting...");
        }

        System.out.println("Parsing configuration file: " + configFile);

        SAXBuilder sxb = new SAXBuilder();
        Document document = sxb.build(configFile);
        racine = document.getRootElement();

        List listInstallations = racine.getChildren("installation");

        boolean hasManyConfigs = (listInstallations.size() > 1);

        // The list of configurations to fill
        final ArrayList<MatSciEngineConfig> configs = new ArrayList<MatSciEngineConfig>();

        //On crée un Iterator sur notre liste
        Iterator i = listInstallations.iterator();
        while (i.hasNext()) {

            Element courant = (Element) i.next();
            String version = courant.getChild("version").getText();
            if ((version == null) || (version.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", version element must not be empty");
            }
            version = version.trim();
            if (!version.matches("^[1-9][\\d]*\\.[\\d]+$")) {
                throw new IllegalArgumentException("In " + configFile +
                    ", version element must match XX.xx, received : " + version);
            }
            String home = courant.getChild("home").getText();
            if ((home == null) || (home.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", home element must not be empty");
            }

            home = home.trim().replaceAll("/", Matcher.quoteReplacement(File.separator));
            File filehome = new File(home);
            checkDir(filehome, configFile);

            String bindir = courant.getChild("bindir").getText();
            if ((bindir == null) || (bindir.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", bindir element must not be empty");
            }
            bindir = bindir.trim().replaceAll("/", Matcher.quoteReplacement(File.separator));
            File filebin = new File(filehome, bindir.trim());
            checkDir(filebin, configFile);

            String command = courant.getChild("command").getText();
            if ((command == null) || (command.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", command element must not be empty");
            }
            command = command.trim();
            File filecommand = new File(filebin, command);
            checkFile(filecommand, configFile, true);


            configs.add(new MatlabEngineConfig(home, version, bindir, command));
        }

        return configs;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(getConfigs());
    }
}
