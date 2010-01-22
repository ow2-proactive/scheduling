/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
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
package org.ow2.proactive.resourcemanager.nodesource.common;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 *
 * A descriptor of pluggable policies and infrastructure manages.
 * Used to dynamically obtain a meta information about the service
 * without having a direct link to the service.
 *
 */
public class PluginDescriptor implements Serializable {

    private String pluginName;
    private String pluginDescription;

    private Collection<ConfigurableField> configurableFields = new LinkedList<ConfigurableField>();

    public PluginDescriptor() {
    }

    public PluginDescriptor(Class<?> cls) {
        try {
            Object instance = cls.newInstance();
            pluginName = cls.getName();

            findConfigurableFileds(cls, instance);

            Method getDescription = cls.getMethod("getDescription");
            if (getDescription != null) {
                pluginDescription = (String) getDescription.invoke(instance);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * Looks through cls which represents a plugin. Collects a configurable
     * skeleton of the plugin.
     */
    private void findConfigurableFileds(Class<?> cls, Object instance) {
        if (cls.getSuperclass() != null && cls.getSuperclass() != Object.class) {
            findConfigurableFileds(cls.getSuperclass(), instance);
        }

        for (Field f : cls.getDeclaredFields()) {
            Configurable configurable = f.getAnnotation(Configurable.class);
            if (configurable != null) {
                String name = f.getName();
                f.setAccessible(true);
                Object valueObj = null;
                try {
                    valueObj = f.get(instance);
                } catch (Exception e) {
                }

                String value = valueObj == null ? "" : valueObj.toString();
                configurableFields.add(new ConfigurableField(name, value, configurable));
            }
        }
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginDescription() {
        return pluginDescription;
    }

    public Collection<ConfigurableField> getConfigurableFields() {
        return configurableFields;
    }

    /**
     * Packs parameters inputed by user into appropriate parameters set required for this plugin.
     * Performs some operations such as file loading on user side.
     *
     * @param parameters input parameters
     * @return output parameters
     * @throws RMException when error occurs
     */
    public Object[] packParameters(Object[] parameters) throws RMException {
        List<Object> resultParams = new ArrayList<Object>();

        if (parameters.length != configurableFields.size()) {
            throw new RMException("Incorrect number of parameters: expected " + configurableFields.size() +
                ", actually " + parameters.length);
        }

        int counter = 0;

        for (ConfigurableField field : configurableFields) {

            Object value = parameters[counter++];

            Configurable configurable = field.getMeta();
            boolean credentialsFilePath = configurable.credential() && value instanceof String;

            if (configurable.fileBrowser() || credentialsFilePath) {
                try {
                    if (value.toString().length() > 0) {
                        value = FileToBytesConverter.convertFileToByteArray(new File(value.toString()));
                    } else {
                        // in case if file path is not specified propagate null to plugin
                        // it will decide then if it's acceptable or not
                        value = null;
                    }
                } catch (IOException e) {
                    throw new RMException("Cannot load file", e);
                }
            }

            resultParams.add(value);
        }
        return resultParams.toArray();
    }

    public static String beautifyName(String name) {
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

    @Override
    public String toString() {
        String result = "Name: " + beautifyName(pluginName) + "\n";
        result += "Description: " + pluginDescription + "\n";
        result += "Class name: " + pluginName + "\n";

        if (configurableFields.size() > 0) {
            String params = "";
            for (ConfigurableField field : configurableFields) {
                params += field.getName() + " ";
            }
            result += "Parameters: <class name> " + params + "\n";
        }

        return result;
    }
}
