/*
* ################################################################
*
* ProActive: The Java(TM) library for Parallel, Distributed,
*            Concurrent computing with Security and Mobility
*
* Copyright (C) 1997-2002 INRIA/University of Nice-Sophia Antipolis
* Contact: proactive-support@inria.fr
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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.SimpleLayout;
import org.xml.sax.SAXException;

import testsuite.manager.ProActiveFuncTestManager;


/**
 * @author rquilici
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MainManager extends ProActiveFuncTestManager {
    private static String FS = System.getProperty("file.separator");
    private static String RESULT_LOCATION = System.getProperty("user.home") +
        FS + "test.html";

    /**
     * Constructor for MainManager.
     */
    public MainManager() {
        this("Main unit test manager", "Manage all unit non-regression tests");
        logger.addAppender(new ConsoleAppender(new SimpleLayout()));
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
		logger.addAppender(new ConsoleAppender(new SimpleLayout()));
    }

    public static void main(String[] args) {
        MainManager manager = null;
        String path = MainManager.class
                             .getResource("/nonregressiontest/MainManager.xml")
                             .getPath();
        File xml = new File(path);
        try {
            manager = new MainManager(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
        manager.execute(false);
        manager.setVerbatim(true);
        try {
            manager.toHTML(new File(RESULT_LOCATION));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
