/*
 * ################################################################
 *
 * ProActive: The Java(TM) library for Parallel, Distributed,
 *            Concurrent computing with Security and Mobility
 *
 * Copyright (C) 1997-2010 INRIA/University of 
 * 				Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
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
 * If needed, contact us to obtain a release under GPL Version 2 
 * or a different license than the GPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $PROACTIVE_INITIAL_DEV$
 */
package scalabilityTests.scenarios;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.SchedulerException;
import org.ow2.proactive.scheduler.common.job.JobId;

import scalabilityTests.fixtures.SchedulerFixture;
import scalabilityTests.fixtures.SchedulerFixture.CannotRegisterListenerException;
import scalabilityTests.framework.AbstractSchedulerUser;
import scalabilityTests.framework.JobSubmissionAction;
import scalabilityTests.framework.SchedulerJobSubmitter;
import scalabilityTests.framework.SchedulerUser;
import scalabilityTests.framework.listeners.ListenerInfo;
import scalabilityTests.framework.listeners.SimpleSchedulerListener;

/**
 * The scenario is the following: multiple {@link AbstractSchedulerUser}s 
 * 	connect to the scheduler and submit the same job 
 * 
 * @author fabratu
 *
 */
public class SchedulerJobSubmission {
	
	private static final int ERROR_EXIT_CODE = 42;
	private static final int DEFAULT_JOB_REPETITION = 1;
	
	private static final String COMMAND_NAME = "java " + SchedulerJobSubmission.class.getName();
	private static final String DEFAULT_SCHEDULER_LISTENER = SimpleSchedulerListener.class.getName();
	private static final Class<? extends AbstractSchedulerUser<JobId>> DEFAULT_SCHEDULER_USER_CLAZZ =
	(Class<? extends AbstractSchedulerUser<JobId>>)(new SchedulerUser<JobId>()).getClass(); // a hackish way of saying SchedulerUser<JobId>.class
	
