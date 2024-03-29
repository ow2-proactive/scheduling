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
package org.ow2.proactive.scheduler.task.executors.forked.env;

import static org.ow2.proactive.scheduler.common.task.ForkEnvironment.DOCKER_FORK_WINDOWS2LINUX;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.config.PAProperty;
import org.objectweb.proactive.core.config.PAPropertyString;
import org.objectweb.proactive.extensions.pamr.PAMRConfig;
import org.objectweb.proactive.extensions.processbuilder.OSProcessBuilder;
import org.ow2.proactive.scheduler.common.task.ForkEnvironment;
import org.ow2.proactive.scheduler.common.util.VariableSubstitutor;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.scheduler.task.containers.ScriptExecutableContainer;
import org.ow2.proactive.scheduler.task.context.TaskContext;
import org.ow2.proactive.scheduler.task.context.TaskContextVariableExtractor;
import org.ow2.proactive.scheduler.task.executors.forked.env.command.JavaPrefixCommandExtractor;
import org.ow2.proactive.scripting.ForkEnvironmentScriptResult;
import org.ow2.proactive.scripting.Script;
import org.ow2.proactive.scripting.ScriptResult;
import org.ow2.proactive.utils.OneJar;
import org.ow2.proactive.utils.OperatingSystem;
import org.ow2.proactive.utils.OperatingSystemFamily;

import com.google.common.base.Strings;


public class ForkedJvmTaskExecutionCommandCreator implements Serializable {

    private static final Logger logger = Logger.getLogger(ForkedJvmTaskExecutionCommandCreator.class);

    private static final String JAVA_HOME_POSTFIX_JAVA_EXECUTABLE = File.separatorChar + "bin" + File.separatorChar +
                                                                    "java";

    private static final String JAVA_HOME_LINUX_JAVA_EXECUTABLE = "/bin/java";

    private final TaskContextVariableExtractor taskContextVariableExtractor = new TaskContextVariableExtractor();

    private final JavaPrefixCommandExtractor javaPrefixCommandExtractor = new JavaPrefixCommandExtractor();

