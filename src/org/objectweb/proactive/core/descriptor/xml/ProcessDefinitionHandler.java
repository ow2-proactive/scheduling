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
package org.objectweb.proactive.core.descriptor.xml;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.process.AbstractListProcessDecorator;
import org.objectweb.proactive.core.process.ExternalProcess;
import org.objectweb.proactive.core.process.ExternalProcessDecorator;
import org.objectweb.proactive.core.process.JVMProcess;
import org.objectweb.proactive.core.process.globus.GlobusProcess;
import org.objectweb.proactive.core.process.gridengine.GridEngineSubProcess;
import org.objectweb.proactive.core.process.lsf.LSFBSubProcess;
import org.objectweb.proactive.core.process.oar.OARSubProcess;
import org.objectweb.proactive.core.process.pbs.PBSSubProcess;
import org.objectweb.proactive.core.process.prun.PrunSubProcess;
import org.objectweb.proactive.core.process.rsh.maprsh.MapRshProcess;
import org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshallerDecorator;
import org.objectweb.proactive.core.xml.handler.CollectionUnmarshaller;
import org.objectweb.proactive.core.xml.handler.PassiveCompositeUnmarshaller;
import org.objectweb.proactive.core.xml.handler.UnmarshallerHandler;
import org.objectweb.proactive.core.xml.io.Attributes;

import org.xml.sax.SAXException;


