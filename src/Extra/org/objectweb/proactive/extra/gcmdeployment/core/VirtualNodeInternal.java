package org.objectweb.proactive.extra.gcmdeployment.core;

import org.objectweb.proactive.extra.gcmdeployment.GCMApplication.FileTransferBlock;


public interface VirtualNodeInternal extends VirtualNode {

    /**
     * Adds a File Transfer Block to be executed before a node
     * is returned to the appplication
     *
     * @param ftb A File Transfer Block
     */
    public void addFileTransfertBlock(FileTransferBlock ftb);

    /**
     * Checks that all required informations are here.
           *
     * Checked things are notably:
     * <ul>
     *         <li>At least one resourceProvider at root level</li>
     *         <li>At least one resourceProvider inside each VirtualNode</li>
     * </ul>
     *
     * @throws IllegalStateException If something is missing
     */
    public void checkDirectMode() throws IllegalStateException;
}