    /**
     * Creates a command to start a task inside a java virtual machine.
     *
     * @param taskContext                   TaskContext object describing the task.
     * @param forkEnvironmentScriptResult   Result from a running fork environment script. If it is
     *                                      of instance {@link ForkEnvironmentScriptResult}, the script return
     *                                      variables will be used for the construction of the command.
     * @param serializedContextAbsolutePath The serialized TaskContext object which will be read by the virtual
     *                                      machine to run the task.
     * @return A List, empty if the TaskContext is null, otherwise filled with a command.
     * @throws Exception If the {@link TaskContextVariableExtractor} could not extract all variables from the
     *                   TaskContext.
     */
    public List<String> createForkedJvmTaskExecutionCommand(TaskContext taskContext,
            ScriptResult forkEnvironmentScriptResult, String serializedContextAbsolutePath,
            OSProcessBuilder processBuilder) throws Exception {
        if (taskContext == null) {
            return new ArrayList<>(0);
        }
        Map<String, Serializable> variables = taskContextVariableExtractor.getAllVariables(taskContext);
        String javaHome = System.getProperty("java.home");
        List<String> jvmArguments = new ArrayList<>(1);

        ForkEnvironment forkEnvironment = null;
        boolean isDockerWindowsToLinuxTmp = false;
        if (taskContext.getInitializer() != null) {
            forkEnvironment = taskContext.getInitializer().getForkEnvironment();
            if (forkEnvironment != null) {
                isDockerWindowsToLinuxTmp = forkEnvironment.isDockerWindowsToLinux();
            }
        }
        final boolean isDockerWindowsToLinux = isDockerWindowsToLinuxTmp;
        // set the task fork property so that script engines have a mean to know
        // if they are running in a forked task or not
        jvmArguments.add(PASchedulerProperties.TASK_FORK.getCmdLine() + "true");
        if (isDockerWindowsToLinux) {
            jvmArguments.add("-D" + DOCKER_FORK_WINDOWS2LINUX + "=true");
        }

        configureLogging(jvmArguments, variables, isDockerWindowsToLinux, processBuilder);

        StringBuilder classpath = new StringBuilder("." + getPathSeparator(isDockerWindowsToLinux));
        if (!System.getProperty("java.class.path", "").contains("node.jar")) {
            // in case the class path of the node is not built with the node.jar, we
            // build the classpath with wildcards to avoid command too long errors on windows
            String classPathEntries = getStandardClassPathEntries(variables,
                                                                  isDockerWindowsToLinux,
                                                                  processBuilder).toString();
            classpath.append(convertToLinuxClassPathIfNeeded(isDockerWindowsToLinux, classPathEntries));
        }

        for (String classpathEntry : OneJar.getClasspath()) {
            classpath.append(getPathSeparator(isDockerWindowsToLinux))
                     .append(convertToLinuxClassPathIfNeeded(isDockerWindowsToLinux, classpathEntry));
        }

        if (forkEnvironment != null) {
            for (String jvmArgument : forkEnvironment.getJVMArguments()) {
                jvmArguments.add(VariableSubstitutor.filterAndUpdate(jvmArgument, variables));
            }

            for (String classpathEntry : forkEnvironment.getAdditionalClasspath()) {
                // classpath defined in the fork environment does not need to be converted to linux (as we expect the user to provide the correct path)
                VariableSubstitutor.filterAndUpdate(classpathEntry, variables);
                classpath.append(getPathSeparator(isDockerWindowsToLinux))
                         .append(VariableSubstitutor.filterAndUpdate(classpathEntry, variables));
            }

            if (!Strings.isNullOrEmpty(forkEnvironment.getJavaHome())) {
                javaHome = VariableSubstitutor.filterAndUpdate(forkEnvironment.getJavaHome(), variables);
            }
        }

        // The following code forwards the PAMR configuration the forked JVM. Though the general use-case involves to
        // write a custom fork environment script to configure properly the properties, this avoids a common misconfiguration issues.
        forwardProActiveProperties(jvmArguments,
                                   PAMRConfig.PA_NET_ROUTER_ADDRESS,
                                   PAMRConfig.PA_NET_ROUTER_PORT,
                                   PAMRConfig.PA_PAMR_SOCKET_FACTORY,
                                   PAMRConfig.PA_PAMRSSH_KEY_DIR,
                                   PAMRConfig.PA_PAMRSSH_REMOTE_ADDRESS,
                                   PAMRConfig.PA_PAMRSSH_REMOTE_USERNAME,
                                   PAMRConfig.PA_PAMRSSH_REMOTE_PORT,
                                   CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOL,
                                   CentralPAPropertyRepository.PA_COMMUNICATION_ADDITIONAL_PROTOCOLS,
                                   CentralPAPropertyRepository.PA_COMMUNICATION_PROTOCOLS_ORDER,
                                   CentralPAPropertyRepository.PA_CLASSLOADING_USEHTTP,
                                   CentralPAPropertyRepository.PA_NET_USE_IP_ADDRESS);

        forwardOtherProperties(jvmArguments,
                               createPAPropertyString("proactive.node.nodesource", "Default", false),
                               createPAPropertyString("java.io.tmpdir", "", true),
                               createPAPropertyString("file.encoding", "UTF-8", true));

        addPropertiesForGrabAnnotation(jvmArguments,
                                       taskContext,
                                       createPAPropertyString("javax.xml.validation.SchemaFactory",
                                                              "http://www.w3.org/2001/XMLSchema=com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory",
                                                              false),
                                       createPAPropertyString("javax.xml.parsers.SAXParserFactory",
                                                              "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl",
                                                              false),
                                       createPAPropertyString("javax.xml.parsers.DocumentBuilderFactory",
                                                              "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl",
                                                              false));

        List<String> prefixes = javaPrefixCommandExtractor.extractJavaPrefixCommandToCommandListFromScriptResult(forkEnvironmentScriptResult);
        if (prefixes.isEmpty() && forkEnvironment != null) {
            prefixes.addAll(forkEnvironment.getPreJavaCommand());
        }

        List<String> javaCommand = new ArrayList<>(prefixes.size() + 3 + jvmArguments.size() + 2);
        javaCommand.addAll(prefixes);
        javaCommand.add(isDockerWindowsToLinux ? javaHome + JAVA_HOME_LINUX_JAVA_EXECUTABLE
                                               : processBuilder.getShortPath(getJavaCommandPath(javaHome)));

        javaCommand.add("-cp");
        javaCommand.add(classpath.toString());
        javaCommand.addAll(jvmArguments.stream()
                                       .map(arg -> isDockerWindowsToLinux ? ForkEnvironment.convertToLinuxPathInJVMArgument(arg)
                                                                          : arg)
                                       .collect(Collectors.toList()));
        javaCommand.add(ExecuteForkedTaskInsideNewJvm.class.getName());
        javaCommand.add(convertToLinuxPathIfNeeded(isDockerWindowsToLinux,
                                                   serializedContextAbsolutePath,
                                                   processBuilder));

        if (logger.isDebugEnabled()) {
            logger.debug("Forked JVM command : " + javaCommand);
        }
        return javaCommand;
    }

