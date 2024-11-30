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

import io.github.pixee.security.BoundedLineReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LogProcessor {

    static final String LINE_SAMPLE = "[2017-11-11 11:11:11,111 ";

    static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss,SSS";

    static private String pathToLogFile = System.getProperty("pa.rm.home") + File.separatorChar + "logs" +
                                          File.separatorChar + "Scheduler-tests-last.log";

    public static String getPathToLogFile() {
        return pathToLogFile;
    }

    public static void setPathToLogFile(String pathToLogFile) {
        LogProcessor.pathToLogFile = pathToLogFile;
    }

    public static List<String> linesThatMatch(String matcher) {
        try (BufferedReader br = new BufferedReader(new FileReader(getPathToLogFile()))) {
            List<String> result = new ArrayList<>();
            String line;
            while ((line = BoundedLineReader.readLine(br, 5_000_000)) != null) {
                if (line.contains(matcher)) {
                    result.add(line);
                }
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static int numberOfLinesWhichMatch(String matcher) {
        return linesThatMatch(matcher).size();
    }

    public static String getLastLineThatMatch(String matcher) {
        List<String> lines = linesThatMatch(matcher);
        if (!lines.isEmpty()) {
            return lines.get(lines.size() - 1);
        } else {
            throw new RuntimeException("There are no lines in " + getPathToLogFile() + " which matches " + matcher);
        }
    }

    public static String getFirstLineThatMatch(String matcher) {
        List<String> lines = linesThatMatch(matcher);
        if (!lines.isEmpty()) {
            return lines.get(0);
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
