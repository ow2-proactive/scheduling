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
package org.objectweb.proactive.p2p.daemon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.nt.NTEventLogAppender;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.core.xml.handler.BasicUnmarshaller;
import org.objectweb.proactive.core.xml.io.Attributes;
import org.objectweb.proactive.core.xml.io.StreamReader;
import org.objectweb.proactive.p2p.service.StartP2PService;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/*
 * Exit codes : 0 => OK, restart the daemon
 *              1 => Error, don't restart
 *              2 => Restart but wait for the next period
 *          other => Error, restart
 *
 * These exit codes must be synchronized with the C code
 */
class Moment implements Comparable {
    private static final int[][] CONV_INT = {
            { Calendar.MONDAY, 0 },
            { Calendar.TUESDAY, 1 },
            { Calendar.WEDNESDAY, 2 },
            { Calendar.THURSDAY, 3 },
            { Calendar.FRIDAY, 4 },
            { Calendar.SATURDAY, 5 },
            { Calendar.SUNDAY, 6 }
        };
    private static final Object[][] CONV_STR = {
            { "monday", new Integer(0) },
            { "tuesday", new Integer(1) },
            { "wednesday", new Integer(2) },
            { "thursday", new Integer(3) },
            { "friday", new Integer(4) },
            { "saturday", new Integer(5) },
            { "sunday", new Integer(6) }
        };
    public static final Moment END_OF_WEEK = new Moment(6, 23, 59);
    public static final Moment START_OF_WEEK = new Moment(0, 0, 0);
    public static final int MINUTES_IN_WEEK = 7 * 24 * 60;
    private int day;
    private int hour;
    private int minute;
    private int absoluteMinutes;
    private boolean valid;

    public Moment() {
        GregorianCalendar now = new GregorianCalendar();
        day = convertDay(now.get(Calendar.DAY_OF_WEEK));
        hour = now.get(Calendar.HOUR_OF_DAY);
        minute = now.get(Calendar.MINUTE);
        init();
    }

    public Moment(int day, int hour, int minute) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        init();
    }

    public Moment(Attributes a) {
        this.day = convertDay(a.getValue(0));
        this.hour = Integer.parseInt(a.getValue(1));
        this.minute = Integer.parseInt(a.getValue(2));
        init();
    }

    private static int convertDay(int day) {
        for (int i = 0; i < CONV_INT.length; i++)
            if (CONV_INT[i][0] == day) {
                return CONV_INT[i][1];
            }

        return -1;
    }

    private static int convertDay(String day) {
        for (int i = 0; i < CONV_STR.length; i++)
            if (CONV_STR[i][0].equals(day)) {
                return ((Integer) CONV_STR[i][1]).intValue();
            }

        return -1;
    }

    private void init() {
        validate();
        absoluteMinutes = (((day * 24) + hour) * 60) + minute;
    }

    private void validate() {
        valid = true;
        valid = valid && (day >= 0);
        valid = valid && (day < 7);
        valid = valid && (hour >= 0);
        valid = valid && (hour < 24);
        valid = valid && (minute >= 0);
        valid = valid && (minute < 60);
    }

    public boolean isValid() {
        return valid;
    }

    public int compareTo(Object o) {
        Moment m = (Moment) o;

        return absoluteMinutes - m.absoluteMinutes;
    }

    @Override
    public boolean equals(Object o) {
        return compareTo(o) == 0;
    }

    public int minutesFromNow() {
        Moment now = new Moment();

        return absoluteMinutes - now.absoluteMinutes;
    }

    /* <Debug> */
    @Override
    public String toString() {
        return "<" + day + ", " + hour + ", " + minute + ">";
    }

    /* </Debug> */
}


class IntervalTime implements Comparable {

    /* <Debug> */
    private static Random random = new Random();
    private Moment start;
    private Moment end;
    private boolean valid;

    public IntervalTime(Moment start, Moment end) {
        this.start = start;
        this.end = end;
        validate();
    }

