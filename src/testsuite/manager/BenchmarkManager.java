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
package testsuite.manager;

import testsuite.group.Group;

import testsuite.result.AbstractResult;
import testsuite.result.BenchmarkResult;
import testsuite.result.ResultsCollections;

import testsuite.test.Benchmark;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


/**
 * @author Alexandre di Costanzo
 *
 */
public abstract class BenchmarkManager extends AbstractManager {

    /**
       *
       */
    public BenchmarkManager() {
        super("BenchmarkManager with no name",
            "BenchmarkManager with no description");
    }

    /**
     * @param name
     * @param description
     */
    public BenchmarkManager(String name, String description) {
        super(name, description);
    }

    /**
     * @see testsuite.manager.AbstractManager#execute()
     */
    public void execute(boolean useAttributesFile) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting ...");
        }
        ResultsCollections results = getResults();

        if (useAttributesFile) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Load attributes");
                }
                loadAttributes();
            } catch (IOException e1) {
                if (logger.isInfoEnabled()) {
                    logger.info(e1);
                }
            }
        }

        results.add(AbstractResult.IMP_MSG, "Starting ...");
        try {
            initManager();
        } catch (Exception e) {
            logger.fatal("Can't init the manager", e);
            results.add(AbstractResult.ERROR, "Can't init the manager", e);
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Init Manager with success");
        }
        results.add(AbstractResult.IMP_MSG, "Init Manager with success");

        Iterator itGroup = iterator();
        while (itGroup.hasNext()) {
            Group group = (Group) itGroup.next();
            ResultsCollections resultsGroup = group.getResults();

            try {
                group.initGroup();
            } catch (Exception e) {
                logger.warn("Can't init group : " + group.getName(), e);
                resultsGroup.add(AbstractResult.ERROR,
                    "Can't init group : " + group.getName(), e);
                results.addAll(resultsGroup);
                continue;
            }

            int runs = 0;
            int errors = 0;
            long sum = 0;
            Iterator itTest = group.iterator();
            while (itTest.hasNext()) {
                Benchmark test = (Benchmark) itTest.next();
                long[] set = new long[getNbRuns()];
                for (int i = 0; i < getNbRuns(); i++) {
                    AbstractResult result = test.runTest();

                    if (test.isFailed()) {
                        logger.warn("Bench " + test.getName() + " [FAILED]");
                        errors++;
                    } else {
                        set[i] = test.getResultTime();
                        if (logger.isInfoEnabled()) {
                            logger.info("Bench " + test.getName() +
                                " runs with success in " +
                                test.getResultTime() + "ms");
                        }
                        runs++;
                        sum += test.getResultTime();
                    }
                }
                resultsGroup.add(new BenchmarkResult(test,
                        AbstractResult.GLOBAL_RESULT, "no message", set));
            }
            resultsGroup.add(AbstractResult.GLOBAL_RESULT,
                "Group : " + group.getName() + ", Moy in " +
                ((runs == 0) ? "Failed" : ((sum / (double) runs) + "ms")) +
                " Runs : " + runs + " Errors : " + errors);

            try {
                group.endGroup();
            } catch (Exception e) {
                logger.warn("Can't ending group : " + group.getName(), e);
                resultsGroup.add(AbstractResult.ERROR,
                    "Can't ending group : " + group.getName(), e);
                results.addAll(resultsGroup);
                continue;
            }
            results.addAll(resultsGroup);
        }

        try {
            endManager();
        } catch (Exception e) {
            logger.fatal("Can't ending the manager", e);
            results.add(AbstractResult.ERROR, "Can't ending the manager", e);
            return;
        }

        results.add(AbstractResult.IMP_MSG, "... Finish");
        if (logger.isInfoEnabled()) {
            logger.info("... Finish");
        }
    }

    /**
     * @see testsuite.result.ResultsExporter#toHTML(java.io.File)
     */
    public void toHTML(File location)
        throws ParserConfigurationException, TransformerException, IOException {
        createSVG(location.getParentFile());
        super.toHTML(location);
    }

    private void createSVG(File location)
        throws ParserConfigurationException, TransformerException, IOException {
        TransformerFactory tFactory = TransformerFactory.newInstance();
        String xslPath = "/" +
            AbstractManager.class.getName().replace('.', '/').replaceAll("manager.*",
                "/xslt/svgExporter.xsl");
        InputStream stylesheet = getClass().getResourceAsStream(xslPath);
        Transformer transformer = tFactory.newTransformer(new StreamSource(
                    stylesheet));
        DOMSource xml = new DOMSource(toXML());

        transformer.setParameter("dest", location.getPath());
        File tmp = File.createTempFile("bench", ".tmp");
        tmp.deleteOnExit();
        StreamResult os = new StreamResult(tmp);
        transformer.transform(xml, os);
        stylesheet.close();
    }
}
