/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */

package org.ow2.proactive_grid_cloud_portal.cli.json;

public class PluginView {
    private String pluginName;
    private String pluginDescription;
    private ConfigurableFieldView[] configurableFields;

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public void setPluginDescription(String pluginDescription) {
        this.pluginDescription = pluginDescription;
    }

    public ConfigurableFieldView[] getConfigurableFields() {
        return configurableFields;
    }

    public void setConfigurableFields(ConfigurableFieldView[] configurableFields) {
        this.configurableFields = configurableFields;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Name: ").append(beautifyName(pluginName)).append('\n');
        buffer.append("Description: ").append(pluginDescription).append('\n');
        buffer.append("Class name: ").append(pluginName).append('\n');
        buffer.append("Parameters: ").append("<class name>");
        for (ConfigurableFieldView field : configurableFields) {
            buffer.append(' ').append(field.getName()).append(" [").append(field.getValue()).append(']');
        }
        return buffer.toString();
    }

    private String beautifyName(String name) {
        StringBuffer buffer = new StringBuffer();

        if (name.contains(".")) {
            name = name.substring(name.lastIndexOf(".") + 1);
        }

        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (i == 0) {
                buffer.append(Character.toUpperCase(ch));
            } else if (i > 0 && (Character.isUpperCase(ch) || Character.isDigit(ch))) {
                boolean nextCharInAupperCase = (i < name.length() - 1) &&
                    (Character.isUpperCase(name.charAt(i + 1)) || Character.isDigit(name.charAt(i + 1)));
                if (!nextCharInAupperCase) {
                    buffer.append(" " + ch);
                } else {
                    buffer.append(ch);
                }
            } else {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

}