    private void validate() {
        valid = true;
        valid = valid && start.isValid();
        valid = valid && end.isValid();
        valid = valid && (start.compareTo(end) < 0);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean contains(Moment moment) {
        return (start.compareTo(moment) <= 0) && (end.compareTo(moment) >= 0);
    }

    public boolean contains(IntervalTime interval) {
        return (start.compareTo(interval.start) <= 0) &&
        (end.compareTo(interval.getEnd()) >= 0);
    }

    public boolean intersect(IntervalTime interval) {
        return contains(interval.start) || contains(interval.end) ||
        interval.contains(start) || interval.contains(end);
    }

    public int compareTo(Object o) {
        IntervalTime it = (IntervalTime) o;

        return start.compareTo(it.start);
    }

    @Override
    public boolean equals(Object o) {
        IntervalTime interval = (IntervalTime) o;

        return start.equals(interval.start) && end.equals(interval.end);
    }

    public Moment getEnd() {
        return end;
    }

    public void setEnd(Moment end) {
        this.end = end;
    }

    public Moment getStart() {
        return start;
    }

    public void setStart(Moment start) {
        this.start = start;
    }

    @Override
    public String toString() {
        return "[" + start + " - " + end + "]";
    }

    public static IntervalTime randomIntervalTime() {
        return new IntervalTime(new Moment(random.nextInt(7),
                random.nextInt(24), random.nextInt(60)),
            new Moment(random.nextInt(8), random.nextInt(25), random.nextInt(61)));
    }

    /* </Debug> */
}


class WorkTime {
    private List<IntervalTime> intervals;

    public WorkTime() {
        intervals = new ArrayList<IntervalTime>();
    }

    private boolean handleIntersection(ListIterator<IntervalTime> iter,
        IntervalTime intersected, IntervalTime interval) {
        if (interval.compareTo(intersected) < 0) {
            intersected.setStart(interval.getStart());
        }

        if (interval.getEnd().compareTo(intersected.getEnd()) > 0) {
            intersected.setEnd(interval.getEnd());

            List<IntervalTime> toDelete = new ArrayList<IntervalTime>();

            while (iter.hasNext()) {
                IntervalTime next = iter.next();

                if (intersected.contains(next)) {
                    toDelete.add(next);
                } else if (intersected.intersect(next)) {
                    intersected.setEnd(next.getEnd());
                    toDelete.add(next);
                } else {
                    break;
                }
            }

            Iterator<IntervalTime> toDeleteIter = toDelete.iterator();

            while (toDeleteIter.hasNext()) {
                IntervalTime toDeleteItem = toDeleteIter.next();
                intervals.remove(toDeleteItem);
            }
        }

        return true;
    }

    private boolean addInterval(IntervalTime interval) {
        if (!interval.isValid()) {
            return false;
        }

        ListIterator<IntervalTime> iter = intervals.listIterator();

        while (iter.hasNext()) {
            IntervalTime inList = iter.next();

            if (inList.intersect(interval)) {
                if (inList.contains(interval)) {
                    return true;
                }

                return handleIntersection(iter, inList, interval);
            }

            if (interval.compareTo(inList) < 0) {
                iter.previous();

                break;
            }
        }

        iter.add(interval);

        return true;
    }

    public boolean addInterval(Moment start, Moment end) {
        if (!start.isValid() || !end.isValid()) {
            return false;
        }

        if (start.compareTo(end) <= 0) {
            return addInterval(new IntervalTime(start, end));
        }

        addInterval(new IntervalTime(start, Moment.END_OF_WEEK));
        addInterval(new IntervalTime(Moment.START_OF_WEEK, end));

        return true;
    }

    public IntervalTime getCurrentWorkInterval() {
        Moment now = new Moment();
        Iterator<IntervalTime> iter = intervals.iterator();

        while (iter.hasNext()) {
            IntervalTime interval = iter.next();

            if (interval.getEnd().equals(now)) {
                continue;
            }

            if (interval.contains(now)) {
                return interval;
            }

            if (interval.getStart().compareTo(now) > 0) {
                break;
            }
        }

        return null;
    }

    public IntervalTime getNextWorkInterval() {
        Moment now = new Moment();
        Iterator<IntervalTime> iter = intervals.iterator();

        while (iter.hasNext()) {
            IntervalTime interval = iter.next();

            if (interval.getStart().compareTo(now) >= 0) {
                return interval;
            }
        }

        return intervals.get(0);
    }

