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
package performancetests.helper;

import static org.grep4j.core.Grep4j.constantExpression;
import static org.grep4j.core.Grep4j.grep;
import static org.grep4j.core.fluent.Dictionary.on;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.grep4j.core.model.Profile;
import org.grep4j.core.model.ProfileBuilder;
import org.grep4j.core.result.GrepResult;
import org.grep4j.core.result.GrepResults;


public class LogProcessor {

    static final String LINE_SAMPLE = "[2017-11-11 11:11:11,111 ";

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

    static private String pathToLogFile = System.getProperty("pa.rm.home") + File.separatorChar + "logs" +
                                          File.separatorChar + "Scheduler-tests.log";

    public static String getPathToLogFile() {
        return pathToLogFile;
    }

    public static void setPathToLogFile(String pathToLogFile) {
        LogProcessor.pathToLogFile = pathToLogFile;
    }

    public static int numberOfLinesWhichMatch(String matcher) {
        Profile localProfile = ProfileBuilder.newBuilder()
                                             .name("Local server log" + System.currentTimeMillis())
                                             .filePath(getPathToLogFile())
                                             .onLocalhost()
                                             .build();
        final GrepResults grepResults = grep(constantExpression(matcher), on(localProfile));
        return grepResults.totalLines();
    }

    public static String getLastLineThatMatch(String matcher) {
        Profile localProfile = ProfileBuilder.newBuilder()
                                             .name("Local server log" + System.currentTimeMillis())
                                             .filePath(getPathToLogFile())
                                             .onLocalhost()
                                             .build();
        final Iterator<GrepResult> iterator = grep(constantExpression(matcher), on(localProfile)).iterator();
        String lastLine = null;

        while (iterator.hasNext()) {
            lastLine = iterator.next().getText();
        }

        if (lastLine != null) {
            return lastLine;
        } else {
            throw new RuntimeException("There are no lines in " + getPathToLogFile() + " which matches " + matcher);
        }
    }

    public static String getFirstLineThatMatch(String matcher) {
        Profile localProfile = ProfileBuilder.newBuilder()
                                             .name("Local server log" + System.currentTimeMillis())
                                             .filePath(getPathToLogFile())
                                             .onLocalhost()
                                             .build();
        final Iterator<GrepResult> iterator = grep(constantExpression(matcher), on(localProfile)).iterator();
        if (iterator.hasNext()) {
            return iterator.next().getText();
        } else {
            throw new RuntimeException("There are no lines in " + getPathToLogFile() + " which matches " + matcher);
        }
    }

    public static Date getDateOfLine(String line) {
        String dateString = line.substring(1, LINE_SAMPLE.length());
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        try {
            return dateFormat.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException("Cannot parse \"" + dateString + "\" to date.", e);
        }
    }

    public static List<Integer> getNumbersFromLine(String line) {
        String content = line.substring(LINE_SAMPLE.length());
        Pattern p = Pattern.compile("[0-9]+");
        final Matcher matcher = p.matcher(content);
        List<Integer> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Integer.valueOf(matcher.group()));
        }
        return numbers;
    }

    public static long millisecondsFromTo(String from, String to) {
        long startedMilliseconds = LogProcessor.getDateOfLine(LogProcessor.getFirstLineThatMatch(from)).getTime();
        long finishedMilliseconds = LogProcessor.getDateOfLine(LogProcessor.getFirstLineThatMatch(to)).getTime();
        return finishedMilliseconds - startedMilliseconds;
    }
}
