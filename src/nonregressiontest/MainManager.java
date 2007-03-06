/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2007 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://www.inria.fr/oasis/ProActive/contacts.html
 *  Contributor(s):
 *
 * ################################################################
 */
package nonregressiontest;

import java.io.File;
import java.io.IOException;

import org.objectweb.proactive.core.config.ProActiveConfiguration;
import org.xml.sax.SAXException;

import testsuite.manager.FunctionalTestManager;


/**
 * @author rquilici
 *
 */
public class MainManager extends FunctionalTestManager {

    /**
     * Constructor for MainManager.
     */
    public MainManager() {
        this("Main unit test manager", "Manage all unit non-regression tests");
        //logger.addAppender(new ConsoleAppender(new SimpleLayout()));
    }

    /**
     * Constructor for MainManager.
     * @param name
     * @param description
     */
    public MainManager(String name, String description) {
        super(name, description);
    }

    /**
     * Constructor for MainManager.
     * @param xmlFile
     */
    public MainManager(File xmlFile)
        throws IOException, SAXException, ClassNotFoundException, 
            InstantiationException, IllegalAccessException {
        super(xmlFile);
        //logger.addAppender(new ConsoleAppender(new SimpleLayout()));
    }

    /**
     * @see testsuite.manager.AbstractManager#initManager()
     */
    public void initManager() throws Exception {
        // nothing to do
    }

    /**
     * @see testsuite.manager.AbstractManager#endManager()
     */
    public void endManager() throws Exception {
        // delete all nodes
        //TestNodes.killNodes();
    }

    public static void main(String[] args) {
        //removeLogfile();
        ProActiveConfiguration.load();
        MainManager manager = null;
        String path = null;
        if (args.length>0) {
                path = MainManager.class.getResource(args[0]).getPath();	
        }  else {
             path = MainManager.class.getResource(
                "/nonregressiontest/testsuite.xml").getPath();
}
        File xml = new File(path);

        try {
            manager = new MainManager(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch all unit tests and interlinked tests
        manager.execute();

        // logger.info(
        // "You can see the results in the test.html file in your ProActive directory.");
        
        if (manager.getErrors() > 0) {
        		System.exit(1);
        }
        System.exit(0);
    }
}