    private String getJavaCommandPath(String javaHome) {
        return javaHome + JAVA_HOME_POSTFIX_JAVA_EXECUTABLE +
               (OperatingSystem.resolveOrError(System.getProperty("os.name"))
                               .getFamily() == OperatingSystemFamily.WINDOWS ? ".exe" : "");
    }

    private PAPropertyString createPAPropertyString(String name, String defaultValue, boolean isSystemProp) {
        PAPropertyString paProperty = new PAPropertyString(name, isSystemProp, defaultValue);
        try {
            paProperty.setValue(System.getProperty(name));
        } catch (NullPointerException npe) {
            logger.warn("System property " + name + " can't be found.");
        }
        return paProperty;
    }

    private String convertToLinuxPathIfNeeded(boolean isDockerWindowsToLinux, String serializedContextAbsolutePath,
            OSProcessBuilder processBuilder) throws IOException {
        return isDockerWindowsToLinux ? ForkEnvironment.convertToLinuxPath(serializedContextAbsolutePath)
                                      : processBuilder.getShortPath(serializedContextAbsolutePath);
    }

    private String convertToLinuxClassPathIfNeeded(boolean isDockerWindowsToLinux, String classPathEntries) {
        return isDockerWindowsToLinux ? ForkEnvironment.convertToLinuxClassPath(classPathEntries) : classPathEntries;
    }

    private Object getPathSeparator(boolean isDockerWindowsToLinux) {
        return isDockerWindowsToLinux ? ":" : File.pathSeparatorChar;
    }

    private void forwardProActiveProperties(List<String> jvmArguments, PAProperty... propertiesToForward) {
        for (PAProperty property : propertiesToForward) {
            if (property.isSet() && !propertyDefinedByScript(jvmArguments, property)) {
                jvmArguments.add(property.getCmdLine() + property.getValueAsString());
            }
        }
    }

    /**
     * The following method adds the received PA properties to JVM arguments.
     * @param jvmArguments Source JVM arguments.
     * @param propertiesToForward PA properties that has to be forwarded to the forked JVM.
     * @return
     */
    private void forwardOtherProperties(List<String> jvmArguments, PAProperty... propertiesToForward) {
        for (PAProperty property : propertiesToForward) {
            if (property.isSet() && !propertyDefinedByScript(jvmArguments, property)) {
                jvmArguments.add(property.getCmdLine() + property.getValueAsString());
            }
        }
    }

    private void addPropertiesForGrabAnnotation(List<String> jvmArguments, TaskContext context,
            PAProperty... propertiesToForward) throws IOException {
        if (context != null && context.getExecutableContainer() != null) {
            if (isScriptUsingGrabAnnotation(((ScriptExecutableContainer) context.getExecutableContainer()).getScript()) ||
                isScriptUsingGrabAnnotation(context.getPreScript()) ||
                isScriptUsingGrabAnnotation(context.getPostScript()) ||
                isScriptUsingGrabAnnotation(context.getControlFlowScript())) {
                for (PAProperty property : propertiesToForward) {
                    if (property.getDefaultValueAsString() != null &&
                        !propertyDefinedByScript(jvmArguments, property)) {
                        jvmArguments.add(property.getCmdLine() + property.getDefaultValueAsString());
                    }
                }
            }
        }
    }

    private boolean isScriptUsingGrabAnnotation(Script<?> script) throws IOException {
        if (script == null) {
            return false;
        }
        script.fetchUrlIfNeeded();
        if (!"groovy".equals(script.getEngineName())) {
            return false;
        }
        if (script.getScript() == null || !script.getScript().contains("@Grab")) {
            return false;
        }
        return true;
    }

