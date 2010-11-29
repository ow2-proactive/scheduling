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
