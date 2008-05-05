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
package org.objectweb.proactive.benchmarks.timit;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jdom.Element;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.benchmarks.timit.config.Benchmark;
import org.objectweb.proactive.benchmarks.timit.config.ConfigChart;
import org.objectweb.proactive.benchmarks.timit.config.ConfigReader;
import org.objectweb.proactive.benchmarks.timit.config.Series;
import org.objectweb.proactive.benchmarks.timit.result.BenchmarkResultWriter;
import org.objectweb.proactive.benchmarks.timit.result.SerieResultWriter;
import org.objectweb.proactive.benchmarks.timit.util.BenchmarkStatistics;
import org.objectweb.proactive.benchmarks.timit.util.Startable;
import org.objectweb.proactive.benchmarks.timit.util.TimItManager;
import org.objectweb.proactive.benchmarks.timit.util.TimItReductor;
import org.objectweb.proactive.benchmarks.timit.util.XMLHelper;
import org.objectweb.proactive.benchmarks.timit.util.charts.Utilities;
import org.objectweb.proactive.core.node.NodeException;


/**
 * TimIt offer a complete solution to benchmark an application. It is an API which provide some
 * advanced timing and event observing services. Benchmarking your ProActive application will permit
 * you to enhance performance of it. Thanks to <emphasis>generated statistics charts</emphasis>,
 * you will be able to determine critical points of your application. <br>
 * <br>
 * Different kind of statistics can be done. You can setup different timers with
 * <emphasis>hierarchical capabilities</emphasis> and see them in charts. Event observers can be
 * placed to study, for example, communication pattern between your application's workers. <br>
 * <br>
 * TimIt generate charts and results XML file, with exact timing and event observers values.
 * 
 * @author The ProActive Team
 */
public class TimIt {
    public static final DecimalFormat df = new DecimalFormat("##0.000", new DecimalFormatSymbols(
        java.util.Locale.US));
    private static final String VERSION = "1.0";
    private static final int MAX_TIMEOUT_ERRORS = 3;
    private static final int WAIT_AFTER_ERROR = 5000;
    private static TimItReductor timitReductor;
    private static Timeout timeoutThread;
    private static boolean timeoutError;
    private static int totalTimeoutErrors;

