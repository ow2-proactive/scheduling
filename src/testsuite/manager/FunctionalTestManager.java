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

import org.xml.sax.SAXException;

import testsuite.group.Group;

import testsuite.result.AbstractResult;
import testsuite.result.ResultsCollections;

import testsuite.test.FunctionalTest;

import java.io.File;
import java.io.IOException;

import java.util.Iterator;


/**
 * @author Alexandre di Costanzo
 *
 */
public abstract class FunctionalTestManager extends AbstractManager {

    /**
 *
 */
    public FunctionalTestManager() {
        super("FunctionalTestManager with no name",
            "FunctionalTestManager with no description");
    }

    /**
 * @param name
 * @param description
 */
    public FunctionalTestManager(String name, String description) {
        super(name, description);
    }

    public FunctionalTestManager(File xmlDescriptor)
        throws IOException, SAXException {
        super(xmlDescriptor);
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
                continue;
            }

            int runs = 0;
            int errors = 0;
            for (int i = 0; i < getNbRuns(); i++) {
                Iterator itTest = group.iterator();
                while (itTest.hasNext()) {
                    FunctionalTest test = (FunctionalTest) itTest.next();
                    AbstractResult result = test.runTest();
                    resultsGroup.add(result);
                    if (test.isFailed()) {
                        logger.warn("Test " + test.getName() + " [FAILED]");
                        errors++;
                    } else {
                        if (logger.isInfoEnabled()) {
                            logger.info("Test " + test.getName() +
                                " runs with [SUCCESS]");
                        }
                        runs++;
                    }
                }
            }
            resultsGroup.add(AbstractResult.GLOBAL_RESULT,
                "Group : " + group.getName() + " Runs : " + runs +
                " Errors : " + errors);

            try {
                group.endGroup();
            } catch (Exception e) {
                logger.warn("Can't ending group : " + group.getName(), e);
                resultsGroup.add(AbstractResult.ERROR,
                    "Can't ending group : " + group.getName(), e);
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

    private int runs = 0;
    private int errors = 0;

    private void execCascadingTests(ResultsCollections results,
        FunctionalTest test) {
        FunctionalTest[] tests = test.getTests();
        int length = (tests != null) ? tests.length : 0;
        for (int i = 0; i < length; i++)
            execCascadingTests(results, tests[i]);
        results.add(test.runTestCascading());
        if (test.isFailed()) {
            logger.warn("Test " + test.getName() + " [FAILED]");
            errors++;
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Test " + test.getName() + " runs with [SUCCESS]");
            }
            runs++;
        }
    }

    public void execute(Group group, FunctionalTest lastestTest,
        boolean useAttributesFile) {
        if (logger.isInfoEnabled()) {
            logger.info("Starting with interlinked Tests ...");
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

        results.add(AbstractResult.IMP_MSG,
            "Starting with interlinked Tests ...");

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

        ResultsCollections resultsGroup = group.getResults();
        try {
            group.initGroup();
        } catch (Exception e) {
            logger.warn("Can't init group : " + group.getName(), e);
            resultsGroup.add(AbstractResult.ERROR,
                "Can't init group : " + group.getName(), e);
        }

        for (int i = 0; i < getNbRuns(); i++) {
            execCascadingTests(resultsGroup, lastestTest);
        }
        resultsGroup.add(AbstractResult.GLOBAL_RESULT,
            "Group : " + group.getName() + " Runs : " + runs + " Errors : " +
            errors);

        try {
            group.endGroup();
        } catch (Exception e) {
            logger.warn("Can't ending group : " + group.getName(), e);
            resultsGroup.add(AbstractResult.ERROR,
                "Can't ending group : " + group.getName(), e);
        }

        results.addAll(resultsGroup);
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
}