    private boolean propertyDefinedByScript(List<String> jvmArguments, PAProperty property) {
        return jvmArguments.stream().anyMatch(s ->

        s.contains(property.getCmdLine()));
    }

    private void configureLogging(List<String> jvmArguments, Map<String, Serializable> variables,
            boolean isDockerWindowsToLinux, OSProcessBuilder processBuilder) {
        String log4jFileUrl = null;
        String schedulerHome = getSchedulerHome(variables);
        String log4jConfig = schedulerHome + File.separator + "config" + File.separator + "log" + File.separator +
                             "scriptengines.properties";

        if (new File(log4jConfig).exists()) {
            try {
                String canonicalPath = new File(log4jConfig).getCanonicalPath();
                log4jFileUrl = "file:" +
                               (convertToLinuxPathIfNeeded(isDockerWindowsToLinux, canonicalPath, processBuilder));
            } catch (IOException e) {
                logger.warn("Error when converting log4j path: " + log4jConfig, e);
            }
        } else {
            URL log4jConfigFromJar = ForkedJvmTaskExecutionCommandCreator.class.getResource("/config/log/scriptengines.properties");
            if (log4jConfigFromJar != null) {
                log4jFileUrl = log4jConfigFromJar.toString();
                if (isDockerWindowsToLinux) {
                    // complex case where the drive is embedded in the jar url
                    log4jFileUrl = log4jFileUrl.replaceFirst("/([a-zA-Z]):/", "/$1/");
                }
            } else {
                logger.warn("Cannot find log4j configuration file for forked JVM, logging disabled");
            }
        }
        if (log4jFileUrl != null) {

            jvmArguments.add(CentralPAPropertyRepository.LOG4J.getCmdLine() + log4jFileUrl);
        }
    }

    private String getSchedulerHome(Map<String, Serializable> variables) {
        String schedulerHome;
        schedulerHome = System.getProperty(CentralPAPropertyRepository.PA_HOME.getName());
        if (schedulerHome == null) {
            schedulerHome = (String) variables.get("PA_SCHEDULER_HOME");
        }
        return schedulerHome;
    }

    private StringBuilder getStandardClassPathEntries(Map<String, Serializable> variables,
            boolean isDockerWindowsToLinux, OSProcessBuilder processBuilder) throws IOException {
        StringBuilder classpathEntries = new StringBuilder();
        String schedulerHome = getSchedulerHome(variables);

        if (schedulerHome != null) {
            File paHome = new File(schedulerHome).getCanonicalFile();
            File distLib = new File(paHome, "dist/lib").getCanonicalFile();
            File distLibShort = getPathForCurrentOperatingSystem(isDockerWindowsToLinux, processBuilder, distLib);
            if (distLib.exists()) {
                File addons = new File(paHome, "addons").getCanonicalFile();
                File addonsClient = new File(new File(paHome, "addons"), "client").getCanonicalFile();
                File addonsShort = getPathForCurrentOperatingSystem(isDockerWindowsToLinux, processBuilder, addons);
                File addonsClientShort = getPathForCurrentOperatingSystem(isDockerWindowsToLinux,
                                                                          processBuilder,
                                                                          addonsClient);
                classpathEntries.append(distLibShort);
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(new File(distLibShort, "*"));
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(addonsShort);
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(new File(addonsShort, "*"));
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(addonsClientShort);
                classpathEntries.append(File.pathSeparatorChar);
                classpathEntries.append(new File(addonsClientShort, "*"));
            } else {
                return getClassPathEntriesUsingJavaClassPath();
            }
        } else {
            return getClassPathEntriesUsingJavaClassPath();
        }

        return classpathEntries;

    }

    private File getPathForCurrentOperatingSystem(boolean isDockerWindowsToLinux, OSProcessBuilder processBuilder,
            File folderPath) throws IOException {
        // do not shorten the path if dockerWindowsToLinux is used
        return new File(isDockerWindowsToLinux ? folderPath.getAbsolutePath()
                                               : processBuilder.getShortPath(folderPath.getAbsolutePath()));
    }

    private StringBuilder getClassPathEntriesUsingJavaClassPath() {
        StringBuilder classpathEntries = new StringBuilder();
        classpathEntries.append(System.getProperty("java.class.path", ""));
        return classpathEntries;
    }
}
