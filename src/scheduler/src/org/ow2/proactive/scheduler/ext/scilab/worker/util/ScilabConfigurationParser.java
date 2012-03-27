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
package org.ow2.proactive.scheduler.ext.scilab.worker.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.objectweb.proactive.core.runtime.ProActiveRuntimeImpl;
import org.objectweb.proactive.utils.OperatingSystem;
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
 * ScilabConfigurationParser
 *
 * @author The ProActive Team
 */
public class ScilabConfigurationParser extends MatSciConfigurationParser {

    static final String configPath = "extensions/scilab/config/worker/ScilabConfiguration.xml";

    protected static OperatingSystem os = OperatingSystem.getOperatingSystem();

    static Document document;
    static Element racine;

    public static ArrayList<MatSciEngineConfig> getConfigs(boolean debug) throws Exception {
        File configFile = null;
        ArrayList<MatSciEngineConfig> configs = new ArrayList<MatSciEngineConfig>();

        String homestr = ProActiveRuntimeImpl.getProActiveRuntime().getProActiveHome();
        File homesched = new File(homestr);
        if (PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE.getValueAsString() != null &&
            !"".equals(PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE.getValueAsString())) {

            configFile = new File(PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE.getValueAsString());

        } else if (System.getProperty(PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE.getKey()) != null) {

            configFile = new File(System.getProperty(PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE
                    .getKey()));

        }
        if (configFile == null) {
            configFile = new File(homesched, configPath);
        }
        if (!configFile.exists() || !configFile.canRead()) {
            throw new FileNotFoundException(configFile + " not found, aborting...");
        }

        if (debug) {
            System.out.println("Parsing configuration file :" + configFile);
        }

        SAXBuilder sxb = new SAXBuilder();
        Document document = sxb.build(configFile);
        racine = document.getRootElement();

        List listInstallations = racine.getChildren("installation");

        boolean hasManyConfigs = (listInstallations.size() > 1);

        //On crée un Iterator sur notre liste
        Iterator i = listInstallations.iterator();
        while (i.hasNext()) {

            Element courant = (Element) i.next();
            String version = courant.getChild("version").getText();
            if ((version == null) || (version.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", version element must not be empty");
            }
            version = version.trim();
            if (!version.matches("^([1-9][\\d]*\\.)*[\\d]+$")) {
                throw new IllegalArgumentException("In " + configFile +
                    ", version element must match XX.xx.xx, received : " + version);
            }
            String home = courant.getChild("home").getText();
            if ((home == null) || (home.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", home element must not be empty");
            }

            home = home.trim().replaceAll("/", Matcher.quoteReplacement("" + os.fileSeparator()));
            File filehome = new File(home);
            checkDir(filehome, configFile);

            String bindir = courant.getChild("bindir").getText();
            if ((bindir == null) || (bindir.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", bindir element must not be empty");
            }
            bindir = bindir.trim().replaceAll("/", Matcher.quoteReplacement("" + os.fileSeparator()));
            File filebin = new File(filehome, bindir.trim());
            checkDir(filebin, configFile);

            String command = courant.getChild("command").getText();
            if ((command == null) || (command.trim().length() == 0)) {
                throw new IllegalArgumentException("In " + configFile + ", command element must not be empty");
            }
            command = command.trim();
            File filecommand = new File(filebin, command);
            checkFile(filecommand, configFile, true);

            configs.add(new ScilabEngineConfig(home, version, bindir, command, false));
        }

        return configs;

    }

    public static void main(String[] args) throws Exception {
        System.out.println(getConfigs(true));
    }
}
