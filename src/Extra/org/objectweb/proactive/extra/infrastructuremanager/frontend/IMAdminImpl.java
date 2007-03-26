package org.objectweb.proactive.extra.infrastructuremanager.frontend;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.descriptor.data.ProActiveDescriptor;
import org.objectweb.proactive.core.descriptor.data.VirtualNode;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.util.wrapper.StringWrapper;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMCore;
import org.objectweb.proactive.extra.infrastructuremanager.core.IMDeploymentFactory;
import org.objectweb.proactive.filetransfer.FileTransfer;
import org.objectweb.proactive.filetransfer.FileVector;


public class IMAdminImpl implements IMAdmin, Serializable {
    private static final Logger logger = ProActiveLogger.getLogger(Loggers.IM_ADMIN);

    // Attributes
    private IMCore imcore;

    //----------------------------------------------------------------------//
    // CONSTRUTORS

    /** ProActive compulsory no-args constructor */
    public IMAdminImpl() {
    }

    public IMAdminImpl(IMCore imcore) {
        System.out.println("IMAdmin constructor");
        if (logger.isInfoEnabled()) {
            logger.info("IMAdmin constructor");
        }
        this.imcore = imcore;
    }

    // =======================================================//
    // TEST
    public StringWrapper echo() {
        return new StringWrapper("Je suis le IMAdmin");
    }

    // =======================================================//

    /**
     *
     */
    private File pullPad(File filePAD, Node remoteNode)
        throws Exception {
        File localDest = File.createTempFile(filePAD.getName()
                                                    .replaceFirst("\\.xml\\z",
                    ""), ".xml");

        FileVector filePulled = FileTransfer.pullFile(remoteNode, filePAD,
                localDest);
        filePulled.waitForAll();

        if (logger.isInfoEnabled()) {
            logger.info("name of the pulled file pad : " + localDest);
        }

        return localDest;
    }

    /**
     * @param xmlDescriptor
     * @param remoteNode
     */
    public void deployAllVirtualNodes(File xmlDescriptor, Node remoteNode)
        throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting deploying all virtual nodes of " +
                xmlDescriptor);
        }
        File localCopyPad = null;
        try {
            localCopyPad = this.pullPad(xmlDescriptor, remoteNode);
        } catch (Exception e) {
            logger.warn("Cannot pull the remote file " + xmlDescriptor, e);
            throw new Exception("Cannot pull the remote file " + xmlDescriptor,
                e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Succefully pull the remote file " + xmlDescriptor +
                " to local file " + localCopyPad);
        }

        ProActiveDescriptor pad = ProActive.getProactiveDescriptor(localCopyPad.getPath());
        IMDeploymentFactory.deployAllVirtualNodes(this.imcore,
            localCopyPad.getName(), pad);
    }

    public void deployVirtualNode(File xmlDescriptor, Node remoteNode,
        String vnName) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting deploying all virtual nodes of " +
                xmlDescriptor);
        }
        File localCopyPad = null;
        try {
            localCopyPad = this.pullPad(xmlDescriptor, remoteNode);
        } catch (Exception e) {
            logger.warn("Cannot pull the remote file " + xmlDescriptor, e);
            throw new Exception("Cannot pull the remote file " + xmlDescriptor,
                e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Succefully pull the remote file " + xmlDescriptor +
                " to local file " + localCopyPad);
        }

        ProActiveDescriptor pad = ProActive.getProactiveDescriptor(localCopyPad.getPath());
        IMDeploymentFactory.deployVirtualNode(this.imcore,
            localCopyPad.getName(), pad, vnName);
    }

    public void deployVirtualNodes(File xmlDescriptor, Node remoteNode,
        String[] vnNames) throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info("Starting deploying all virtual nodes of " +
                xmlDescriptor);
        }
        File localCopyPad = null;
        try {
            localCopyPad = this.pullPad(xmlDescriptor, remoteNode);
        } catch (Exception e) {
            logger.warn("Cannot pull the remote file " + xmlDescriptor, e);
            throw new Exception("Cannot pull the remote file " + xmlDescriptor,
                e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("Succefully pull the remote file " + xmlDescriptor +
                " to local file " + localCopyPad);
        }

        ProActiveDescriptor pad = ProActive.getProactiveDescriptor(localCopyPad.getPath());
        IMDeploymentFactory.deployVirtualNodes(this.imcore,
            localCopyPad.getName(), pad, vnNames);
    }

    //----------------------------------------------------------------------//
    // GET DEPLOYED VIRTUAL NODES BY PAD 
    // FOR KILL OR REDEPLOY VIRTUAL NODE(S)
    public HashMap<String, ArrayList<VirtualNode>> getDeployedVirtualNodeByPad() {
        return imcore.getDeployedVirtualNodeByPad();
    }

    //----------------------------------------------------------------------//
    // REDEPLOY
    public void redeploy(String padName) {
        this.imcore.redeploy(padName);
    }

    public void redeploy(String padName, String vnName) {
        this.imcore.redeploy(padName, vnName);
    }

    public void redeploy(String padName, String[] vnNames) {
        this.imcore.redeploy(padName, vnNames);
    }

    //----------------------------------------------------------------------//
    // KILL
    public void killAll() throws ProActiveException {
        this.imcore.killAll();
    }

    public void killPAD(String padName) throws ProActiveException {
        this.imcore.killPAD(padName);
    }

    public void killPAD(String padName, String vnName) {
        this.imcore.killPAD(padName, vnName);
    }

    public void killPAD(String padName, String[] vnNames) {
        this.imcore.killPAD(padName, vnNames);
    }

    //----------------------------------------------------------------------//
    // SHUTDOWN
    public void shutdown() {
        // TODO Auto-generated method stub
    }
}
