/*
 * Created on Oct 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.objectweb.proactive.core.process.globus;

import org.apache.log4j.Logger;
import org.globus.gram.*;

import org.globus.io.gass.server.*;

//import org.globus.security.*;

import org.globus.util.deactivator.Deactivator;

import org.gridforum.jgss.ExtendedGSSManager;

import org.ietf.jgss.GSSCredential;


/**
 * Java CoG Job submission class
 */
public class GridJob implements GramJobListener{
	static Logger logger = Logger.getLogger(GridJob.class.getName());
    private GassServer m_gassServer; // GASS Server: required to get job output
    private String m_gassURL = null; // URL of the GASS server
    private GramJob m_job = null; // GRAM JOB to be executed
    private String m_jobOutput = ""; // job output as string
    private boolean m_batch = false; // Submission modes:
    private String m_remoteHost = null; // host where job will run
//    private JobOutputStream m_stdoutStream = null;
//    private JobOutputStream m_stderrStream = null;
    private String m_jobid = null; // Globus job id on the form: 
    int options = 
	    org.globus.io.gass.server.GassServer.READ_ENABLE |
	    org.globus.io.gass.server.GassServer.WRITE_ENABLE |
	    org.globus.io.gass.server.GassServer.STDOUT_ENABLE |
	    org.globus.io.gass.server.GassServer.STDERR_ENABLE;

    //https://server.com:39374/15621/1021382777/
    public GridJob(String Contact, boolean batch) {
        m_remoteHost = Contact; // remote host
        m_batch = batch; // submission mode
    }

    public  String GlobusRun(String RSL) {
        try {
            // load default Globus proxy. Java CoG kit must be installed 
            // and a user certificate setup properly
            ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
            GSSCredential cred = manager.createCredential(GSSCredential.INITIATE_AND_ACCEPT);
            if (cred == null){
				logger.error("credential null");
            }

            // Start GASS server
            if (!startGassServer(cred)) {
                throw new Exception("Unable to start GASS server.");
            }

            //	   setup Job Output listeners
            initJobOutListeners();

            //	   Append GASS URL to job String so we can get some output back
            String newRSL = null;

            // if non-batch, then get some output back
//            if (!m_batch) {
//                newRSL = "&" + RSL.substring(0, RSL.indexOf('&')) +
//                    "(rsl_substitution=(GLOBUSRUN_GASS_URL " + m_gassURL +
//                    "))" + RSL.substring(RSL.indexOf('&') + 1, RSL.length())+
//					"(stdout=$(GLOBUSRUN_GASS_URL)/dev/stdout-5)(stderr=$(GLOBUSRUN_GASS_URL)/dev/sterr-5)";
//				//newRSL = RSL;
//				System.out.println(newRSL);
//            } else {
//                //	   format batching RSL so output can be retrieved later on using any GTK commands
//                newRSL = RSL +
//                    "(stdout=x-gass-cache://$(GLOBUS_GRAM_JOB_CONTACT)stdout anExtraTag)" +
//                    "(stderr=x-gass-cache://$(GLOBUS_GRAM_JOB_CONTACT)stderr anExtraTag)";
//            }
			
							newRSL = "&" + RSL.substring(0, RSL.indexOf('&')) +
								"(rsl_substitution=(GLOBUSRUN_GASS_URL " + m_gassURL +
								"))" + RSL.substring(RSL.indexOf('&') + 1, RSL.length())+
								"(stdout=$(GLOBUSRUN_GASS_URL)/dev/stdout-5)(stderr=$(GLOBUSRUN_GASS_URL)/dev/sterr-5)";
							//newRSL = RSL;
				
						
			Gram.ping(m_remoteHost);
			System.out.println("ping successfull");
			System.out.println(newRSL);
            m_job = new GramJob(newRSL);

            //	   set proxy. CoG kit and user credentials must be installed and set 
            //	   up properly
            m_job.setCredentials(cred);

            // if non-batch then listen for output
//            if (!m_batch) {
                //m_job.addListener(new GramJobListenerImpl());
			m_job.addListener(this);
           // }

            System.out.println("Sending job request to: " + m_remoteHost);
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
            System.out.println("gass server started succesfully "+m_gassURL);
        } catch (Exception e) {
            System.err.println("gass server failed to start!");
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
    
	public void  statusChanged(GramJob job) {
				String status = job.getStatusAsString();
				System.out.println("status changed "+status);

				try {
					if (job.getStatus() == GramJob.STATUS_ACTIVE) {
						// notify waiting thread when job ready
						m_jobOutput = "Job sent. url=" + job.getIDAsString();
						logger.info(m_jobOutput);

						// if notify enabled return URL as output
//						synchronized (this) {
//							notify();
//						}
					}
				} catch (Exception ex) {
					System.out.println("statusChanged Error:" + ex.getMessage());
					ex.printStackTrace();
				}
			}
    

    private class JobOutputListenerImpl implements JobOutputListener {
        public void outputClosed() {
        	System.out.println("output closed");
        }

        public void outputChanged(String output) {
            m_jobOutput += output;
			System.out.println("output changed: "+m_jobOutput);
        }
    }
}
