/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 *              Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.gui.preferences;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.ow2.proactive.scheduler.Activator;
import org.ow2.proactive.scheduler.gui.Internal;


/**
 * Because of RemoteConnectionPreferences, 
 * Eclipse throws exceptions like crazy if this class does not exist.
 * Otherwise, it would have been named something like RemoteConnectionProperties instead.
 * <p>
 * This holds a static reference to the type/application association used by 
 * the RemoteConnectionPreferences editor
 * 
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

    private static Properties remoteProps = null;

    /**
     * @return properties mapping a lowercase type name to a path to a local application used
     *     to connect to this application type, ie ("vnc", "/usr/bin/vncviewer"). 
     *     Also, this should be platform specific
     */
    public static Properties getRemoteConnectionProperties() {
        if (remoteProps == null) {
            loadRemoteConnectionProperties();
        }
        return remoteProps;
    }

    private static void loadRemoteConnectionProperties() {
        if (remoteProps != null) {
            return;
        }
        Map<String, String> vis = null;
        Properties props = new Properties();

        String sys = System.getProperty("os.name").toLowerCase();
        if (sys.indexOf("win") >= 0) {
            vis = Internal.winRemoteConnAssociation;
        } else if (sys.indexOf("mac") >= 0) {
            vis = Internal.macRemoteConnAssociation;
        } else if (sys.indexOf("nix") >= 0 || sys.indexOf("nux") >= 0) {
            vis = Internal.unixRemoteConnAssociation;
        }

        /** set default values */
        for (Entry<String, String> assoc : vis.entrySet()) {
            props.setProperty(assoc.getKey(), assoc.getValue());
        }

        if (!RemoteConnectionPreferences.remoteConnPropsFile.exists()) {
            try {
                RemoteConnectionPreferences.remoteConnPropsFile.createNewFile();
            } catch (IOException e) {
                Activator.log(IStatus.ERROR, "Failed to create property file " +
                    RemoteConnectionPreferences.remoteConnPropsFile.getAbsolutePath(), e);
            }
        }

        /** override with the content of the file */
        try {
            props.load(new FileInputStream(RemoteConnectionPreferences.remoteConnPropsFile));
        } catch (IOException e) {
            Activator.log(IStatus.ERROR,
                    "Failed to load remote connection application association property file in " +
                        RemoteConnectionPreferences.remoteConnPropsFile.getAbsolutePath(), e);
        }

        remoteProps = props;
    }

    @Override
    public void initializeDefaultPreferences() {
        loadRemoteConnectionProperties();
    }
}
