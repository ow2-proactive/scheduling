/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2009 INRIA/University of Nice-Sophia Antipolis
 * Contact: proactive@ow2.org
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
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.util.adminconsole;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXProviderException;
import javax.security.auth.login.LoginException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.ow2.proactive.jmx.connector.PAAuthenticationConnectorClient;
import org.ow2.proactive.scheduler.common.UserSchedulerInterface;
import org.ow2.proactive.scheduler.common.util.userconsole.UserController;
import org.ow2.proactive.utils.console.MBeanInfoViewer;


/**
 * AdminController will help you to manage the scheduler.
 *
 * @author The ProActive Team
 * @since ProActive Scheduling 1.0
 */
public class AdminController extends UserController {

    /**
     * Start the Scheduler controller
     *
     * @param args the arguments to be passed
     */
    public static void main(String[] args) {
        shell = new AdminController(null);
        shell.load(args);
    }

    /**
     * Create a new instance of AdminController
     */
    protected AdminController() {
    }

    /**
     * Create a new instance of AdminController
     *
     * Convenience constructor to let the default one do nothing
     */
    protected AdminController(Object o) {
        commandName = "adminScheduler";
        model = AdminSchedulerModel.getModel(true);
    }

    @Override
    protected void connect() throws LoginException {
        UserSchedulerInterface scheduler = auth.logAsAdmin(credentials);
        model.connectScheduler(scheduler);
        String userStr = (user != null) ? "'" + user + "' " : "";
        logger.info("\t-> Admin " + userStr + "successfully connected" + newline);
    }

    @Override
    protected OptionGroup addCommandLineOptions(Options options) {
        super.addCommandLineOptions(options);

        OptionGroup group = super.addCommandLineOptions(options);

        Option opt = new Option("start", false, control + "Start the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("stop", false, control + "Stop the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("pause", false, control +
            "Pause the Scheduler (cause all non-running jobs to be paused)");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("freeze", false, control +
            "Freeze the Scheduler (cause all non-running tasks to be paused)");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("resume", false, control + "Resume the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("shutdown", false, control + "Shutdown the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("kill", false, control + "Kill the Scheduler");
        opt.setRequired(false);
        group.addOption(opt);

        opt = new Option("linkrm", true, control + "Reconnect a RM to the scheduler");
        opt.setArgName("rmURL");
        opt.setRequired(false);
        opt.setArgs(1);
        group.addOption(opt);

        opt = new Option("policy", true, control + "Change the current scheduling policy");
        opt.setArgName("fullName");
        opt.setRequired(false);
        opt.setArgs(1);
        group.addOption(opt);

        options.addOptionGroup(group);

        return group;
    }

    @Override
    protected boolean startCommandLine(CommandLine cmd) {
        if (super.startCommandLine(cmd)) {
            model.setDisplayOnStdStream(true);
            if (cmd.hasOption("start")) {
                AdminSchedulerModel.start();
            } else if (cmd.hasOption("stop")) {
                AdminSchedulerModel.stop();
            } else if (cmd.hasOption("pause")) {
                AdminSchedulerModel.pause();
            } else if (cmd.hasOption("freeze")) {
                AdminSchedulerModel.freeze();
            } else if (cmd.hasOption("resume")) {
                AdminSchedulerModel.resume();
            } else if (cmd.hasOption("shutdown")) {
                AdminSchedulerModel.shutdown();
            } else if (cmd.hasOption("kill")) {
                AdminSchedulerModel.kill();
            } else if (cmd.hasOption("linkrm")) {
                AdminSchedulerModel.linkRM(cmd.getOptionValue("linkrm"));
            } else if (cmd.hasOption("policy")) {
                AdminSchedulerModel.changePolicy(cmd.getOptionValue("policy"));
            } else {
                model.setDisplayOnStdStream(false);
                return true;
            }
        }
        return false;
    }

    protected void connectJMXClient() throws JMXProviderException {
        try {
            PAAuthenticationConnectorClient cli = new PAAuthenticationConnectorClient(auth
                    .getJmxMonitoringUrl() +
                "_admin");
            cli.connect(credentials, user);
            MBeanServerConnection conn = cli.getConnection();
            ObjectName on = new ObjectName("SchedulerFrontend:name=SchedulerWrapperMBean_admin");
            model.setJMXInfo(new MBeanInfoViewer(conn, on));
        } catch (Exception e) {
            logger.error("Error while connecting JMX : ", e);
        }
    }

}
