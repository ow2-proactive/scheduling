/*
 * ProActive Parallel Suite(TM):
 * The Open Source library for parallel and distributed
 * Workflows & Scheduling, Orchestration, Cloud Automation
 * and Big Data Analysis on Enterprise Grids & Clouds.
 *
 * Copyright (c) 2007 - 2017 ActiveEon
 * Contact: contact@activeeon.com
 *
 * This library is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation: version 3 of
 * the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 */
package org.ow2.proactive.resourcemanager.nodesource.common;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.log4j.Logger;
import org.objectweb.proactive.annotation.PublicAPI;
import org.ow2.proactive.resourcemanager.exception.RMException;
import org.ow2.proactive.utils.FileToBytesConverter;


/**
 *
 * A descriptor of pluggable policies and infrastructure manages.
 * Used to dynamically obtain a meta information about the service
 * without having a direct link to the service.
 *
 */
@PublicAPI
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "pluginDescriptor")
public class PluginDescriptor implements Serializable {

    @XmlTransient
    private final static Logger logger = Logger.getLogger(PluginDescriptor.class);

    private String pluginName;

    private String pluginDescription;

    private Collection<ConfigurableField> configurableFields = new LinkedList<>();

    private Map<String, String> defaultValues;

    private Map<Integer, String> sectionDescriptions = new HashMap<>();

    private Map<String, String> meta = new HashMap<>();

    public PluginDescriptor() {
    }

    public PluginDescriptor(Class<?> cls, Map<String, String> defaultValues) {
        try {
            // The context class loader of the thread for instantiating plugin object should always be the same as plugin class loader
            Thread.currentThread().setContextClassLoader(cls.getClassLoader());
            Object instance = cls.newInstance();
            pluginName = cls.getName();
            this.defaultValues = defaultValues;

            findSectionDescriptions(cls, instance);
            findConfigurableFileds(cls, instance);
            findMeta(cls, instance);

            Method getDescription = cls.getMethod("getDescription");
            if (getDescription != null) {
                pluginDescription = (String) getDescription.invoke(instance);
            }

        } catch (Exception e) {
            logger.error("Error when reading configurable fields", e);
        }
    }

    /**
     * Create a plugin descriptor populated with the given parameters
     */
    public PluginDescriptor(Class<?> cls, Object[] parameters) {
        this(cls, new HashMap<>());

        this.validateParametersOrFail(cls, parameters);

        Object parameterObject;
        int fieldIndex = 0;

        for (ConfigurableField field : this.configurableFields) {

            parameterObject = parameters[fieldIndex++];
            this.setFieldToStringValueOf(field, parameterObject);
        }
    }

    private void validateParametersOrFail(Class<?> cls, Object[] parameters) {
        if (parameters == null || this.configurableFields.size() > parameters.length) {
            throw new IllegalArgumentException("There are not enough parameters to populate the plugin descriptor of " +
                                               cls.getSimpleName());
        }
    }

    private void setFieldToStringValueOf(ConfigurableField field, Object parameterObject) {
        if (parameterObject instanceof byte[]) {
            field.setValue(new String((byte[]) parameterObject));
        } else {
            field.setValue(String.valueOf(parameterObject));
        }
    }

    private void findSectionDescriptions(Class<?> cls, Object instance) {
        try {
            Method getSectionDescriptions = cls.getMethod("getSectionDescriptions");
            Map<Integer, String> sectionDescriptions = (Map<Integer, String>) getSectionDescriptions.invoke(instance);
            this.sectionDescriptions.putAll(sectionDescriptions);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.debug("Could not load `getSectionDescriptions`.", e);
        }
    }

    private void findMeta(Class<?> cls, Object instance) {
        try {
            Method getMetaMethod = cls.getMethod("getMeta");
            Map<String, String> metaInfo = (Map<String, String>) getMetaMethod.invoke(instance);
            this.meta.putAll(metaInfo);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            logger.debug("Could not load `getMeta`.", e);
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
                String value = valueObj == null ? (this.defaultValues.get(name) != null ? this.defaultValues.get(name)
                                                                                        : "")
                                                : valueObj.toString();
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
     * @return the default values set by the rm core for this plugin
     */
    public Map<String, String> getDefaultValues() {
        return this.defaultValues;
    }

    public Map<Integer, String> getSectionDescriptions() {
        return sectionDescriptions;
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
        int configurableFieldsSize = configurableFields.size();
        List<Object> resultParams = new ArrayList<>(configurableFieldsSize);

        if (parameters.length != configurableFieldsSize) {
            throw new RMException("Incorrect number of parameters: expected " + configurableFieldsSize + ", provided " +
                                  parameters.length);
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
                boolean nextCharInAupperCase = (i < name.length() - 1) && (Character.isUpperCase(name.charAt(i + 1)) ||
                                                                           Character.isDigit(name.charAt(i + 1)));
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
                params += field.getName();
                //we add a default value
                if (!field.getValue().equals("")) {
                    params += "[" + field.getValue() + "]";
                }
                params += " ";
            }
            result += "Parameters: <class name> " + params + "\n";
        }

        return result;
    }

    public Map<String, String> getMeta() {
        return meta;
    }
}
