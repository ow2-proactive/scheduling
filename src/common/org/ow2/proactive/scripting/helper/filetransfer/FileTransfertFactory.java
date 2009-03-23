/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2008 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scripting.helper.filetransfer;

import org.ow2.proactive.scripting.helper.filetransfer.driver.*;
import org.ow2.proactive.scripting.helper.filetransfer.initializer.*;


/**
 * FileTransfertFactory...
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class FileTransfertFactory {

    /**
     *
     * @param myInit
     * @return
     */
    public static FileTransfertDriver getDriver(FileTransfertInitializer myInit) {

        if (myInit.getDriverClass() != null) {
            try {
                //System.out.println(myInit.getDriverClass().getName());

                FileTransfertDriver ftDriver = (FileTransfertDriver) (myInit.getDriverClass().newInstance());
                ftDriver.init(myInit);
                return ftDriver;

            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    //	//TODO: to check this
    //	public FileTransfertInitializerFTP getInitializerFTP(String host, String user, String pass, int port, Class<? extends FileTransfertDriver> driver, boolean useBinaryMode){
    //		return new FileTransfertInitializerFTP(host, user, pass, port, driver, useBinaryMode);
    //	}

}