    public int computeDuration(IntervalTime work) {
        Moment end = work.getEnd();

        if (!end.equals(Moment.END_OF_WEEK)) {
            return end.minutesFromNow();
        }

        if (work.getStart().equals(Moment.START_OF_WEEK)) {

            /* Infinite */
            return -1;
        }

        IntervalTime first = intervals.get(0);

        if (!first.getStart().equals(Moment.START_OF_WEEK)) {
            return end.minutesFromNow();
        }

        return first.getEnd().minutesFromNow() + Moment.MINUTES_IN_WEEK;
    }

    public boolean isEmpty() {
        return intervals.isEmpty();
    }

    /* <Debug> */
    public boolean isValid() {
        Iterator<IntervalTime> iter = intervals.iterator();
        IntervalTime previous = null;
        IntervalTime current = null;

        while (iter.hasNext()) {
            previous = current;
            current = iter.next();

            if (!current.isValid()) {
                return false;
            }

            if (previous != null) {
                if (previous.getEnd().compareTo(current.getStart()) >= 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        if (intervals.isEmpty()) {
            return "()";
        }

        String res = "(" + intervals.get(0);

        Iterator<IntervalTime> iter = intervals.iterator();
        iter.next();

        while (iter.hasNext()) {
            IntervalTime current = iter.next();
            res += (", " + current);
        }

        return res + ")";
    }

    public void test() {
        IntervalTime interval = IntervalTime.randomIntervalTime();
        System.out.println("\nAjout de : " + interval);
        System.out.println("Reussi   : " + addInterval(interval));
        System.out.println("Resultat : " + this);
        System.out.println("isValid  : " + isValid());
    }

    /* </Debug> */
}


class XMLConfig extends BasicUnmarshaller implements ErrorHandler {
    private static final String CONFIG_PREFIX = ".." + File.separator +
        "config" + File.separator;
    private static String hostname = null;
    private WorkTime work;
    private Vector<String> url;

    /* Used when parsing */
    private boolean rightHost;
    private boolean rightConfig;
    private Stack<File> currentConfigFile;
    private Moment start;
    private Moment end;
    private boolean parsed;
    private Collection<String> parsedXML;

    public XMLConfig(WorkTime work, Vector<String> url) {
        this.work = work;
        this.url = url;
    }

    public static String getLocalHostName() {
        if (hostname == null) {
            try {
                hostname = InetAddress.getLocalHost().getCanonicalHostName()
                                      .toLowerCase();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                hostname = "localhost";
            }
        }

        return hostname;
    }

    public void error(SAXParseException e) throws SAXException {
        throw new SAXException(e);
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw new SAXException(e);
    }

    public void warning(SAXParseException e) throws SAXException {

        /* Ignore the warning */
    }

    @Override
    public void startElement(String name, Attributes attributes)
        throws SAXException {
        if (parsed) {
            return;
        }

        if (rightConfig) {
            if (name.equals("start")) {
                start = new Moment(attributes);
            } else if (name.equals("end")) {
                end = new Moment(attributes);
                work.addInterval(start, end);
            } else if (name.equals("registry")) {
                url.add(attributes.getValue("url"));
            }
        }

        if (name.equals("loadconfig")) {
            String path = attributes.getValue("path")
                                    .replace('/', File.separatorChar);

            if (!new File(path).isAbsolute()) {
                File current = currentConfigFile.peek();
                path = new File(current.getParentFile(), path).getAbsolutePath();
            }

            parseConfigFile(path);
        } else if (name.equals("host")) {
            if (attributes.getValue("name").equalsIgnoreCase(getLocalHostName())) {
                rightHost = true;
            }
        } else if (name.equals("default")) {
            rightConfig = true;
        } else if (name.equals("configForHost")) {
            if (rightHost) {
                rightConfig = true;
            }
        }
    }

    @Override
    public void endElement(String name) throws SAXException {
        if (parsed) {
            return;
        }

        if (name.equals("configForHost") || name.equals("default") ||
                name.equals("p2pconfig")) {
            if (rightConfig) {
                parsed = true;
                rightConfig = false;
            }
        }
    }

    private static java.net.URL getXMLSchema() {
        java.net.URL url = null;

        try {
            url = (new java.io.File(CONFIG_PREFIX + "proactivep2p.xsd")).toURI()
                   .toURL();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return url;
    }

    private static String getDefaultConfig(boolean hostDependant) {
        String prefix = CONFIG_PREFIX + "proactivep2p.";

        if (hostDependant) {
            prefix += (getLocalHostName() + ".");
        }

        return prefix + "xml";
    }

    /*
     * true --> config file read
     * false --> file not found or something like that --> config not loaded
     * SAXException --> Schema not validated
     */
    private boolean parseConfigFile(String configFile)
        throws SAXException {
        if ((configFile == null) || parsedXML.contains(configFile)) {
            return false;
        }

        StreamReader stream;

        try {
            File file = new File(configFile);
            Reader reader = new FileReader(file);
            stream = new StreamReader(new InputSource(reader), this,
                    new String[] { getXMLSchema().toString() }, this);
        } catch (IOException ioe) {
            return false;
        }

        currentConfigFile.push(new File(configFile));
        parsedXML.add(configFile);

        try {
            stream.read();
        } catch (IOException e) {
            throw new SAXException(e);
        } finally {
            currentConfigFile.pop();
        }

        return true;
    }

    public void parse() throws SAXException {
        currentConfigFile = new Stack<File>();
        rightHost = false;
        rightConfig = false;
        parsed = false;
        parsedXML = new HashSet<String>();

        if (parseConfigFile(getDefaultConfig(true)) ||
                parseConfigFile(getDefaultConfig(false))) {

            /* Nothing */
        }

        currentConfigFile = null;
        parsedXML = null;
    }
}


public class Daemon {
    private static Logger logger = ProActiveLogger.getLogger(Loggers.P2P_DAEMON);

    /* Logging */
    private static final String LOG_DIR = ".." + File.separator + "logs" +
        File.separator;
    private static final String MAX_SIZE = "100KB";
    private static final String LOG_PATTERN = "%d %c %x %m\n\n";
    private static WriterAppender writerAppender;

    static {
        configureLogging();
    }

    /* Shared with the native daemon */
    private static final int DAEMON_PORT = 9015;
    private static final String LOG_HEADER = "[P2P] ";
    private static final String RESTART_CMD = "restart";
    private static final String STOP_CMD = "stop";
    private static final String KILL_CMD = "killdaemon";
    private static final String ALIVE_CMD = "alive";
    private static final String FLUSH_CMD = "flush";
    private WorkTime work;
    private Vector<String> url;

    private Daemon(boolean nextRun) {
        work = new WorkTime();
        url = new Vector<String>();

        try {
            new XMLConfig(work, url).parse();
        } catch (SAXException e) {
            log(e.getMessage(), true);
            System.exit(1);
        }

        try {
            startCommandListener();
        } catch (Exception e) {
            flush("Cannot create command listener");
            System.exit(1);
        }

        if (work.isEmpty()) {
            log("No work time scheduled", false);

            /* Wait for a command from p2pctl */
            try {
                Thread.currentThread().join();
            } catch (InterruptedException ie) {
                System.exit(3);
            }
        }

        IntervalTime current = work.getCurrentWorkInterval();

        if ((current == null) || nextRun) {
            IntervalTime next = work.getNextWorkInterval();
            int minutes;

            if (nextRun && (current == next)) {
                log("No sleep moment to use", false);
                minutes = 24 * 60; /* 2424 => sleep one day */
            } else {
                minutes = next.getStart().minutesFromNow();

                if (minutes < 0) {
                    minutes += Moment.MINUTES_IN_WEEK; /* Next week */
                }
            }

            log("Waiting for " + minutes + " minutes before working", false);
            sleep(minutes);
            current = next;
        }

        int minutes = work.computeDuration(current);

        try {
            setDestructionTimeout(minutes);
        } catch (Exception e) {
            flush("Cannot set the destruction timeout");
            System.exit(1);
        }

        startWorking();
    }

    static void log(String msg, boolean isError) {
        msg = LOG_HEADER + msg;

        try {
            if (isError) {
                logger.error(msg);
            } else {
                logger.info(msg);
            }
        } catch (Exception e) {
            /* Log the logging exception ;-) */
            System.out.println(e.getMessage() + " when logging : " + msg);
        }
    }

    private static void configureLogging() {
        Appender appender;

        try {
            appender = new NTEventLogAppender("ProActiveP2P");
        } catch (java.lang.UnsatisfiedLinkError e) {
            String hostname = XMLConfig.getLocalHostName();

            Layout layout = new PatternLayout(LOG_PATTERN);
            String filename = LOG_DIR + hostname;
            RollingFileAppender rfa;

            try {
                new File(LOG_DIR).mkdir();
                rfa = new RollingFileAppender(layout, filename, true);
            } catch (IOException ioe) {
                ioe.printStackTrace();

                return;
            }

            rfa.setMaxBackupIndex(0);
            rfa.setMaxFileSize(MAX_SIZE);
            rfa.setImmediateFlush(false);
            writerAppender = rfa;
            appender = rfa;
        }

        Logger root = Logger.getRootLogger();
        root.addAppender(appender);

        /* First message :) */
        log("Starting P2P Daemon", false);
    }

    static void sleep(long minutes) {
        try {
            Thread.sleep(minutes * 60 * 1000);
        } catch (InterruptedException ie) {
            System.exit(3);
        }
    }

    private static void flush(String message) {
        if (writerAppender != null) {
            writerAppender.setImmediateFlush(true);
        }

        log(message, false);

        if (writerAppender != null) {
            writerAppender.setImmediateFlush(false);
        }
    }

    static String readCommand(BufferedReader reader) {
        char[] tab = new char[256];
        int offset = 0;

        do {
            try {
                int nbRead = reader.read(tab, offset, tab.length - offset);

                if (nbRead <= 0) {
                    break;
                }

                offset += nbRead;
            } catch (IOException ioe) {
                break;
            }

            String line = new String(tab);
            String trimmed = line.trim();

            if (!trimmed.equals(line)) {
                return trimmed;
            }
        } while (offset < 256);

        return null;
    }

    void handleCommand(Socket client, String command) {
        if (!KILL_CMD.equals(command) && !STOP_CMD.equals(command) &&
                !RESTART_CMD.equals(command) && !ALIVE_CMD.equals(command) &&
                !FLUSH_CMD.equals(command)) {
            return;
        }

        try {
            OutputStream out = client.getOutputStream();
            out.write("OK".getBytes("US-ASCII"));
            out.close();
        } catch (IOException e) {

            /* The client left, we won't process its request */
            return;
        }

        if (KILL_CMD.equals(command)) {
            flush(command);
            System.exit(220);
        }

        if (STOP_CMD.equals(command)) {
            flush(command);
            System.exit(2);
        }

        if (RESTART_CMD.equals(command)) {
            flush(command);
            System.exit(0);
        }

        if (FLUSH_CMD.equals(command)) {
            flush(command);
        }
    }

    private void startCommandListener() {
        new Thread(new Runnable() {
                public void run() {
                    ServerSocket server;

                    try {
                        server = new ServerSocket(DAEMON_PORT);
                    } catch (IOException ioe) {
                        log(ioe.getMessage(), true);
                        System.exit(1);

                        return; /* To avoid a javac error */
                    }

                    for (;;) {
                        try {
                            Socket client = server.accept();

                            /*
                             * TODO: When the experimenting phase is done we should allow connections only from localhost
                             */
                            InputStream stream = client.getInputStream();
                            InputStreamReader ireader = new InputStreamReader(stream);
                            BufferedReader reader = new BufferedReader(ireader);
                            String line = readCommand(reader);
                            handleCommand(client, line);

                            stream.close();
                            client.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                }
            }).start();
    }

    private static void setDestructionTimeout(final int minutes) {
        if (minutes >= 0) {
            log("Killing the JVM in " + minutes + " minutes", false);
            new Thread(new Runnable() {
                    public void run() {
                        sleep(minutes);
                        System.exit(0);
                    }
                }).start();
        } else {
            log("Never killing the JVM", false);
        }
    }

    private void startWorking() {
        log("Start to work", false);

        StartP2PService service = new StartP2PService(url);

        try {
            service.start();
        } catch (ProActiveException e) {
            logger.warn("Couldn't start the P2P Service", e);
        }
    }

    public static void main(String[] args) {
        try {
            boolean nextRun = false;

            for (int i = 0; i < args.length; i++)
                if ("-n".equals(args[i])) {
                    nextRun = true;
                }

            new Daemon(nextRun);
        } catch (Throwable t) {
            logger.error("Throwable:", t);
            System.exit(3);
        }
    }

    /* <Debug> */
    public void test() {
        for (int i = 0; i < 100; i++)
            work.test();
    }

    /* </Debug> */
}
