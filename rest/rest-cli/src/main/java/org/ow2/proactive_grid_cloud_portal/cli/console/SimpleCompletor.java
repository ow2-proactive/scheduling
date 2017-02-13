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
package org.ow2.proactive_grid_cloud_portal.cli.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import jline.console.completer.Completer;


/**
 *  <p>
 *  A simple {@link Completer} implementation that handles a pre-defined
 *  list of completion words.
 *  </p>
 *
 *  <p>
 *  Example usage:
 *  </p>
 *  <pre>
 *  myConsoleReader.addCompletor (new SimpleCompletor (new String [] { "now", "yesterday", "tomorrow" }));
 *  </pre>
 *
 *  @author  <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 */
public class SimpleCompletor implements Completer, Cloneable {
    /**
     *  The list of candidates that will be completed.
     */
    SortedSet candidates;

    /**
     *  A delimiter to use to qualify completions.
     */
    String delimiter;

    final SimpleCompletorFilter filter;

    /**
     *  Create a new SimpleCompletor with a single possible completion
     *  values.
     */
    public SimpleCompletor(final String candidateString) {
        this(new String[] { candidateString });
    }

    /**
     *  Create a new SimpleCompletor with a list of possible completion
     *  values.
     */
    public SimpleCompletor(final String[] candidateStrings) {
        this(candidateStrings, null);
    }

    public SimpleCompletor(final String[] strings, final SimpleCompletorFilter filter) {
        this.filter = filter;
        setCandidateStrings(strings);
    }

    /**
     *  Complete candidates using the contents of the specified Reader.
     */
    public SimpleCompletor(final Reader reader) throws IOException {
        this(getStrings(reader));
    }

    /**
     *  Complete candidates using the whitespearated values in
     *  read from the specified Reader.
     */
    public SimpleCompletor(final InputStream in) throws IOException {
        this(getStrings(new InputStreamReader(in)));
    }

    private static String[] getStrings(final Reader in) throws IOException {
        final Reader reader = (in instanceof BufferedReader) ? in : new BufferedReader(in);

        List words = new LinkedList();
        String line;

        while ((line = ((BufferedReader) reader).readLine()) != null) {
            for (StringTokenizer tok = new StringTokenizer(line); tok.hasMoreTokens(); words.add(tok.nextToken())) {
                ;
            }
        }

        return (String[]) words.toArray(new String[words.size()]);
    }

    public int complete(final String buffer, final int cursor, final List clist) {
        String start = (buffer == null) ? "" : buffer;

        SortedSet matches = candidates.tailSet(start);

        for (Iterator i = matches.iterator(); i.hasNext();) {
            String can = (String) i.next();

            if (!(can.startsWith(start))) {
                break;
            }

            if (delimiter != null) {
                int index = can.indexOf(delimiter, cursor);

                if (index != -1) {
                    can = can.substring(0, index + 1);
                }
            }

            clist.add(can);
        }

        if (clist.size() == 1) {
            clist.set(0, ((String) clist.get(0)) + " ");
        }

        // the index of the completion is always from the beginning of
        // the buffer.
        return (clist.size() == 0) ? (-1) : 0;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public String getDelimiter() {
        return this.delimiter;
    }

    public void setCandidates(final SortedSet candidates) {
        if (filter != null) {
            TreeSet filtered = new TreeSet();

            for (Iterator i = candidates.iterator(); i.hasNext();) {
                String element = (String) i.next();
                element = filter.filter(element);

                if (element != null) {
                    filtered.add(element);
                }
            }

            this.candidates = filtered;
        } else {
            this.candidates = candidates;
        }
    }

    public SortedSet getCandidates() {
        return Collections.unmodifiableSortedSet(this.candidates);
    }

    public void setCandidateStrings(final String[] strings) {
        setCandidates(new TreeSet(Arrays.asList(strings)));
    }

    public void addCandidateString(final String candidateString) {
        final String string = (filter == null) ? candidateString : filter.filter(candidateString);

        if (string != null) {
            candidates.add(string);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     *  Filter for elements in the completor.
     *
     *  @author  <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
     */
    public interface SimpleCompletorFilter {
        /**
         *  Filter the specified String. To not filter it, return the
         *  same String as the parameter. To exclude it, return null.
         */
        String filter(String element);
    }

    public static class NoOpFilter implements SimpleCompletorFilter {
        public String filter(final String element) {
            return element;
        }
    }
}
