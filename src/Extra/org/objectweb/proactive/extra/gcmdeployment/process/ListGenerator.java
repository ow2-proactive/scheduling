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
package org.objectweb.proactive.extra.gcmdeployment.process;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ListGenerator {
    final static protected Pattern plainHostname = Pattern.compile("[a-z0-9]+");
    final static protected Pattern simpleInterval = Pattern.compile(
            "\\[([\\d\\-,; ]+)\\]");
    final static protected Pattern subInterval = Pattern.compile(
            "(\\d+)-(\\d+);?(\\d+)?");
    final static protected String SUB_INTERVAL_SPLIT_REGEXP = " *, *";

    static public List<String> generateNames(String nameSetDefinition) {
        StringTokenizer tokenizer = new StringTokenizer(nameSetDefinition);

        List<String> names = new ArrayList<String>();

        while (tokenizer.hasMoreTokens()) {
            String nextToken = tokenizer.nextToken();

            names.addAll(generateNamesOneToken(nextToken));
        }
        return names;
    }

    static private List<String> generateNamesOneToken(String nameSetDefinition) {
        List<String> res = null;
        Matcher matcher = simpleInterval.matcher(nameSetDefinition);

        // check for simple interval first
        //
        if (matcher.find()) {
            String prefix = nameSetDefinition.substring(0, matcher.start());
            String intervalDef = matcher.group(1);
            String exclusionInterval = null;
            int lastMatchedEnd = matcher.end();

            // a simple interval was found, do another match for the same regexp
            // and check if there's a '^' preceding this other match - if so,
            // we have a range exclusion interval (e.g. 'foo[0-9]^[5-8]')
            //
            if (lastMatchedEnd < (nameSetDefinition.length())) {
                if (nameSetDefinition.charAt(lastMatchedEnd) == '^') {
                    // there should be an exclusion interval right after this
                    //
                    boolean newIntervalFound = matcher.find();

                    // if there wasn't, or the interval doesn't start right after the '^',
                    // throw an exception
                    //
                    if (!newIntervalFound ||
                            (matcher.start() != (lastMatchedEnd + 1))) {
                        throw new IllegalArgumentException(
                            "misformed interval def : " + nameSetDefinition);
                    }
                    exclusionInterval = matcher.group(1);
                    lastMatchedEnd = matcher.end();
                }
            }

            String suffix = "";
            if (lastMatchedEnd < (nameSetDefinition.length() - 1)) {
                suffix = nameSetDefinition.substring(lastMatchedEnd);
            }

            String[] subIntervals = intervalDef.split(SUB_INTERVAL_SPLIT_REGEXP);
            String[] subExclusionIntervals = (exclusionInterval != null)
                ? exclusionInterval.split(SUB_INTERVAL_SPLIT_REGEXP) : null;

            res = getSubNames(prefix, suffix, subIntervals,
                    subExclusionIntervals);
        } else {
            // it doesn't look like an interval ('hostname[0-9]'), but we still need to make sure
            // it looks like a normal hostname
            //
            if (plainHostname.matcher(nameSetDefinition).matches()) {
                res = new ArrayList<String>();
                res.add(nameSetDefinition);
            } else {
                throw new IllegalArgumentException("misformed interval def : " +
                    nameSetDefinition);
            }
        }

        return res;
    }

    static private List<String> getSubNames(String prefix, String suffix,
        String[] subIntervalsDefs, String[] exclusionIntervalsDefs) {
        if (suffix == null) {
            suffix = "";
        }

        List<String> res = new ArrayList<String>();

        NumberChecker numberChecker = null;

        if (exclusionIntervalsDefs != null) {
            numberChecker = getNumberChecker(exclusionIntervalsDefs);
        }

        for (int i = 0; i < subIntervalsDefs.length; ++i) {
            String subIntervalDef = subIntervalsDefs[i];
            if (subIntervalDef.indexOf('-') > 0) {
                generateNames(prefix, suffix, subIntervalDef, numberChecker, res);
            } else { // subIntervalDef is actually a single integer
                     // check if that integer is within allowed interval                
                if ((numberChecker == null) ||
                        numberChecker.check(Integer.parseInt(subIntervalDef))) {
                    res.add(prefix + subIntervalDef + suffix);
                }
            }
        }

        return res;
    }

    /**
     * Returns the list of names given a single interval pattern
     * e.g. :
     * node10
     * node[0-10]      => node0, node1... node10
     * node[1,2,3]     => node1, node2, node3
     * node[0-10;2]    => node0, node2, node4, node6, node8, node10
     * node[1,2,10-20] => node1, node2, node10, node11... node20
     *
     * or with an exclusion interval :
     *
     * node[0-10]^[2-4]   => node0, node1, node5, node6... node10
     * node[0-10]^[2-4,8] => node0, node1, node5, node6, node7, node9, node10
     *
     * @param nameSetDefinition a set definition in the form described above
     * @return
     */
    static private void generateNames(String prefix, String suffix,
        String subIntervalDef, NumberChecker numberChecker, List<String> names) {
        Interval interval = new Interval(subIntervalDef);

        String paddingFormat = getPadding(interval.startStr);

        for (int n = interval.start; n <= interval.end; n += interval.step) {
            if ((numberChecker != null) && !numberChecker.check(n)) {
                continue;
            }

            String formattedName = MessageFormat.format("{0}{1,number," +
                    paddingFormat + "}{2}", prefix, n, suffix);
            names.add(formattedName);
        }
    }

    static private String getPadding(String group) {
        if (group.charAt(0) == '0') {
            StringBuilder res = new StringBuilder();
            for (int i = 0; i < group.length(); ++i) {
                res.append('0');
            }
            return res.toString();
        }

        return "#";
    }

    static private NumberChecker getNumberChecker(String[] exclusionIntervals) {
        NumberChecker res = new NumberChecker();

        for (String intervalDef : exclusionIntervals) {
            if (intervalDef.indexOf('-') > 0) {
                Interval interval = new Interval(intervalDef);
                res.addExclusionInterval(interval.start, interval.end);
            } else {
                res.addExclusionValue(Integer.parseInt(intervalDef));
            }
        }

        return res;
    }

    /**
     * Check if a given host number is part of the 'excluded' list of an interval
     * (e.g. [1-10]^[5-7] )
     *
     * @author glaurent
     *
     */
    static protected class NumberChecker {
        List<int[]> intervals;
        List<Integer> values;

        public NumberChecker() {
            intervals = new ArrayList<int[]>();
            values = new ArrayList<Integer>();
        }

        public void addExclusionInterval(int start, int end) {
            intervals.add(new int[] { start, end });
        }

        public void addExclusionValue(int val) {
            values.add(val);
        }

        public boolean check(int candidate) {
            if (values.contains(candidate)) {
                return false;
            }

            for (int[] interval : intervals) {
                if ((candidate >= interval[0]) && (candidate <= interval[1])) {
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Parses an interval from its definition string "[n-m;s]"
     *
     * @author glaurent
     *
     */
    static protected class Interval {
        public int start;
        public int end;
        public int step;
        public String startStr;
        public String endStr;

        public Interval(String intervalDef) {
            Matcher matcher = subInterval.matcher(intervalDef);

            if (!matcher.matches()) {
                throw new IllegalArgumentException("misformed interval def : " +
                    intervalDef);
            }

            startStr = matcher.group(1);
            endStr = matcher.group(2);
            start = Integer.parseInt(startStr);
            end = Integer.parseInt(endStr);
            step = (matcher.group(3) != null)
                ? Integer.parseInt(matcher.group(3)) : 1;

            if (start >= end) {
                throw new IllegalArgumentException(
                    "wrong range : start >= end in " + intervalDef);
            }
        }
    }
}
