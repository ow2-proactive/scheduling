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
package org.objectweb.proactive.core.xml;

import java.io.Serializable;

import org.objectweb.proactive.core.descriptor.legacyparser.ProActiveDescriptorConstants;


/**
 * This class specifies different Variable Contract types, and their behaviour
 *
 * @author The ProActive Team
 * @since ProActive 3.0 (December 2005)
 */
public class VariableContractType implements Serializable {
    static final public VariableContractType DescriptorVariable = new VariableContractType(0,
            new String[] { "Descriptor", "Program" },
            new String[] { "Descriptor" }, new String[] { "Program" });
    static final public VariableContractType ProgramVariable = new VariableContractType(1,
            new String[] { "Program", "Descriptor" },
            new String[] { "Program" }, new String[] { "Descriptor" });
    static final public VariableContractType JavaPropertyVariable = new VariableContractType(2,
            new String[] { "JavaProperty" }, new String[] { "JavaProperty" },
            new String[] { "Program", "Descriptor" });
    static final public VariableContractType ProgramDefaultVariable = new VariableContractType(3,
            new String[] { "Descriptor", "Program" },
            new String[] { "Descriptor", "Program" }, new String[0]);
    static final public VariableContractType DescriptorDefaultVariable = new VariableContractType(4,
            new String[] { "Program", "Descriptor" },
            new String[] { "Descriptor", "Program" }, new String[0]);
    static final public VariableContractType JavaPropertyDescriptorDefault = new VariableContractType(5,
            new String[] { "JavaProperty", "Descriptor", "Program" },
            new String[] { "JavaProperty", "Descriptor", "Program" },
            new String[] { "Program" });
    static final public VariableContractType JavaPropertyProgramDefault = new VariableContractType(6,
            new String[] { "JavaProperty", "Program", "Descriptor" },
            new String[] { "JavaProperty", "Program", "Descriptor" },
            new String[] { "Descriptor" });
    private int type; //Type internal identefier
    private String[] priority; //The lower indexed element has higher priority
    private String[] setAbility; //Who can set a non empty value
    private String[] setEmptyAbility; //Who can set an empty value

    /**
     * Class constructor.
     * @param type An identifier of this type
     * @param priority An array with the names of the elements. The higher priority is defined as the lower indexed element.
     * @param setAbility An array with the names of the elements that can set a non empty value.
     * @param setEmptyAbility An array with the names of the elements that can set an empty value.
     */
    private VariableContractType(int type, String[] priority,
        String[] setAbility, String[] setEmptyAbility) {
        this.type = type;
        this.priority = priority;
        this.setAbility = setAbility;
        this.setEmptyAbility = setEmptyAbility;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + type;
        return result;
    }

    /**
     * Tests if two variable types are the same.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VariableContractType)) {
            return false;
        }
        final VariableContractType other = (VariableContractType) obj;
        if (type != other.type) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        switch (type) {
        case 0:
            return ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG;
        case 1:
            return ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG;
        case 2:
            return ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG;
        case 3:
            return ProActiveDescriptorConstants.VARIABLES_PROGRAM_DEFAULT_TAG;
        case 4:
            return ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG;
        case 5:
            return ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG;
        case 6:
            return ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG;
        default:
            return "UnkownVariable";
        }
    }

    /**
     * @param type The String representing the types name.
     * @return The VariableType The java object representing this type.
     */
    public static VariableContractType getType(String type) {
        if (type.equals(ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_TAG)) {
            return DescriptorVariable;
        }
        if (type.equals(ProActiveDescriptorConstants.VARIABLES_PROGRAM_TAG)) {
            return ProgramVariable;
        }
        if (type.equals(ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_TAG)) {
            return JavaPropertyVariable;
        }
        if (type.equals(
                    ProActiveDescriptorConstants.VARIABLES_PROGRAM_DEFAULT_TAG)) {
            return ProgramDefaultVariable;
        }
        if (type.equals(
                    ProActiveDescriptorConstants.VARIABLES_DESCRIPTOR_DEFAULT_TAG)) {
            return DescriptorDefaultVariable;
        }
        if (type.equals(
                    ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_DESCRIPTOR_TAG)) {
            return JavaPropertyDescriptorDefault;
        }
        if (type.equals(
                    ProActiveDescriptorConstants.VARIABLES_JAVAPROPERTY_PROGRAM_TAG)) {
            return JavaPropertyProgramDefault;
        }

        return null;
    }

    /**
     * Determines if a non empty value can be set from a source.
     * @param from A string with the name of the source
     * @return True if  "from" has non empty set ability, false otherwise.
     */
    public boolean hasSetAbility(String from) {
        for (int i = 0; i < setAbility.length; i++)
            if (from.equalsIgnoreCase(setAbility[i])) {
                return true;
            }

        return false;
    }

    /**
     * Determines if an empty value can be set from a source.
     * @param from A string with the name of the source
     * @return True if  "from" has empty set ability, false otherwise.
     */
    public boolean hasSetEmptyAbility(String from) {
        for (int i = 0; i < setEmptyAbility.length; i++)
            if (from.equalsIgnoreCase(setEmptyAbility[i])) {
                return true;
            }

        return false;
    }

    /**
     * Compares two sources and determines who has higher priority to set the value.
     * @param current The last source to set a value.
     * @param candidate The current source trying to replace a value.
     * @return True if candidate has better or equal priority than current. False otherwise.
     */
    public boolean hasPriority(String current, String candidate) {
        int i;
        int j;

        //Find the priority of the current set value
        for (i = 0; i < priority.length; i++)
            if (current.equalsIgnoreCase(priority[i])) {
                break;
            }

        //Find the priority of the replacing candidate
        for (j = 0; j < priority.length; j++)
            if (candidate.equalsIgnoreCase(priority[j])) {
                break;
            }

        return j <= i;
    }

    String getEmptyErrorMessage(String name) {
        String canSetValue = "nobody";
        for (int i = 0; i < setAbility.length; i++) {
            canSetValue = setAbility[i] + " ";
        }

        return "Empty value for variable" + this.toString() + " " + name +
        " value can be set by: " + canSetValue;
    }
}
