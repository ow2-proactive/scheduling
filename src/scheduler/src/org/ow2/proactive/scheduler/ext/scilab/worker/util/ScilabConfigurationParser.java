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
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciConfigurationParser;
import org.ow2.proactive.scheduler.ext.matsci.worker.util.MatSciEngineConfig;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * ScilabConfigurationParser
 *
 * @author The ProActive Team
 */
public class ScilabConfigurationParser extends MatSciConfigurationParser {

    static final String configPath = "extensions/scilab/config/worker/ScilabConfiguration.xml";

    static Document document;
    static Element racine;

    public static ArrayList<MatSciEngineConfig> getConfigs() throws Exception {
        File configFile;
        ArrayList<MatSciEngineConfig> configs = new ArrayList<MatSciEngineConfig>();

        if (PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE != null &&
            PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE.getValueAsString() != "") {
            configFile = new File(PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE.getValueAsString());
        } else if (System.getProperty(PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE.getKey()) != null) {
            configFile = new File(System.getProperty(PASchedulerProperties.SCILAB_WORKER_CONFIGURATION_FILE
                    .getKey()));
        } else {
            File home = findSchedulerHome();
            URI configFileURI = home.toURI().resolve(configPath);
            configFile = new File(configFileURI);
        }

        SAXBuilder sxb = new SAXBuilder();
        Document document = sxb.build(configFile);
        racine = document.getRootElement();

        List listInstallations = racine.getChildren("installation");

        //On crée un Iterator sur notre liste
        Iterator i = listInstallations.iterator();
        while (i.hasNext()) {

            Element courant = (Element) i.next();
            String version = courant.getChild("version").getText();
            String home = courant.getChild("home").getText();
            String libdir = courant.getChild("libdir").getText();
            String tpdir = courant.getChild("tpdir").getText();
            String scidir = courant.getChild("scidir").getText();
            String bindir = courant.getChild("bindir").getText();
            String command = courant.getChild("command").getText();

            configs.add(new ScilabEngineConfig(home, version, libdir, scidir, tpdir, bindir, command));
        }

        return configs;

    }

    public static void main(String[] args) throws Exception {
        System.out.println(getConfigs());
    }
}
