package org.objectweb.proactive.examples.fastdeployment;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.objectweb.proactive.ProActive;
import org.objectweb.proactive.api.ProActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.util.log.Loggers;
import org.objectweb.proactive.core.util.log.ProActiveLogger;


/**
 * An example of fast ProActive Deployment.
 *
 * This example, try to create and use active objects as soon as possible.
 *
 * The VNActivator activates ProActive Descriptor/Virtual Node and creates active
 * objects. Operations are performed in parallel and parameters can be tuned to optimize
 * the deployment.
 *
 * The manager is in charge of Active Object Management. When an active object has been created,
 * it is send to the manager.
 *
 * Benchmarks showed that you can expect ~8 AO creations per second.
 *
 * Usage:
 *         --descriptor desc1.xml desc2.xml desc3.xml --virtualNode vn --pause 8000 --concurrency 4
 *
 */
public class Main {
    final static Logger logger = ProActiveLogger.getLogger(Loggers.EXAMPLES);
    final static int DEFAULT_PAUSE = 60000;
    final static int DEFAULT_CONCURRENCY = 1;
    private Set<String> virtualNodes;
    private Set<String> descriptors;
    private int concurrency;
    private int pause;

    public static void main(String[] args) throws Exception {
        new Main(args);
    }

    public Main(String[] args) {
        virtualNodes = new HashSet<String>();
        descriptors = new HashSet<String>();
        concurrency = DEFAULT_CONCURRENCY;
        pause = DEFAULT_PAUSE;

        // Parse command line option by using Jakarta common CLI
        parseArguments(args);

        if (logger.isDebugEnabled()) {
            logger.debug("Params: concurency=" + concurrency);
            logger.debug("Params: pause=" + pause);

            String vns = new String();
            for (String vn : virtualNodes) {
                vns += (" " + vn);
            }
            logger.debug("Params: virtual nodes=" + vns);

            String descs = new String();
            for (String desc : descriptors) {
                descs += (" " + desc);
            }
            logger.debug("Params: descriptors=" + descs);
        }

        if (descriptors.size() == 0) {
            logger.error("At least one descriptor is required");
            System.exit(1);
        }

        try {
            // Create the manager on this ProActive Runtime
            Manager manager = (Manager) ProActiveObject.newActive(Manager.class.getName(),
                    new Object[] {  });

            // Create the Virtual Node Activator on this ProActive Runtime
            // For optimal performances, the manager & VNActivator should be in 
            // the same ProActive Runtime.
            ProActiveObject.newActive(VNActivator.class.getName(),
                new Object[] {
                    manager, descriptors, virtualNodes, concurrency, pause
                });
        } catch (ProActiveException e) {
            logger.error("Manager or VNActivator cannot be created", e);
            ProActive.exitFailure();
        }
    }

    public void parseArguments(String[] args) {
        CommandLineParser parser = new PosixParser();

        Options options = new Options();
        options.addOption(Params.concurrency.sOpt,
            Params.concurrency.toString(), true, Params.concurrency.desc);
        options.addOption(Params.pause.sOpt, Params.pause.toString(), true,
            Params.pause.desc);

        Option descOption;
        descOption = new Option(Params.descriptor.sOpt,
                Params.descriptor.toString(), true, Params.descriptor.desc);
        descOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(descOption);

        Option vnOption;
        vnOption = new Option(Params.virtualNode.sOpt,
                Params.virtualNode.toString(), true, Params.virtualNode.desc);
        vnOption.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(vnOption);

        CommandLine line = null;

        String arg;

        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("fast Deployment", options);
            System.exit(1);
        }

        Option[] pOptions = line.getOptions();
        for (Option pOption : pOptions) {
            if (pOption.getOpt().equals(Params.virtualNode.sOpt)) {
                for (String s : pOption.getValues()) {
                    virtualNodes.add(s);
                }
            }

            if (pOption.getOpt().equals(Params.descriptor.sOpt)) {
                for (String s : pOption.getValues()) {
                    descriptors.add(s);
                }
            }
        }

        arg = line.getOptionValue(Params.concurrency.sOpt);
        if (arg != null) {
            try {
                concurrency = new Integer(arg);
            } catch (NumberFormatException e) {
                logger.warn("Invalid option value " + arg);
            }
        }

        arg = line.getOptionValue(Params.pause.sOpt);
        if (arg != null) {
            try {
                pause = new Integer(arg);
            } catch (NumberFormatException e) {
                logger.warn("Invalid option value " + arg);
            }
        }
    }
    public enum Params {virtualNode("v", "Virtual Node name to be activated"),
        descriptor("d", "Descritpor to be activated"),
        concurrency("c", "Number of VNActivator threads"),
        pause("p", "pause time between VN activation");
        protected String sOpt;
        protected String desc;

        private Params(String sOpt, String desc) {
            this.sOpt = sOpt;
            this.desc = desc;
        }

        public String shortOpt() {
            return sOpt;
        }

        public String longOpt() {
            return toString();
        }
    }
}
