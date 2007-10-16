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
package org.objectweb.proactive.core.process.filetransfer;


/**
 * Remote File Copy (RCP) module.
 *
 * @author  ProActive Team
 * @version 1.0,  2005/08/26
 * @since   ProActive 2.3
 */
public class RemoteFileCopy extends SecureCopyProtocol {
    public RemoteFileCopy(String name) {
        super(name);
        super.COMMAND = "rcp";
    }

    public static void main(String[] args) {
        //Default is RCP_COMMAND
        FileTransferWorkShop fts = new FileTransferWorkShop("rcp");

        FileTransferDefinition ft1 = new FileTransferDefinition("1");
        FileTransferDefinition ft2 = new FileTransferDefinition("2");

        ft1.addFile("FileTransferTest.txt", "FileTransferTest-dest.txt");
        ft1.addFile("FileTransferTest.txt", "FileTransferTest.txt");
        ft1.addFile("heterofile1A", "heterofile1B");
        ft1.addDir("FileTransferTestDir", "FileTransferTestDir");
        ft1.addDir("heterodir1A", "heterodir1B");

        ft2.addFile("homofile2", "homofile2");
        ft2.addFile("heterofile2A", "heterofile2B");
        ft2.addDir("homodir2", "homodir2");
        ft2.addDir("heterodir2A", "heterodir2B");

        fts.addFileTransfer(ft1);
        fts.addFileTransfer(ft2);
        fts.setFileTransferCopyProtocol("processDefault");
        fts.dstInfoParams.setInfoParameter("username", "mleyton");
        fts.dstInfoParams.setInfoParameter("hostname", "plugrid1.inria.fr");
        fts.dstInfoParams.setInfoParameter("prefix", "/0/user/mleyton");
        fts.srcInfoParams.setInfoParameter("prefix", "/home/mleyton");

        CopyProtocol[] cp = fts.getCopyProtocols();

        System.out.println("Copying protocols:");
        for (int i = 0; i < cp.length; i++) {
            System.out.println(cp[i].getProtocolName());
            System.out.println(cp[i].getClass());

            cp[i].startFileTransfer();
        }
    }
}
