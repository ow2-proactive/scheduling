/*
 * Created on Jul 18, 2003
 *
 */
package testsuite.manager;

import java.io.IOException;
import java.util.Iterator;

import testsuite.group.Group;
import testsuite.result.AbstractResult;
import testsuite.result.ResultsCollections;
import testsuite.test.FunctionalTest;


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
                "Group : " + group.getName() + " Runs : " + runs + " Errors : " +
                errors);

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
            logger.info("Starting with imbricated Tests ...");
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

        results.add(AbstractResult.IMP_MSG, "Starting with imbricated Tests ...");

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