public class ProcessDefinitionHandler extends AbstractUnmarshallerDecorator
    implements ProActiveDescriptorConstants {
    protected String id;
    protected ProActiveDescriptor proActiveDescriptor;
    protected ExternalProcess targetProcess;

    public ProcessDefinitionHandler(ProActiveDescriptor proActiveDescriptor) {
        super(false);
        this.proActiveDescriptor = proActiveDescriptor;
        this.addHandler(JVM_PROCESS_TAG,
            new JVMProcessHandler(proActiveDescriptor));
        this.addHandler(RSH_PROCESS_TAG,
            new RSHProcessHandler(proActiveDescriptor));
        this.addHandler(MAPRSH_PROCESS_TAG,
            new MapRshProcessHandler(proActiveDescriptor));
        this.addHandler(SSH_PROCESS_TAG,
            new SSHProcessHandler(proActiveDescriptor));
        this.addHandler(RLOGIN_PROCESS_TAG,
            new RLoginProcessHandler(proActiveDescriptor));
        this.addHandler(BSUB_PROCESS_TAG,
            new BSubProcessHandler(proActiveDescriptor));
        this.addHandler(GLOBUS_PROCESS_TAG,
            new GlobusProcessHandler(proActiveDescriptor));
        this.addHandler(PRUN_PROCESS_TAG,
            new PrunProcessHandler(proActiveDescriptor));
        this.addHandler(PBS_PROCESS_TAG,
            new PBSProcessHandler(proActiveDescriptor));
        this.addHandler(GRID_ENGINE_PROCESS_TAG,
            new GridEngineProcessHandler(proActiveDescriptor));
        this.addHandler(OAR_PROCESS_TAG,
            new OARProcessHandler(proActiveDescriptor));
        ProcessListHandler handler = new ProcessListHandler(proActiveDescriptor);
        this.addHandler(PROCESS_LIST_TAG, handler);
        this.addHandler(PROCESS_LIST_BYHOST_TAG, handler);
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.AbstractUnmarshallerDecorator#notifyEndActiveHandler(String, UnmarshallerHandler)
     */
    protected void notifyEndActiveHandler(String name,
        UnmarshallerHandler activeHandler) throws SAXException {
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#getResultObject()
     */
    public Object getResultObject() throws SAXException {
        ExternalProcess result = targetProcess;
        targetProcess = null;
        return result;
    }

    /**
     * @see org.objectweb.proactive.core.xml.handler.UnmarshallerHandler#startContextElement(String, Attributes)
     */
    public void startContextElement(String name, Attributes attributes)
        throws SAXException {
        id = attributes.getValue("id");
    }

    public class ProcessHandler extends AbstractUnmarshallerDecorator
        implements ProActiveDescriptorConstants {
        protected ProActiveDescriptor proActiveDescriptor;
        protected boolean isRef;

        public ProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super();
            this.proActiveDescriptor = proActiveDescriptor;
            addHandler(ENVIRONMENT_TAG, new EnvironmentHandler());
            addHandler(PROCESS_REFERENCE_TAG, new ProcessReferenceHandler());
            addHandler(COMMAND_PATH_TAG, new CommandPathHanlder());
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            String className = attributes.getValue("class");
            if (!checkNonEmpty(className)) {
                throw new org.xml.sax.SAXException(
                    "Process defined without specifying the class");
            }
            try {
                targetProcess = proActiveDescriptor.createProcess(id, className);
            } catch (ProActiveException e) {
                //e.printStackTrace();
                throw new org.xml.sax.SAXException(e.getMessage());
            }
            String hostname = attributes.getValue("hostname");
            if (checkNonEmpty(hostname)) {
                targetProcess.setHostname(hostname);
            }
            String username = attributes.getValue("username");
            if (checkNonEmpty(username)) {
                targetProcess.setUsername(username);
            }
        }

        //
        // -- implements UnmarshallerHandler ------------------------------------------------------
        //
        public Object getResultObject() throws org.xml.sax.SAXException {
            return null;
        }

        //
        // -- PROTECTED METHODS ------------------------------------------------------
        //
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            if (name.equals(ENVIRONMENT_TAG)) {
                targetProcess.setEnvironment((String[]) activeHandler.getResultObject());
            } else if (name.equals(PROCESS_REFERENCE_TAG)) {
                if (!(targetProcess instanceof ExternalProcessDecorator)) {
                    throw new org.xml.sax.SAXException(
                        "found a Process defined inside a non composite process");
                }
                ExternalProcessDecorator cep = (ExternalProcessDecorator) targetProcess;
                Object result = activeHandler.getResultObject();
                proActiveDescriptor.registerProcess(cep, (String) result);
            } else if (name.equals(COMMAND_PATH_TAG)) {
                targetProcess.setCommandPath((String) activeHandler.getResultObject());
            }
        }

        //
        // -- INNER CLASSES ------------------------------------------------------
        //

        /**
         * This class receives environment events
         */
        protected class EnvironmentHandler extends BasicUnmarshaller {
            private java.util.ArrayList variables;

            public EnvironmentHandler() {
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
                variables = new java.util.ArrayList();
            }

            public Object getResultObject() throws org.xml.sax.SAXException {
                if (variables == null) {
                    isResultValid = false;
                } else {
                    int n = variables.size();
                    String[] result = new String[n];
                    if (n > 0) {
                        variables.toArray(result);
                    }
                    setResultObject(result);
                    variables.clear();
                    variables = null;
                }
                return super.getResultObject();
            }

            public void startElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
                if (name.equals(VARIABLE_TAG)) {
                    String vName = attributes.getValue("name");
                    String vValue = attributes.getValue("value");
                    if (checkNonEmpty(vName) && (vValue != null)) {
                        logger.info("Found environment variable name=" + vName +
                            " value=" + vValue);
                        variables.add(vName + "=" + vValue);
                    }
                }
            }
        }

        // end inner class EnvironmentHandler
        protected class CommandPathHanlder extends BasicUnmarshaller {
            public CommandPathHanlder() {
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
                String path = attributes.getValue("value");

                if (checkNonEmpty(path)) {
                    setResultObject(path);
                } else {
                    throw new org.xml.sax.SAXException(
                        "The Id of the referenced definition cannot be set to an empty string");
                }
            }
        }
    }

    public class ProcessListHandler extends ProcessHandler
        implements ProActiveDescriptorConstants {
        public ProcessListHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            int padding = 0;
            String className = attributes.getValue("class");
            if (!checkNonEmpty(className)) {
                throw new org.xml.sax.SAXException(
                    "Process defined without specifying the class");
            }
            try {
                targetProcess = proActiveDescriptor.createProcess(id, className);
            } catch (ProActiveException e) {
                //e.printStackTrace();
                throw new org.xml.sax.SAXException(e.getMessage());
            }
            String fixedName = attributes.getValue("fixedName");
            String list = attributes.getValue("list");
            String domain = attributes.getValue("domain");
            String spadding = attributes.getValue("padding");
            String hostlist = attributes.getValue("hostlist");
            if (checkNonEmpty(spadding)) {
                padding = Integer.parseInt(spadding);
            }
            if (checkNonEmpty(fixedName) && checkNonEmpty(list) &&
                    checkNonEmpty(domain)) {
                ((AbstractListProcessDecorator) targetProcess).setHostConfig(fixedName,
                    list, domain, padding);
            }
            if (checkNonEmpty(hostlist)) {
                ((AbstractListProcessDecorator) targetProcess).setHostList(hostlist,domain);                                                                
            }
            String username = attributes.getValue("username");
            if (checkNonEmpty(username)) {
                targetProcess.setUsername(username);
            }
        }
    }

    //end of inner class ProcessHandler
    protected class PrunProcessHandler extends ProcessHandler {
        public PrunProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(PRUN_OPTIONS_TAG, new PrunOptionHandler());
            //	System.out.println("ProcessDefinitionHandler.PrunProcessHandler()");
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);
            String queueName = (attributes.getValue("queue"));
            if (checkNonEmpty(queueName)) {
                ((PrunSubProcess) targetProcess).setQueueName(queueName);
            }
        }

        protected class PrunOptionHandler extends PassiveCompositeUnmarshaller {
            //  	private static final String HOSTLIST_ATTRIBUTE = "hostlist";
            //  	private static final String PROCESSOR_ATRIBUTE = "processor";
            //private LSFBSubProcess bSubProcess;
            public PrunOptionHandler() {
                //this.bSubProcess = (LSFBSubProcess)targetProcess;
                //      System.out.println("ProcessDefinitionHandler.PrunOptionHandler()");
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(HOSTS_NUMBER_TAG, new SingleValueUnmarshaller());
                this.addHandler(PROCESSOR_PER_NODE_TAG,
                    new SingleValueUnmarshaller());
                this.addHandler(BOOKING_DURATION_TAG,
                    new SingleValueUnmarshaller());
                this.addHandler(PRUN_OUTPUT_FILE, new SingleValueUnmarshaller());
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                //    this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
            }

            protected void notifyEndActiveHandler(String name,
                UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
                // we know that it is a prun process since we are
                // in prun option!!!
                PrunSubProcess prunSubProcess = (PrunSubProcess) targetProcess;

                //    System.out.println("+++++ notifyEndActiveHandler " + name);
                if (name.equals(HOST_LIST_TAG)) {
                    prunSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(HOSTS_NUMBER_TAG)) {
                    prunSubProcess.setHostsNumber((String) activeHandler.getResultObject());
                } else if (name.equals(PROCESSOR_PER_NODE_TAG)) {
                    prunSubProcess.setProcessorPerNodeNumber((String) activeHandler.getResultObject());
                    //                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    //                    prunSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                    //                }
                } else if (name.equals(BOOKING_DURATION_TAG)) {
                    prunSubProcess.setBookingDuration((String) activeHandler.getResultObject());
                } else if (name.equals(PRUN_OUTPUT_FILE)) {
                    prunSubProcess.setOutputFile((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class PBSProcessHandler extends ProcessHandler {
        public PBSProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(PBS_OPTIONS_TAG, new PBSOptionHandler());
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);
            String queueName = (attributes.getValue("queue"));
            if (checkNonEmpty(queueName)) {
                ((PBSSubProcess) targetProcess).setQueueName(queueName);
            }
        }

        protected class PBSOptionHandler extends PassiveCompositeUnmarshaller {
            public PBSOptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(HOSTS_NUMBER_TAG, new SingleValueUnmarshaller());
                this.addHandler(PROCESSOR_PER_NODE_TAG,
                    new SingleValueUnmarshaller());
                this.addHandler(BOOKING_DURATION_TAG,
                    new SingleValueUnmarshaller());
                this.addHandler(PRUN_OUTPUT_FILE, new SingleValueUnmarshaller());
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
            }

            protected void notifyEndActiveHandler(String name,
                UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
                PBSSubProcess pbsSubProcess = (PBSSubProcess) targetProcess;
                if (name.equals(HOST_LIST_TAG)) {
                    pbsSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(HOSTS_NUMBER_TAG)) {
                    pbsSubProcess.setHostsNumber((String) activeHandler.getResultObject());
                } else if (name.equals(PROCESSOR_PER_NODE_TAG)) {
                    pbsSubProcess.setProcessorPerNodeNumber((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    pbsSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else if (name.equals(BOOKING_DURATION_TAG)) {
                    pbsSubProcess.setBookingDuration((String) activeHandler.getResultObject());
                } else if (name.equals(PRUN_OUTPUT_FILE)) {
                    pbsSubProcess.setOutputFile((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class GridEngineProcessHandler extends ProcessHandler {
        public GridEngineProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(GRID_ENGINE_OPTIONS_TAG,
                new GridEngineOptionHandler());
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);
            //  String queueName = (attributes.getValue("queue"));
            //        if (checkNonEmpty(queueName)) {
            //            ((PBSSubProcess) targetProcess).setQueueName(queueName);
            //        }
        }

        protected class GridEngineOptionHandler
            extends PassiveCompositeUnmarshaller {
            public GridEngineOptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(HOSTS_NUMBER_TAG, new SingleValueUnmarshaller());
                this.addHandler(PARALLEL_ENVIRONMENT_TAG,
                    new SingleValueUnmarshaller());
                //   this.addHandler(PROCESSOR_TAG, new SingleValueUnmarshaller());
                this.addHandler(BOOKING_DURATION_TAG,
                    new SingleValueUnmarshaller());
                //   this.addHandler(PRUN_OUTPUT_FILE, new SingleValueUnmarshaller());
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
            }

            protected void notifyEndActiveHandler(String name,
                UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
                GridEngineSubProcess geSubProcess = (GridEngineSubProcess) targetProcess;
                if (name.equals(HOST_LIST_TAG)) {
                    geSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(HOSTS_NUMBER_TAG)) {
                    geSubProcess.setHostsNumber((String) activeHandler.getResultObject());
                } else if (name.equals(PARALLEL_ENVIRONMENT_TAG)) {
                    geSubProcess.setParallelEnvironment((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    geSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else if (name.equals(BOOKING_DURATION_TAG)) {
                    geSubProcess.setBookingDuration((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    protected class OARProcessHandler extends ProcessHandler {
        public OARProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(OAR_OPTIONS_TAG, new OAROptionHandler());
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            super.startContextElement(name, attributes);
            String queueName = (attributes.getValue("queue"));
            if (checkNonEmpty(queueName)) {
                ((OARSubProcess) targetProcess).setQueueName(queueName);
            }
        }

        protected class OAROptionHandler extends PassiveCompositeUnmarshaller {
            public OAROptionHandler() {
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(HOSTS_NUMBER_TAG, new SingleValueUnmarshaller());
                //  this.addHandler(PROCESSOR_TAG, new SingleValueUnmarshaller());
                this.addHandler(BOOKING_DURATION_TAG,
                    new SingleValueUnmarshaller());
                //   this.addHandler(PRUN_OUTPUT_FILE, new SingleValueUnmarshaller());
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
            }

            protected void notifyEndActiveHandler(String name,
                UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
                OARSubProcess oarSubProcess = (OARSubProcess) targetProcess;

                if (name.equals(HOST_LIST_TAG)) {
                    oarSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(HOSTS_NUMBER_TAG)) {
                    oarSubProcess.setHostsNumber((String) activeHandler.getResultObject());
                    //  } else if (name.equals(PROCESSOR_PER_NODE_TAG)) {
                    //       oarSubProcess.setProcessorPerNodeNumber((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    oarSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else if (name.equals(BOOKING_DURATION_TAG)) {
                    oarSubProcess.setBookingDuration((String) activeHandler.getResultObject());
                } else if (name.equals(PRUN_OUTPUT_FILE)) {
                    oarSubProcess.setOutputFile((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }
    }

    //end of inner class PrunProcessHandler
    protected class JVMProcessHandler extends ProcessHandler {
        public JVMProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            UnmarshallerHandler pathHandler = new PathHandler();
            {
                CollectionUnmarshaller cu = new CollectionUnmarshaller(String.class);
                cu.addHandler(ABS_PATH_TAG, pathHandler);
                cu.addHandler(REL_PATH_TAG, pathHandler);
                cu.addHandler(JVMPARAMETER_TAG, new SimpleValueHandler());
                this.addHandler(CLASSPATH_TAG, cu);
                this.addHandler(BOOT_CLASSPATH_TAG, cu);
                this.addHandler(JVMPARAMETERS_TAG, cu);
            }
            BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
            bch.addHandler(ABS_PATH_TAG, pathHandler);
            bch.addHandler(REL_PATH_TAG, pathHandler);
            bch.addHandler(JVMPARAMETER_TAG, new BasicUnmarshaller());
            //  this.addHandler(JVMPARAMETERS_TAG, bch);
            this.addHandler(JAVA_PATH_TAG, bch);
            this.addHandler(POLICY_FILE_TAG, bch);
            this.addHandler(LOG4J_FILE_TAG, bch);
            this.addHandler(PROACTIVE_PROPS_FILE_TAG, bch);
            this.addHandler(CLASSNAME_TAG, new SingleValueUnmarshaller());
            this.addHandler(PARAMETERS_TAG, new SingleValueUnmarshaller());
            // this.addHandler(JVMPARAMETERS_TAG, new SingleValueUnmarshaller());
        }

        //
        //  ----- PUBLIC METHODS -----------------------------------------------------------------------------------
        //
        //
        //  ----- PROTECTED METHODS -----------------------------------------------------------------------------------
        //
        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            // the fact targetProcess is a JVMProcess is checked in startContextElement
            //super.notifyEndActiveHandler(name,activeHandler);
            JVMProcess jvmProcess = (JVMProcess) targetProcess;

            if (name.equals(CLASSPATH_TAG)) {
                String[] paths = (String[]) activeHandler.getResultObject();
                if (paths.length > 0) {
                    StringBuffer sb = new StringBuffer();
                    String pathSeparator = System.getProperty("path.separator");
                    sb.append(paths[0].trim());
                    //we call trim method to avoid whitespace at the beginning or end of a path
                    for (int i = 1; i < paths.length; i++) {
                        sb.append(pathSeparator);
                        sb.append(paths[i].trim());
                    }
                    jvmProcess.setClasspath(sb.toString());
                }
            } else if (name.equals(BOOT_CLASSPATH_TAG)) {
                String[] paths = (String[]) activeHandler.getResultObject();
                if (paths.length > 0) {
                    StringBuffer sb = new StringBuffer();
                    String pathSeparator = System.getProperty("path.separator");
                    sb.append(paths[0].trim());
                    for (int i = 1; i < paths.length; i++) {
                        sb.append(pathSeparator);
                        sb.append(paths[i].trim());
                    }
                    jvmProcess.setBootClasspath(sb.toString());
                }
            } else if (name.equals(JVMPARAMETERS_TAG)) {
                String[] paths = (String[]) activeHandler.getResultObject();

                if (paths.length > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < paths.length; i++) {
                        //  sb.append(pathSeparator);
                        sb.append(paths[i]);
                        sb.append(" ");
                    }
                    jvmProcess.setJvmOptions(sb.toString());
                }
            } else if (name.equals(JAVA_PATH_TAG)) {
                String jp = (String) activeHandler.getResultObject();
                jvmProcess.setJavaPath(jp.trim());
            } else if (name.equals(POLICY_FILE_TAG)) {
                jvmProcess.setPolicyFile(((String) activeHandler.getResultObject()).trim());
            } else if (name.equals(LOG4J_FILE_TAG)) {
                jvmProcess.setLog4jFile(((String) activeHandler.getResultObject()).trim());
            } else if (name.equals(PROACTIVE_PROPS_FILE_TAG)) {
                jvmProcess.setJvmOptions("-Dproactive.configuration=" +
                    (String) activeHandler.getResultObject());
            } else if (name.equals(CLASSNAME_TAG)) {
                jvmProcess.setClassname((String) activeHandler.getResultObject());
            } else if (name.equals(PARAMETERS_TAG)) {
                jvmProcess.setParameters((String) activeHandler.getResultObject());
            } // else if (name.equals(JVMPARAMETERS_TAG)) {

            else {
                super.notifyEndActiveHandler(name, activeHandler);
            }
        }
    }

    // end of inner class JVMProcessHandler 
    protected class RSHProcessHandler extends ProcessHandler {
        public RSHProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
        }
    }

    //end of inner class RSHProcessHandler
    protected class MapRshProcessHandler extends ProcessHandler {
        public MapRshProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            UnmarshallerHandler pathHandler = new PathHandler();
            BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
            bch.addHandler(ABS_PATH_TAG, pathHandler);
            bch.addHandler(REL_PATH_TAG, pathHandler);
            this.addHandler(SCRIPT_PATH_TAG, bch);
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            // we know that it is a maprsh process since we are
            // in map rsh handler!!!
            //MapRshProcess mapRshProcess = (MapRshProcess)targetProcess;
            super.startContextElement(name, attributes);
            String parallelize = attributes.getValue("parallelize");
            if (checkNonEmpty(parallelize)) {
                ((MapRshProcess) targetProcess).setParallelization(
                    "parallelize");
            }
        }

        protected void notifyEndActiveHandler(String name,
            UnmarshallerHandler activeHandler) throws org.xml.sax.SAXException {
            //MapRshProcess mapRshProcess = (MapRshProcess)targetProcess;
            if (name.equals(SCRIPT_PATH_TAG)) {
                ((MapRshProcess) targetProcess).setScriptLocation((String) activeHandler.getResultObject());
            } else {
                super.notifyEndActiveHandler(name, activeHandler);
            }
        }
    }

    //end of inner class MapRshProcessHandler
    protected class SSHProcessHandler extends ProcessHandler {
        public SSHProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
        }
    }

    //end of inner class SSHProcessHandler
    protected class RLoginProcessHandler extends ProcessHandler {
        public RLoginProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
        }
    }

    //end of inner class RLoginProcessHandler
    protected class BSubProcessHandler extends ProcessHandler {
        public BSubProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(BSUB_OPTIONS_TAG, new BsubOptionHandler());
        }

        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            // we know that it is a maprsh process since we are
            // in map rsh handler!!!
            //MapRshProcess mapRshProcess = (MapRshProcess)targetProcess;
            super.startContextElement(name, attributes);
            String interactive = (attributes.getValue("interactive"));
            if (checkNonEmpty(interactive)) {
                ((LSFBSubProcess) targetProcess).setInteractive(interactive);
            }
            String queueName = (attributes.getValue("queue"));
            if (checkNonEmpty(queueName)) {
                ((LSFBSubProcess) targetProcess).setQueueName(queueName);
            }
        }

        protected class BsubOptionHandler extends PassiveCompositeUnmarshaller {
            //  	private static final String HOSTLIST_ATTRIBUTE = "hostlist";
            //  	private static final String PROCESSOR_ATRIBUTE = "processor";
            //private LSFBSubProcess bSubProcess;
            public BsubOptionHandler() {
                //this.bSubProcess = (LSFBSubProcess)targetProcess;
                UnmarshallerHandler pathHandler = new PathHandler();
                this.addHandler(HOST_LIST_TAG, new SingleValueUnmarshaller());
                this.addHandler(PROCESSOR_TAG, new SingleValueUnmarshaller());
                this.addHandler(RES_REQ_TAG, new SimpleValueHandler());
                BasicUnmarshallerDecorator bch = new BasicUnmarshallerDecorator();
                bch.addHandler(ABS_PATH_TAG, pathHandler);
                bch.addHandler(REL_PATH_TAG, pathHandler);
                this.addHandler(SCRIPT_PATH_TAG, bch);
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
            }

            protected void notifyEndActiveHandler(String name,
                UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
                // we know that it is a bsub process since we are
                // in bsub option!!!
                LSFBSubProcess bSubProcess = (LSFBSubProcess) targetProcess;
                if (name.equals(HOST_LIST_TAG)) {
                    bSubProcess.setHostList((String) activeHandler.getResultObject());
                } else if (name.equals(PROCESSOR_TAG)) {
                    bSubProcess.setProcessorNumber((String) activeHandler.getResultObject());
                } else if (name.equals(RES_REQ_TAG)) {
                    bSubProcess.setRes_requirement((String) activeHandler.getResultObject());
                } else if (name.equals(SCRIPT_PATH_TAG)) {
                    bSubProcess.setScriptLocation((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }

        // end inner class OptionHandler 
    }

    // end of inner class BSubProcessHandler
    protected class GlobusProcessHandler extends ProcessHandler {
        public GlobusProcessHandler(ProActiveDescriptor proActiveDescriptor) {
            super(proActiveDescriptor);
            this.addHandler(GLOBUS_OPTIONS_TAG, new GlobusOptionHandler());
        }

        protected class GlobusOptionHandler extends PassiveCompositeUnmarshaller {
            public GlobusOptionHandler() {
                this.addHandler(GLOBUS_COUNT_TAG, new SingleValueUnmarshaller());
            }

            public void startContextElement(String name, Attributes attributes)
                throws org.xml.sax.SAXException {
            }

            protected void notifyEndActiveHandler(String name,
                UnmarshallerHandler activeHandler)
                throws org.xml.sax.SAXException {
                // we know that it is a globus process since we are
                // in globus option!!!
                GlobusProcess globusProcess = (GlobusProcess) targetProcess;
                if (name.equals(GLOBUS_COUNT_TAG)) {
                    globusProcess.setCount((String) activeHandler.getResultObject());
                } else {
                    super.notifyEndActiveHandler(name, activeHandler);
                }
            }
        }

        //end of inner class GlobusOptionHandler
    }

    //end of inner class GlobusProcessHandler
    private class SingleValueUnmarshaller extends BasicUnmarshaller {
        public void readValue(String value) throws org.xml.sax.SAXException {
            //System.out.println("SingleValueUnmarshaller.readValue() " + value);
            setResultObject(value);
        }
    }

    private class SimpleValueHandler extends BasicUnmarshaller {
        public void startContextElement(String name, Attributes attributes)
            throws org.xml.sax.SAXException {
            // read from XML
            String value = attributes.getValue("value");

            setResultObject(value);
        }
    }

    //end of inner class SingleValueUnmarshaller
}