    /**
     * Main method of Timit. Use -h argument for help
     * 
     * @param args
     *            arguments to run TimIt
     */
    public static void main(String[] args) {
        try {
            Options opt = new Options();
            opt.addOption("c", "config", true, "Specify the configuration file to use");
            opt.addOption("h", "help", false, "Print help for this application");
            opt.addOption("g", "generate-charts", true,
                    "Generate charts from merged result file without running benchmarks");
            opt.addOption("m", "merge", true, "Produce a merged result file from benchmarks results files");
            opt.addOption("v", "version", false, "Returns the TimIt version");

            BasicParser parser = new BasicParser();
            CommandLine cl = parser.parse(opt, args);

            if (cl.hasOption('c')) {
                if (cl.hasOption('g')) {
                    generateCharts(cl.getOptionValue('c'), cl.getOptionValue('g'));
                } else if (cl.hasOption('m')) {
                    mergeResults(cl.getOptionValue('c'), cl.getOptionValues('g'));
                } else {
                    TimIt.timeoutThread = new Timeout();
                    TimIt.timeoutThread.start();
                    createTimItReductor();
                    runBenchmarkSuite(cl.getOptionValue('c'));
                    TimIt.timeoutThread.terminate();
                }
            } else if (cl.hasOption('v')) {
                System.out.println(getVersion());
            } else {
                HelpFormatter help = new HelpFormatter();
                help.printHelp("TimIt options", opt);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    /**
     * Invoke this method from the main of the Startable Object if you don't want to use the
     * TimIt launcher (with the TimIt configuration file etc...).
     * See an example of usage in {@link Benchmark} main method.
     */
    public static void standaloneMode() {
        TimIt.createTimItReductor();
        TimIt.timitReductor.getStatistics();
    }

    /**
     * Used to generate charts from config a finalized file, without running benchmarks
     * 
     * @param configFile
     *            the configuration file
     * @param finalFile
     *            the finalized file
     */
    private static void generateCharts(String configFile, String finalFile) {
        ConfigReader config = new ConfigReader(configFile);
        Series[] serie = config.getSeries();

        for (Series element : serie) {
            ConfigChart[] chart = element.getCharts();
            Utilities.generatingCharts(XMLHelper.readFile(finalFile).getRootElement(), null, chart);
        }
    }

    /**
     * Used to generate finalized file from config file, without running benchmarks
     * 
     * @param configFile
     *            the configuration file
     * @param resultFiles
     *            all result files
     */
    private static void mergeResults(String configFile, String[] resultFiles) {
        ConfigReader config = new ConfigReader(configFile);
        Series[] serie = config.getSeries();

        for (Series element : serie) {
            ConfigChart[] chart = element.getCharts();
            SerieResultWriter serieResults = new SerieResultWriter(element.get("result"));
            Benchmark[] bench = element.getBenchmarks();

            for (int j = 0; j < bench.length; j++) {
                Benchmark benchmark = bench[j];
                Element eResult = XMLHelper.readFile(resultFiles[j]).getRootElement();
                int warmup = Integer.valueOf(benchmark.get("warmup"));
                int nbRuns = Integer.valueOf(benchmark.get("run")) + warmup;
                serieResults.addResult(eResult, benchmark.get("name"), nbRuns, TimIt.totalTimeoutErrors);
            }

            Utilities.generatingCharts(serieResults.getRoot(), null, chart);
        }
    }

    /**
     * Create a reductor which will be used by all Timed objects to return their results (time qand
     * event statistics)
     */
    private static void createTimItReductor() {
        try {
            TimIt.timitReductor = (TimItReductor) PAActiveObject.newActive(TimItReductor.class.getName(),
                    new Object[] {});
            TimItManager.getInstance().setTimitReductor(TimIt.timitReductor);
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Used to run all series of benchmarks from the configuration file
     * 
     * @param configfile
     *            the configuration file
     */
    private static void runBenchmarkSuite(String configfile) {
        try {
            ConfigReader config = new ConfigReader(configfile);
            Series[] serie = config.getSeries();
            int timeoutErrorCount = TimIt.MAX_TIMEOUT_ERRORS;

            for (Series element : serie) {
                //
                // ** Series **
                //
                ConfigChart[] chart = element.getCharts();
                Benchmark[] bench = element.getBenchmarks();
                Class<?> runClass = Class.forName(element.get("class"));
                Startable startable = (Startable) runClass.newInstance();
                message(1, "RUN SERIES " + runClass.getSimpleName() + " [" + element.get("result") + "]");

                SerieResultWriter serieResults = new SerieResultWriter(element.get("result"));

                BenchmarkStatistics bstats = null;
                TimIt.totalTimeoutErrors = 0;

                for (Benchmark b : bench) {
                    //
                    // ** Benchmarks **
                    //
                    String name = b.get("name");
                    String outDesc = b.get("descriptorGenerated");
                    message(2, "RUN BENCHMARK " + name + " [" + b.get("output") + "]");

                    BenchmarkResultWriter benchResults = new BenchmarkResultWriter(b.get("output"));

                    if (outDesc.length() > 0) {
                        XMLHelper.generateDescriptor(element.get("descriptorBase"), config
                                .getGlobalVariables(), b.getVariables(), outDesc);
                    }

                    int warmup = Integer.valueOf(b.get("warmup"));
                    int nbRuns = Integer.valueOf(b.get("run")) + warmup;
                    TimIt.timeoutThread.setNewBenchmark(Long.valueOf(b.get("timeout")) * 1000, startable);
                    timeoutErrorCount = TimIt.MAX_TIMEOUT_ERRORS;

                    for (int run = 1; run <= nbRuns; run++) {
                        //
                        // ** Runs **
                        //
                        if ((nbRuns <= 50) || ((nbRuns <= 100) && ((run % 2) == 0)) ||
                            ((nbRuns > 100) && (nbRuns <= 1000) && ((run % 10) == 0)) ||
                            ((nbRuns > 1000) && ((run % ((int) (nbRuns / 0.05))) == 0))) {
                            message(3, "RUN " + run + " ON " + nbRuns + ((run <= warmup) ? " [WARMUP]" : ""));
                        }

                        TimIt.timeoutError = false;
                        TimIt.timeoutThread.newRun();

                        try {
                            startable.start(b.get("parameters").split(" "));
                        } catch (Exception e) {
                            e.printStackTrace();

                            if (--timeoutErrorCount <= 0) {
                                System.err.println("Too many exceptions for this benchmark, skip it");
                                XMLHelper.errorLog(element.get("errorfile"), "Too many exceptions (" +
                                    TimIt.MAX_TIMEOUT_ERRORS + ") for this benchmark " + " (" +
                                    b.get("name") + ") : " + e.getMessage() + "\n  Skip it.");
                                TimIt.timeoutError = true;
                                TimIt.sleep(WAIT_AFTER_ERROR);

                                break;
                            } else {
                                System.err.println("An exception occur... retrying...");
                                XMLHelper.errorLog(element.get("errorfile"), "An exception occur : " +
                                    e.getMessage() + "... retrying...");
                                TimIt.sleep(WAIT_AFTER_ERROR);
                                run--;

                                continue;
                            }
                        }

                        bstats = TimIt.timitReductor.getStatistics();
                        TimIt.timitReductor.clean();
                        TimItReductor.ready();
                        PAFuture.waitFor(bstats);

                        if (TimIt.timeoutError) {
                            TimIt.totalTimeoutErrors++;
                            XMLHelper.errorLog(element.get("errorfile"), "Timeout for benchmark '" +
                                b.get("name") + "'" + " args=[" + b.get("parameters") + "]" + " on run " +
                                run);

                            if (--timeoutErrorCount <= 0) {
                                System.err.println("Too many timeout errors for this benchmark, skip it");
                                XMLHelper.errorLog(element.get("errorfile"), "Too many timeout errors (" +
                                    TimIt.MAX_TIMEOUT_ERRORS + ") for this benchmark " + " (" +
                                    b.get("name") + "), skip it.");
                                TimIt.sleep(WAIT_AFTER_ERROR);

                                break; // then continue if timeoutError=true
                            }

                            run--;

                            continue;
                        }

                        TimIt.timeoutThread.ok();

                        if (run > warmup) {
                            benchResults.addResult(bstats, name + " [" + run + "/" + nbRuns + "]");

                            if (b.get("writeEveryRun").equalsIgnoreCase("true")) {
                                benchResults.writeResult();
                            }
                        }

                        startable.kill();
                    }

                    if (TimIt.timeoutError) {
                        continue; // from previous break
                    }

                    startable.masterKill();
                    threadsCleaning();
                    benchResults.writeResult();

                    if (b.get("removeExtremums").equalsIgnoreCase("true")) {
                        benchResults.removeExtremums();
                    }

                    serieResults.addResult(benchResults.getRoot(), b.get("name"), nbRuns,
                            TimIt.totalTimeoutErrors);
                }

                // Charts generation (from serie's file)
                // last bstats is used for unmergable values (like comm events)
                Utilities.generatingCharts(serieResults.getRoot(), bstats, chart);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        message(1, "Done.");
    }

    @SuppressWarnings("deprecation")
    public static void threadsCleaning() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup().getParent();
        Thread[] threads = new Thread[200];
        int len = tg.enumerate(threads, true);
        int nbKilled = 0;

        for (int i = 0; i < len; i++) {
            Thread ct = threads[i];

            if ((ct.getName().indexOf("RMI RenewClean") >= 0) ||
                (ct.getName().indexOf("ThreadInThePool") >= 0)) {
                nbKilled++;
                ct.stop();
            }
        }

        System.err.println(nbKilled + " thread(s) stopped on " + len);
    }

    /**
     * Used to show logging information on stdout while running q benchmark suite
     * 
     * @param level
     *            the importance of the message (0=high)
     * @param msg
     *            the message
     */
    public static void message(int level, String msg) {
        switch (level) {
            case 1:
                System.out.println("\n\n**** " + msg + " ****\n");
                break;
            case 2:
                System.out.println("\n**** " + msg + " ****\n");
                break;
            case 3:
                System.out.println("\n**** " + msg + " ****");
                break;
            default:
                System.out.println("**** " + msg + " ****");
        }
    }

    /**
     * This method return the size in byte of a given object
     * 
     * @param object
     *            the object you want the size
     * @return the byte size of the given object, -1 if it can't be computed
     */
    public static int getObjectSize(Object object) {
        if (object == null) {
            return 0;
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);

            byte[] bytes = baos.toByteArray();
            oos.close();
            baos.close();

            return bytes.length;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /**
     * @return a String containing the TimIt version number (ex: "TimIt 1.0")
     */
    public static String getVersion() {
        return "TimIt " + TimIt.VERSION;
    }

    /**
     * Cause a sleep
     * 
     * @param millis
     *            the time to sleep
     */
    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Internal class for timeout handling
     */
    private static class Timeout extends Thread {
        private long timeout;
        private Startable startableObject;
        private boolean terminated;
        private boolean error;
        private boolean newRun;
        private boolean ok;

        public Timeout() {
            this.terminated = true;
            this.newRun = false;
        }

        synchronized public void setNewBenchmark(long timeout, Startable obj) {
            this.timeout = timeout;
            this.startableObject = obj;
        }

        public void newRun() {
            synchronized (this) {
                if (!this.newRun) {
                    this.newRun = true;
                    this.notifyAll();
                }

                this.error = true;
            }
        }

        public void ok() {
            synchronized (this) {
                this.newRun = false;
                this.error = false;
                this.ok = true;
                this.notifyAll();
            }
        }

        public void terminate() {
            synchronized (this) {
                this.terminated = true;
                this.error = false;
                this.notifyAll();
            }
        }

        @Override
        public void run() {
            try {
                synchronized (this) {
                    while (this.terminated) {
                        if (this.newRun) {
                            this.wait(this.timeout);
                        } else {
                            this.wait();

                            continue;
                        }

                        if (this.error && !this.ok) {
                            message(2, "TIMEOUT !!!  RESTART THE RUN");
                            TimIt.timeoutError = true;
                            this.startableObject.kill();
                            TimItReductor.stop();
                        }

                        this.ok = false;
                    }
                }
            } catch (InterruptedException e) {
            }
        }
    }
}
