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
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version
 * 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 */
package org.objectweb.proactive.core.process.globus;

import org.apache.log4j.Logger;
import org.globus.gram.Gram;
import org.globus.gram.GramJob;
import org.globus.gram.GramJobListener;
import org.globus.gram.internal.GRAMConstants;
import org.globus.io.gass.server.GassServer;
import org.globus.io.gass.server.JobOutputListener;
import org.globus.io.gass.server.JobOutputStream;
import org.globus.util.deactivator.Deactivator;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * Java CoG Job submission class
 */
public class GridJob implements GramJobListener {
    static Logger logger = ProActiveLogger.getLogger(Loggers.DEPLOYMENT_PROCESS);
    private GassServer m_gassServer; // GASS Server: required to get job output
    private String m_gassURL = null; // URL of the GASS server
    private GramJob m_job = null; // GRAM JOB to be executed
    private String m_jobOutput = ""; // job output as string
    private boolean m_batch = false; // Submission modes:
    private String m_remoteHost = null; // host where job will run
    private String m_jobid = null; // Globus job id on the form: 
    int options = org.globus.io.gass.server.GassServer.READ_ENABLE |
        org.globus.io.gass.server.GassServer.WRITE_ENABLE |
        org.globus.io.gass.server.GassServer.STDOUT_ENABLE |
        org.globus.io.gass.server.GassServer.STDERR_ENABLE;

    //https://server.com:39374/15621/1021382777/
    public GridJob(String Contact, boolean batch) {
        m_remoteHost = Contact; // remote host
        m_batch = batch; // submission mode
    }

    public String GlobusRun(String RSL) {
        String newRSL = null;
        try {
            // load default Globus proxy. Java CoG kit must be installed 
            // and a user certificate setup properly
            ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
            GSSCredential cred = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
            if (cred == null) {
                logger.error("credential null");
            }

            //We check if both stdout and stderr are already set
            //We should test if one of both, but usually we set both or none
            //This might be change if it doesn't fit user requirements
            if ((RSL.indexOf("stdout") < 0) && (RSL.indexOf("stderr") < 0)) {
                // Start GASS server
                if (!startGassServer(cred)) {
                    throw new Exception("Unable to start GASS server.");
                }

                //	   setup Job Output listeners
                initJobOutListeners();

                // Append GASS URL to job String so we can get some output back
                // if non-batch, then get some output back
                if (!m_batch) {
                    newRSL = "&" + RSL.substring(0, RSL.indexOf('&')) +
                        "(rsl_substitution=(GLOBUSRUN_GASS_URL " + m_gassURL +
                        "))" +
                        RSL.substring(RSL.indexOf('&') + 1, RSL.length()) +
                        "(stdout=$(GLOBUSRUN_GASS_URL)/dev/stdout-5)(stderr=$(GLOBUSRUN_GASS_URL)/dev/sterr-5)";
                    //newRSL = RSL;
                } else {
                    //	   format batching RSL so output can be retrieved later on using any GTK commands
                    newRSL = RSL +
                        "(stdout=x-gass-cache://$(GLOBUS_GRAM_JOB_CONTACT)stdout anExtraTag)" +
                        "(stderr=x-gass-cache://$(GLOBUS_GRAM_JOB_CONTACT)stderr anExtraTag)";
                }
            } else {
                newRSL = RSL;
            }
            Gram.ping(m_remoteHost);
            logger.info("ping successfull");
            logger.info(newRSL);
            m_job = new GramJob(newRSL);

            //	   set proxy. CoG kit and user credentials must be installed and set 
            //	   up properly
            m_job.setCredentials(cred);

            // if non-batch then listen for output
            //            if (!m_batch) {
            //m_job.addListener(new GramJobListenerImpl());
            m_job.addListener(this);
            // }
            logger.info("Sending job request to: " + m_remoteHost);
            m_job.request(m_remoteHost, m_batch, false);

            // Wait for job to complete
            //         if (!m_batch) {
            //                synchronized (this) {
            //                    try {
            //                        wait();
            //                    } catch (Exception e) {
            //                    	e.printStackTrace();
            //                    }
            // }
            //} else {
            // do not wait for job. Return immediately
            m_jobOutput = "Job sent. url=" + m_job.getIDAsString();
            // }
        } catch (Exception ex) {
            if (m_gassServer != null) {
                // unregister from gass server
                m_gassServer.unregisterJobOutputStream("err-5");
                m_gassServer.unregisterJobOutputStream("out-5");
            }

            m_jobOutput = "Error submitting job: " + ex.getClass() + ":" +
                ex.getMessage();
            ex.printStackTrace();
        }

        // cleanup
        Deactivator.deactivateAll();
        return m_jobOutput;
    }

    /**
     * Start the Globus GASS Server. Used to get the output from the server
     * back to the client.
     */
    private boolean startGassServer(GSSCredential cred) {
        if (m_gassServer != null) {
            return true;
        }

        try {
            m_gassServer = new GassServer(cred, 0);
            m_gassServer.setOptions(options);
            m_gassURL = m_gassServer.getURL();
            logger.info("gass server started succesfully " + m_gassURL);
        } catch (Exception e) {
            logger.error("gass server failed to start!");
            e.printStackTrace();

            return false;
        }

        m_gassServer.registerDefaultDeactivator();

        return true;
    }

    /**
     * Init job out listeners for non-batch mode jobs.
     */
    private void initJobOutListeners() throws Exception {
        // job output vars
        JobOutputListenerImpl outListener = new JobOutputListenerImpl();
        JobOutputStream outStream = new JobOutputStream(outListener);
        m_gassServer.registerJobOutputStream("out-5", outStream);
        m_gassServer.registerJobOutputStream("err-5", outStream);
    }

    public void statusChanged(GramJob job) {
        String status = job.getStatusAsString();
        logger.info("status changed " + status);

        try {
            if (job.getStatus() == GRAMConstants.STATUS_ACTIVE) {
                // notify waiting thread when job ready
                m_jobOutput = "Job sent. url=" + job.getIDAsString();
                logger.info(m_jobOutput);

                // if notify enabled return URL as output
                //						synchronized (this) {
                //							notify();
                //						}
            }
        } catch (Exception ex) {
            logger.error("statusChanged Error:" + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private class JobOutputListenerImpl implements JobOutputListener {
        public void outputClosed() {
            logger.info("output closed");
        }

        public void outputChanged(String output) {
            m_jobOutput += output;
            logger.info("output changed: " + m_jobOutput);
        }
    }
}