	public static void main(String[] args) {
		Options options = new Options();
		addCommandLineOptions(options);
		Parser parser = new GnuParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
			if(cmd.hasOption("h")) {
				printHelp(options);
				System.exit(ERROR_EXIT_CODE);
			}
			int repeatsNo = cmd.hasOption("repeats") ? Integer.parseInt(cmd.getOptionValue("repeats")) : DEFAULT_JOB_REPETITION;
			Class<? extends AbstractSchedulerUser<JobId>> schedulerUserClazz = 
				cmd.hasOption("jobResult") ? SchedulerJobSubmitter.class : DEFAULT_SCHEDULER_USER_CLAZZ;
			SchedulerUsersCoordinator coordinator;
			if(cmd.hasOption("listener") || cmd.hasOption("jobResult")) {  // either of these options involve listeners
				ListenerInfo li = getListenerInfo(cmd);
				coordinator = new SchedulerUsersCoordinator(
						cmd.getOptionValue("gcmad"),
						cmd.getOptionValue("virtualNode"),
						cmd.getOptionValue("schedulerUrl"),
						schedulerUserClazz,
						cmd.getOptionValue("jobDesc"),
						li,
						repeatsNo,
						options
				);
			}
			else {
				coordinator = new SchedulerUsersCoordinator(
						cmd.getOptionValue("gcmad"),
						cmd.getOptionValue("virtualNode"),
						cmd.getOptionValue("schedulerUrl"),
						schedulerUserClazz,
						cmd.getOptionValue("jobDesc"),
						repeatsNo,
						options
				);
			}
			if(cmd.hasOption("loginFile")) {
				if(cmd.hasOption("login") || cmd.hasOption("password")) {
					System.err.println("The loginFile option excludes the usage of login/password options");
					printHelp(options);
					System.exit(ERROR_EXIT_CODE);
				}
				coordinator.setFileLogin(cmd.getOptionValue("loginFile"));
			} else {
				if(!(cmd.hasOption("login") && cmd.hasOption("password"))) {
					System.err.println("You need to specify either a loginFile or a login|password combination");
					printHelp(options);
					System.exit(ERROR_EXIT_CODE);
				}
				coordinator.setSingleLogin(
						cmd.getOptionValue("login"), 
						cmd.getOptionValue("password"));
			}
			coordinator.internalMain();
		} catch (ParseException e) {
			printHelp(options);
		} catch(NumberFormatException e) {
			System.err.println("Illegal argument for the jobRepeats option: should be an integer, instead of '" + cmd.getOptionValue("repeats") + "'");
			printHelp(options);
		}
	}
	
	private static ListenerInfo getListenerInfo(CommandLine cmd) {
		String listenerClassName = cmd.getOptionValue("listener");
		if(listenerClassName == null) {
			listenerClassName = DEFAULT_SCHEDULER_LISTENER;
			System.out.println("No listener class specified, going for the default " + DEFAULT_SCHEDULER_LISTENER);
		}
		boolean initialState = cmd.hasOption("gui");
		boolean myEventsOnly = cmd.hasOption("me");
		return new ListenerInfo(listenerClassName,initialState,myEventsOnly);
	}

	private static void addCommandLineOptions(Options options) {
		
		Option help = new Option("h", "help", false, "Display this help");
        help.setRequired(false);
        options.addOption(help);
        
        Option xmlDescriptor = new Option("ad", "gcmad", true, "path to the GCM Aplication Descriptor to be used");
		xmlDescriptor.setArgName("GCMA_xml");
		xmlDescriptor.setArgs(1);
		xmlDescriptor.setRequired(true);
		options.addOption(xmlDescriptor);
		
		Option vnode = new Option("vn", "virtualNode", true, "the name of the virtual node which identifies the nodes onto which the Active Object Actors will be deployed");
		vnode.setArgName("AO_nodes");
		vnode.setArgs(1);
		vnode.setRequired(true);
		options.addOption(vnode);
		
		Option schedulerUrl = new Option("u", "schedulerUrl", true, "the URL of the Scheduler");
		schedulerUrl.setArgName("schedulerURL");
		schedulerUrl.setArgs(1);
		schedulerUrl.setRequired(true);
		options.addOption(schedulerUrl);
		
		Option username = new Option("l", "login", true, "The username to join the Scheduler");
        username.setArgName("login");
        username.setArgs(1);
        username.setRequired(false);
        options.addOption(username);
        
        Option password = new Option("p", "password", true, "The password to join the Scheduler");
        password.setArgName("pwd");
        password.setArgs(1);
        password.setRequired(false);
        options.addOption(password);
        
        Option loginFile = new Option("lf", "loginFile", true, "The path to a file containing valid username:password combinations for the Scheduler");
        loginFile.setArgName("login_cfg");
        loginFile.setArgs(1);
        loginFile.setRequired(false);
        options.addOption(loginFile);
        
        Option jobDescriptor = new Option("j", "jobDesc", true, "The path to the XML job descriptor");
        jobDescriptor.setArgName("XML_Job_Descriptor");
        jobDescriptor.setArgs(1);
        jobDescriptor.setRequired(true);
        options.addOption(jobDescriptor);
        
        Option registerListeners = new Option("rl", "listener", false, "Register the specified Scheduler listener for each user. If no listener is specified, then a 'simple' scheduler listener will be registered");
        registerListeners.setRequired(false);
        registerListeners.setOptionalArg(true);
        registerListeners.setArgs(1); 
        registerListeners.setArgName("listenerClassName");
        options.addOption(registerListeners);
        
        Option jobRepetition = new Option("rp", "repeats", true, "The number of times the job will be submitted(optional parameter; by default it will be set to 1)");
        jobRepetition.setRequired(false);
        jobRepetition.setArgName("jobRepeats");
        jobRepetition.setArgs(1);
        options.addOption(jobRepetition);
        
        Option jobResult = new Option("jr", "jobResult", false, "Fetch the result for the submitted jobs. Optional parameter, defaults to false");
        jobResult.setRequired(false);
        options.addOption(jobResult);
        
        OptionGroup initialStateGroup = new OptionGroup();
        initialStateGroup.setRequired(false);
        
        Option gui = new Option("gu", "gui", false, "Simulate an user which interacts with the Scheduler using a GUI(all scheduler state will be downloaded at login)");
        gui.setRequired(false);
        initialStateGroup.addOption(gui);
        
        Option cli = new Option("cu", "cli", false, "Simulate an user which interacts with the Scheduler using a CLI(the scheduler state will NOT be downloaded at login)");
        cli.setRequired(false);
        initialStateGroup.addOption(cli);
        
        options.addOptionGroup(initialStateGroup);
        
        Option myEventsOnly = new Option("me", "myEvsOnly", false, "While registering a listener, get only the events which concern me");
        myEventsOnly.setRequired(false);
        options.addOption(myEventsOnly);
	}

	private static void printHelp(Options options) {
		HelpFormatter hf = new HelpFormatter();
		hf.setWidth(160);
		hf.printHelp(COMMAND_NAME ,options, true);
	}

	private static class SchedulerUsersCoordinator {
		private final String gcmad;
		private final String virtualNode;
		private final String schedulerUrl;
		private final String jobDescriptor;
		private final Options options;
		private final boolean registerListeners;
		private final ListenerInfo listenerInfo;
		private final int jobRepeats;
		private final Class<? extends AbstractSchedulerUser<JobId>> schedulerUserClazz;
		
		public SchedulerUsersCoordinator(String gcmad, String virtualNode,
				String schedulerUrl, Class<? extends AbstractSchedulerUser<JobId>> schedulerUserClazz,
				String jobDescriptor, boolean registerListeners, 
				ListenerInfo listenerInfo, int jobRepeats, Options options) {
			super();
			this.gcmad = gcmad;
			this.virtualNode = virtualNode;
			this.schedulerUrl = schedulerUrl;
			this.schedulerUserClazz = schedulerUserClazz;
			this.jobDescriptor = jobDescriptor;
			this.registerListeners = registerListeners;
			this.listenerInfo = listenerInfo;
			this.jobRepeats = jobRepeats;
			this.options = options;
		}
		
		public SchedulerUsersCoordinator(String gcmad, String virtualNode,
				String schedulerUrl, Class<? extends AbstractSchedulerUser<JobId>> schedulerUserClazz,
				String jobDescriptor, int jobRepeats, Options options)
		{
			this(gcmad,virtualNode,schedulerUrl,schedulerUserClazz,jobDescriptor,false,null,jobRepeats,options);
		}
		
		public SchedulerUsersCoordinator(String gcmad, String virtualNode,
				String schedulerUrl, Class<? extends AbstractSchedulerUser<JobId>> schedulerUserClazz,
				String jobDescriptor, ListenerInfo listenerInfo, 
				int jobRepeats, Options options)
		{
			this(gcmad,virtualNode,schedulerUrl,schedulerUserClazz,jobDescriptor,true,listenerInfo,jobRepeats,options);
		}
		
		// info on whether we are using a single login/pwd combination
		// or a login.cfg file
		private boolean singleLogin;
		private String username;
		private String password;
		private String loginPath;
		
		private void setSingleLogin(String username, String pwd) {
			this.singleLogin = true;
			this.username = username;
			this.password = pwd;
		}
		
		private void setFileLogin(String loginFilePath) {
			this.singleLogin = false;
			this.loginPath = loginFilePath;
		}
		
		public void internalMain() {
			try {
				SchedulerAuthenticationInterface sai = SchedulerConnection.join(schedulerUrl);
				PublicKey pubKey = sai.getPublicKey();
				
				SchedulerFixture jobSubmissionScenario;
				if(registerListeners)
					jobSubmissionScenario =	new SchedulerFixture(gcmad,virtualNode,listenerInfo);
				else 
					jobSubmissionScenario = new SchedulerFixture(gcmad,virtualNode);
				JobSubmissionAction jobAction = new JobSubmissionAction(jobDescriptor);
				
				jobSubmissionScenario.loadInfrastructure();
				List<AbstractSchedulerUser<JobId>> schedulerUsers = null;
				if(singleLogin) {
					Credentials credentials = Credentials.createCredentials(username, password, pubKey);
					schedulerUsers = jobSubmissionScenario.deployConnectedUsers(schedulerUserClazz, schedulerUrl, credentials);
				} else {
					List<Credentials> credentials = loadCredentials(pubKey);
					Credentials[] credentialsArray = credentials.toArray(new Credentials[0]);
					schedulerUsers = jobSubmissionScenario.deployConnectedUsers(schedulerUserClazz,
							schedulerUrl, credentialsArray);
				}
				
				String answer = "y";
				while(answer.toLowerCase().equals("y")) {
					// launching the jobs in parallel
					for(int i=0; i<this.jobRepeats; i++) 
						jobSubmissionScenario.launchSameJob(schedulerUsers, jobAction);
					
					System.out.println("Would you like to run the scenario again?[y/n]");
					BufferedReader console =
						new BufferedReader(
								new InputStreamReader(
										System.in));
					answer = console.readLine();
				}

				System.out.println("Cleanup...");
				jobSubmissionScenario.cleanup();
			} catch(IllegalArgumentException e){
				System.err.println("Invalid argument: " + e.getMessage());
				printHelp(options);
				System.exit(ERROR_EXIT_CODE);
			} catch(NodeException e){
				System.err.println("Could not create the Active Actors - there was something wrong with a Node : " + e.getMessage());
				System.exit(ERROR_EXIT_CODE);
			} catch(ActiveObjectCreationException e) {
				System.err.println("Could not create the Active Actors, reason: " + e.getMessage());
				System.exit(ERROR_EXIT_CODE);
			} catch(ProActiveException e) {
				System.err.println("Failed to deploy the nodes from the GCM descriptor, reason:" + e.getMessage());
				System.exit(ERROR_EXIT_CODE);
			} catch (IOException e) {
				// outta here!
				System.exit(0);
			} catch (KeyException e) {
				System.err.println("Cannot create Credentials for user " + username + ":" + password
				                     + " reason: " + e.getMessage());
				System.exit(ERROR_EXIT_CODE);
			} catch (LoginException e) {
				System.err.println("One of the Scheduler users could not log in, reason:" + e.getMessage());
				System.exit(ERROR_EXIT_CODE);
			} catch (SchedulerException e) {
				System.err.println("One of the Scheduler users could not log in, reason:" + e.getMessage());
				System.exit(ERROR_EXIT_CODE);
			} catch (CannotRegisterListenerException e) {
				System.err.println("Trouble with scheduler listener registration:" + e.getMessage());
				System.exit(ERROR_EXIT_CODE);
			}
		}

		private List<Credentials> loadCredentials(PublicKey pubKey) {
			List<Credentials> credentialsList = new ArrayList<Credentials>();
			try {
				File loginFile = new File(loginPath);
				// routine checks of the path
				if(!loginFile.exists())
					throw new IllegalArgumentException("File " + loginPath + " does not exist");
				if(!loginFile.isFile())
					throw new IllegalArgumentException("The path " + loginPath + " does not point to a file");
				if(!loginFile.canRead())
					throw new IllegalArgumentException("The file " + loginPath + " cannot be read - maybe check your permissions?");

				BufferedReader loginFileReader = new BufferedReader(
						new FileReader(loginFile));
				
				String line;
				while( (line = loginFileReader.readLine()) != null ) {
					String[] lineTokens = line.split(":");
					if(lineTokens.length != 2) {
						throw new IllegalArgumentException("Illegal format for the file " + loginPath + " it contains the line " + line + ", but it should only contain lines of the form username:password");
					}
					String user = lineTokens[0];
					String pwd = lineTokens[1];
					Credentials credentials = Credentials.createCredentials(user, pwd, pubKey);
					credentialsList.add(credentials);
				}
			} catch(FileNotFoundException e) {
				throw new IllegalArgumentException("File " + loginPath + " does not exist");
			} catch (IOException e) {
				// return the credentials available until now - if any
				if(credentialsList.size()!=0)
					return credentialsList;
				else
					throw new IllegalArgumentException("Could not read any Credentials from the file " + loginPath);
			} catch (KeyException e) {
				throw new IllegalArgumentException("Invalid key:" + pubKey);
			}
			return credentialsList;
		}
		
	}
	
}
