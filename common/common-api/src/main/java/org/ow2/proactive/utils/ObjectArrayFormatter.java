/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2015 INRIA/University of
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
 *  Initial developer(s):               The ActiveEon Team
 *                        http://www.activeeon.com/
 *  Contributor(s):
 *
 * ################################################################
 * $$ACTIVEEON_INITIAL_DEV$$
 */
package org.ow2.proactive.utils;

import java.util.ArrayList;
import java.util.List;


/**
 * ObjectArrayFormatter is used to format (as string) an object in ordered column.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 2.0
 */
public class ObjectArrayFormatter {

    private List<String> title;
    private List<List<String>> lines = new ArrayList<>();
    private int spaces;
    private int maxColumnLength = 20;

    /**
     * Set the title value to the given title value.
     * Must have the same number of elements than each added lines.
     *
     * @param title the title to set
     */
    public void setTitle(List<String> title) {
        this.title = title;
    }

    /**
     * Add the given lines value to existing lines.
     * Must have the same number of elements than the titles.
     *
     * @param lines the lines to add
     */
    public void addLine(List<String> lines) {
        if (lines == null || lines.size() == 0) {
            throw new IllegalArgumentException("Lines must be a non-empty list.");
        }
        this.lines.add(lines);
    }

    /**
     * Add a empty line to the list of lines.
     * This will be printed as a newline.
     */
    public void addEmptyLine() {
        this.lines.add(null);
    }

    /**
     * Set the space value to the given space value.
     * It represent the space between each column.
     *
     * @param space the space to set
     */
    public void setSpace(int spaces) {
        if (spaces < 1) {
            throw new IllegalArgumentException("spaces must be a positive value.");
        }
        this.spaces = spaces;
    }

    /**
     * Set the maxColumnLength value to the given maxColumnLength value
     *
     * @param maxColumnLength the maxColumnLength to set
     */
    public void setMaxColumnLength(int maxColumnLength) {
        if (maxColumnLength < 1) {
            throw new IllegalArgumentException("maxColumnLength must be a positive");
        }
        this.maxColumnLength = maxColumnLength;
    }

    /**
     * Get the columned string according to the titles, lines and spaces.
     */
    public String getAsString() {
        //if title does not exist
        if (title == null || title.size() == 0) {
            throw new IllegalStateException("Title array does not contain anything !");
        }
        //check that each lines has the same length
        for (List<String> l : lines) {
            if (l != null && l.size() != title.size()) {
                throw new IllegalStateException(
                    "One of the line is not as long as the other or the title array !");
            }
        }
        //init length array with title length
        int[] columnLengths = new int[title.size()];
        for (int i = 0; i < columnLengths.length; i++) {
            columnLengths[i] = title.get(i).length();
        }
        //get max length for each fields
        for (List<String> l : lines) {
            if (l != null) {
                int i = 0;
                for (String s : l) {
                    columnLengths[i] = (s.length() > columnLengths[i]) ? s.length() : columnLengths[i];
                    i++;
                }
            }
        }
        //make the maxColumnLength the top limit of the int array and add the separator number of spaces
        for (int i = 0; i < columnLengths.length; i++) {
            if (columnLengths[i] > maxColumnLength) {
                columnLengths[i] = maxColumnLength;
            }
            columnLengths[i] += spaces;
        }
        //write the string with the computed limits
        StringBuilder sb = new StringBuilder();
        //print title line
        sb.append("\t");
        for (int i = 0; i < title.size(); i++) {
            sb.append(String
                    .format(" %1$-" + columnLengths[i] + "s", cutNchar(title.get(i), maxColumnLength)));
        }
        sb.append(System.lineSeparator());
        for (List<String> l : lines) {
            if (l == null) {
                sb.append(System.lineSeparator());
            } else {
                sb.append("\t");
                for (int i = 0; i < l.size(); i++) {
                    sb.append(String.format(" %1$-" + columnLengths[i] + "s", cutNchar(l.get(i),
                            maxColumnLength)));
                }
                sb.append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    /**
     * Cut the given string to the specified number of character
     *
     * @param str the string to cut.
     * @param nbChar the maximum length of the string to be returned
     * @return a string that is the same as the given one if str.length() is lesser or equals to nbChar,
     * 			otherwise a shortcut string endind with '...'
     */
    private String cutNchar(String str, int nbChar) {
        if (str == null) {
            return "";
        }
        nbChar--;//use to have a space after the returned string
        if (str.length() <= nbChar) {
            return str;
        }
        return str.substring(0, nbChar - 3) + "...";
    }

}
