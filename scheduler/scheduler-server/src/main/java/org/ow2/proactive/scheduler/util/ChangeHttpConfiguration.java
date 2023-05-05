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
package org.ow2.proactive.scheduler.util;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.OperatingSystem;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.web.WebProperties;
import org.ow2.proactive_grid_cloud_portal.webapp.PortalConfiguration;

import com.google.common.base.Strings;


/**
 * Command-line tool to change the Http configuration inside the ProActive server
 */
public class ChangeHttpConfiguration {

    private static final String newline = System.lineSeparator();

    public static final String HELP_OPTION = "h";

    public static final String HELP_OPTION_NAME = "help";

    public static final String SCHEME_OPTION = "S";

    public static final String SCHEME_OPTION_NAME = "scheme";

    public static final String HOSTNAME_OPTION = "H";

    public static final String HOSTNAME_OPTION_NAME = "hostname";

    public static final String PUBLIC_SCHEME_OPTION_NAME = "public-scheme";

    public static final String PUBLIC_HOSTNAME_OPTION_NAME = "public-hostname";

    public static final String PUBLIC_PORT_OPTION_NAME = "public-port";

    public static final String PORT_OPTION = "P";

    public static final String PORT_OPTION_NAME = "port";

    public static final String DEBUG_OPTION = "d";

    public static final String DEBUG_OPTION_NAME = "debug";

    private static final String APPLICATIONS_PROPERTIES_SEARCH_BASE_PATH = "dist/war";

    private static final String APPLICATIONS_PROPERTIES_SEARCH_SUB_PATH = "WEB-INF/classes/application.properties";

    private static final String RM_CONF_SUB_PATH = "rm.conf";

    private static final String SCHEDULER_CONF_SUB_PATH = "scheduler.conf";

    private static final String VALID_HOSTNAME_REGEXP = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    private static final String VALID_IP_REGEXP = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    private static final String HTTP_URL_REGEXP = "=\\s*https?://[a-zA-Z0-9\\-\\.]+:[1-9][0-9]*";

    private static final Pattern hostnamePattern = Pattern.compile(VALID_HOSTNAME_REGEXP);

    private static final Pattern ipPattern = Pattern.compile(VALID_IP_REGEXP);

    private static final Pattern urlPattern = Pattern.compile(HTTP_URL_REGEXP);

    private static boolean isDebug = false;

    private static boolean isHttps = false;

    private static Integer port = null;

    private static String configuredHostname = null;

    private static boolean publicUrlConfigured = false;

    private static String configuredPublicScheme = null;

    private static String configuredPublicHostname = null;

    private static String configuredPublicPort = null;

    private static String configuredPublicUrl = null;

    /**
     * Entry point
     *
     * @param args arguments, try '-h' for help
     * @see org.ow2.proactive.web.WebProperties
     */
    public static void main(String[] args) {

        try {
            if (changeHttpConfiguration(args)) {
                log("Applied configuration: scheme=" + (isHttps ? "https" : "http") + " hostname=" +
                    configuredHostname + " port=" + port +
                    (publicUrlConfigured ? " publicUrl=" + configuredPublicUrl : ""));
                switch (OperatingSystem.resolveOrError(System.getProperty("os.name")).getFamily()) {
                    case LINUX:
                    case UNIX:
                        log(newline +
                            "NOTE: if ProActive is installed as a service under /etc/init.d/proactive-scheduler,");
                        log("1) Edit this file and set PROTOCOL=" + (isHttps ? "https" : "http") + ", PORT=" + port +
                            ", and eventually ALIAS=" + configuredHostname);
                        log("2) Run the command \"sudo systemctl daemon-reload\"");
                }

            }
        } catch (ChangeHttpConfigurationException e) {
            System.err.println("ERROR : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(e.getCause().getMessage());
            }
            if (e.getAdditionalInfo() != null) {
                log(e.getAdditionalInfo());
            }
            System.exit(1);
        } catch (Exception e) {
            System.err.println("ERROR : " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println(e.getCause().getMessage());
            }
            e.printStackTrace();
            System.exit(1);
        }

        System.exit(0);
    }

    public static boolean changeHttpConfiguration(String... args)
            throws ChangeHttpConfigurationException, IOException, URISyntaxException {
        Options options = new Options();
        CommandLine cmd = getCommandLine(args, options);

        String scheme = null;
        String hostname = null;
        String port = null;

        if (cmd.hasOption(HELP_OPTION_NAME) || cmd.getOptions().length == 0) {
            displayHelp(options);
            return false;
        }

        if (cmd.hasOption(SCHEME_OPTION_NAME)) {
            scheme = cmd.getOptionValue(SCHEME_OPTION_NAME).trim();
            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                exitWithErrorMessage(SCHEME_OPTION_NAME + " must be either http or https.", null, null);
            }
        }

        if (cmd.hasOption(PUBLIC_SCHEME_OPTION_NAME)) {
            configuredPublicScheme = cmd.getOptionValue(PUBLIC_SCHEME_OPTION_NAME).trim();
            if (!"http".equals(configuredPublicScheme) && !"https".equals(configuredPublicScheme)) {
                exitWithErrorMessage(PUBLIC_SCHEME_OPTION_NAME + " must be either http or https.", null, null);
            }
        }

        if (cmd.hasOption(HOSTNAME_OPTION_NAME)) {
            hostname = cmd.getOptionValue(HOSTNAME_OPTION_NAME).trim();
            Matcher hostnameMatcher = hostnamePattern.matcher(hostname);
            Matcher ipMatcher = ipPattern.matcher(hostname);
            if (!hostnameMatcher.find() && !ipMatcher.find()) {
                exitWithErrorMessage(HOSTNAME_OPTION_NAME + " must be either a valid hostname or ip address.",
                                     null,
                                     null);
            }

        }

        if (cmd.hasOption(PUBLIC_HOSTNAME_OPTION_NAME)) {
            configuredPublicHostname = cmd.getOptionValue(PUBLIC_HOSTNAME_OPTION_NAME).trim();
            Matcher hostnameMatcher = hostnamePattern.matcher(configuredPublicHostname);
            Matcher ipMatcher = ipPattern.matcher(configuredPublicHostname);
            if (!hostnameMatcher.find() && !ipMatcher.find()) {
                exitWithErrorMessage(PUBLIC_HOSTNAME_OPTION_NAME + " must be either a valid hostname or ip address.",
                                     null,
                                     null);
            }

        }

        if (cmd.hasOption(PORT_OPTION_NAME)) {
            port = cmd.getOptionValue(PORT_OPTION_NAME).trim();
            try {
                int portValue = Integer.parseInt(port);
                if (portValue < 0 || portValue > 65353) {
                    throw new IllegalArgumentException();
                }
                if (portValue < 1024) {
                    System.err.println("WARN: port range from 0 to 1023 are accessible only to privileged users. Using it within ProActive require additional operating system configuration.");
                }
            } catch (Exception e) {
                exitWithErrorMessage(PORT_OPTION_NAME + " must be a valid port value.", null, null);
            }
        }

        if (cmd.hasOption(PUBLIC_PORT_OPTION_NAME)) {
            configuredPublicPort = cmd.getOptionValue(PUBLIC_PORT_OPTION_NAME).trim();
            try {
                int portValue = Integer.parseInt(configuredPublicPort);
                if (portValue < 0 || portValue > 65353) {
                    throw new IllegalArgumentException();
                }
            } catch (Exception e) {
                exitWithErrorMessage(PUBLIC_PORT_OPTION_NAME + " must be a valid port value.", null, null);
            }
        }

        if (cmd.hasOption(DEBUG_OPTION_NAME)) {
            isDebug = true;
        }

        if (!Strings.isNullOrEmpty(configuredPublicScheme) || !Strings.isNullOrEmpty(configuredPublicHostname) ||
            !Strings.isNullOrEmpty(configuredPublicPort)) {
            if (Strings.isNullOrEmpty(configuredPublicScheme) || Strings.isNullOrEmpty(configuredPublicHostname) ||
                Strings.isNullOrEmpty(configuredPublicPort)) {
                exitWithErrorMessage("When either " + PUBLIC_SCHEME_OPTION_NAME + ", " + PUBLIC_HOSTNAME_OPTION_NAME +
                                     " or " + PUBLIC_PORT_OPTION_NAME +
                                     " is configured, all three options must be configured.", null, null);
            } else {
                publicUrlConfigured = true;
                configuredPublicUrl = configuredPublicScheme + "://" + configuredPublicHostname + ":" +
                                      configuredPublicPort;
            }

        }

        if (Strings.isNullOrEmpty(scheme) && Strings.isNullOrEmpty(hostname) && Strings.isNullOrEmpty(port)) {
            exitWithErrorMessage("At least one option needs to be provided, nothing to do.", null, null);
        }
        // resolve the final configuration to apply, based on the command line parameters and the existing ProActive configuration.
        setCurrentConfiguration(scheme, port);
        // update the config/web/settings.ini file
        changeWebPropertiesConfiguration();
        if (publicUrlConfigured) {
            // update the config/scheduler/settings.ini file
            changeSchedulerPropertiesConfiguration();
        }
        // update all application.properties files in dist/war
        changeAllApplicationPropertiesConfiguration(hostname);
        return true;
    }

    /**
     * Build the command line options and parse
     */
    private static CommandLine getCommandLine(String[] args, Options options) throws ChangeHttpConfigurationException {
        Option opt = new Option(HELP_OPTION, HELP_OPTION_NAME, false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(DEBUG_OPTION, DEBUG_OPTION_NAME, false, "Debug mode (prints modified files and properties)");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(SCHEME_OPTION,
                         SCHEME_OPTION_NAME,
                         false,
                         "Http protocol to use (http or https). When this option is not provided, the currently configured protocol will remain unchanged.");
        opt.setRequired(false);
        opt.setArgName(SCHEME_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         PUBLIC_SCHEME_OPTION_NAME,
                         false,
                         "Public protocol used (http or https). This setting should be used when the ProActive server is deployed behind a reverse proxy or inside a cloud instance. This option must be defined when any of " +
                                PUBLIC_HOSTNAME_OPTION_NAME + " or " + PUBLIC_PORT_OPTION_NAME + " is set.");
        opt.setRequired(false);
        opt.setArgName(PUBLIC_SCHEME_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(HOSTNAME_OPTION,
                         HOSTNAME_OPTION_NAME,
                         true,
                         "Hostname used (e.g. localhost, myserver) or IP address (e.g. 127.0.0.1, 192.168.12.1). When this option is not provided, the hostname appearing in existing configuration files urls will be unchanged.");
        opt.setRequired(false);
        opt.setArgName(HOSTNAME_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         PUBLIC_HOSTNAME_OPTION_NAME,
                         true,
                         "Public Hostname used (e.g. myserver.mydomain) or IP address (e.g. 192.168.12.1). This setting should be used when the ProActive server is deployed behind a reverse proxy or inside a cloud instance. This option must be defined when any of " +
                               PUBLIC_SCHEME_OPTION_NAME + " or " + PUBLIC_PORT_OPTION_NAME + " is set.");
        opt.setRequired(false);
        opt.setArgName(PUBLIC_HOSTNAME_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(PORT_OPTION,
                         PORT_OPTION_NAME,
                         true,
                         "Port used (e.g. 8080, 8443). When this option is not provided, the port configured for the current scheme (http or https) will be used.");
        opt.setRequired(false);
        opt.setArgName(PORT_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(null,
                         PUBLIC_PORT_OPTION_NAME,
                         true,
                         "Public port used (e.g. 8080, 8443). This setting should be used when the ProActive server is deployed behind a reverse proxy or inside a cloud instance. This option must be defined when any of " +
                               PUBLIC_SCHEME_OPTION_NAME + " or " + PUBLIC_HOSTNAME_OPTION_NAME + " is set.");
        opt.setRequired(false);
        opt.setArgName(PUBLIC_PORT_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (Exception e) {
            exitWithErrorMessage(newline + e.getMessage() + newline, "type -h or --help to display help screen", null);
        }
        return cmd;
    }

    private static void exitWithErrorMessage(String errorMessage, String infoMessage, Throwable e)
            throws ChangeHttpConfigurationException {
        throw new ChangeHttpConfigurationException(errorMessage, e, infoMessage);
    }

    private static void displayHelp(Options options) {
        String header = newline +
                        "Change ProActive Jetty (Web) Server parameters and apply it to all configuration files." +
                        newline;
        header += "This command can be used, for example, to change the ProActive server HTTP port or switch the server to the HTTPS protocol." +
                  newline + newline;

        String footer = newline + "Examples: " + newline;
        footer += "# Configure scheme to https with port 8444 and default hostname. Debug output to see all modifications." +
                  newline;
        footer += "configure-http -S https -P 8444 -d" + newline;
        footer += "# Configure scheme to http with default port and specific hostname" + newline;
        footer += "configure-http -S http -H myserver" + newline;
        footer += "# Configure public address to use the server behind a reverse-proxy" + newline;
        footer += "configure-http --public-scheme=https --public-hostname=myproxy.mycompany.com --public-port=8443" +
                  newline;
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("configure-http" + Tools.shellExtension(), header, options, footer, true);
    }

    private static File getSchedulerFile(String path) {
        if (new File(path).isAbsolute()) {
            return new File(path);
        } else {
            File pathName = new File(PASchedulerProperties.SCHEDULER_HOME.getValueAsString(), path);
            try {
                return new File(pathName.getCanonicalPath());
            } catch (IOException e) {
                return new File(pathName.getAbsolutePath());
            }
        }
    }

    /**
     * Return the config/web/settings.ini file
     */
    private static File getWebPropertiesFile() {
        return getSchedulerFile(WebProperties.PA_WEB_PROPERTIES_RELATIVE_FILEPATH);
    }

    private static File getSchedulerPropertiesFile() {
        return getSchedulerFile(PASchedulerProperties.PA_SCHEDULER_PROPERTIES_RELATIVE_FILEPATH);
    }

    /**
     * Find all application.properties files inside the dist/war microservice
     * @return a list of application.properties file paths
     */
    private static List<File> findAllPropertiesFiles() throws ChangeHttpConfigurationException {
        final List<File> answer = new ArrayList<>();
        File distWarDirectory = getSchedulerFile(APPLICATIONS_PROPERTIES_SEARCH_BASE_PATH);
        if (!distWarDirectory.exists() || !distWarDirectory.isDirectory() || !distWarDirectory.canRead()) {
            throw new ChangeHttpConfigurationException("Directory " + distWarDirectory + " cannot be accessed");
        }
        for (File applicationFolder : distWarDirectory.listFiles()) {
            File applicationPropertiesFile = null;
            if ("rm".equals(applicationFolder.getName())) {
                applicationPropertiesFile = new File(applicationFolder, RM_CONF_SUB_PATH);
            } else if ("scheduler".equals(applicationFolder.getName())) {
                applicationPropertiesFile = new File(applicationFolder, SCHEDULER_CONF_SUB_PATH);
            } else if (applicationFolder.isDirectory()) {
                applicationPropertiesFile = new File(applicationFolder, APPLICATIONS_PROPERTIES_SEARCH_SUB_PATH);

            }
            checkApplicationPropertiesFile(answer, applicationPropertiesFile);
        }
        return answer;
    }

    /**
     * Check that an application properties file exists and can be written to
     * @param answer list where this application.properties file will be added
     * @param applicationPropertiesFile file to analyse
     * @throws ChangeHttpConfigurationException when the application.properties file cannot be modified
     */
    private static void checkApplicationPropertiesFile(List<File> answer, File applicationPropertiesFile)
            throws ChangeHttpConfigurationException {
        if (applicationPropertiesFile != null && applicationPropertiesFile.exists() &&
            applicationPropertiesFile.isFile()) {
            if (!applicationPropertiesFile.canWrite()) {
                throw new ChangeHttpConfigurationException("File " + applicationPropertiesFile + " cannot be modified");
            }
            answer.add(applicationPropertiesFile);
        }
    }

    /**
     * Apply the current configuration to config/web/settings.ini
     */
    private static void changeWebPropertiesConfiguration() throws IOException {
        File webPropertiesFile = getWebPropertiesFile();
        if (isDebug) {
            log("Checking " + webPropertiesFile);
        }
        List<String> inputLines;
        try (BufferedReader reader = new BufferedReader(new FileReader(webPropertiesFile))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInWebPropertiesFile(line));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(webPropertiesFile))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
    }

    /**
     * Apply the current configuration to config/web/settings.ini
     */
    private static void changeSchedulerPropertiesConfiguration() throws IOException {
        File schedPropertiesFile = getSchedulerPropertiesFile();
        if (isDebug) {
            log("Checking " + schedPropertiesFile);
        }
        List<String> inputLines;
        try (BufferedReader reader = new BufferedReader(new FileReader(schedPropertiesFile))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInSchedulerPropertiesFile(line));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(schedPropertiesFile))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
    }

    private static void log(String line) {
        System.out.println(line);
    }

    /**
     * Apply the current configuration to all application.properties files
     */
    private static void changeAllApplicationPropertiesConfiguration(String hostname)
            throws IOException, ChangeHttpConfigurationException, URISyntaxException {
        List<File> propertiesFiles = findAllPropertiesFiles();
        for (File applicationPropertiesFile : propertiesFiles) {
            changeApplicationPropertiesConfiguration(applicationPropertiesFile, hostname);
        }
    }

    /**
     * Apply the current configuration to one application.properties file
     */
    private static void changeApplicationPropertiesConfiguration(File applicationPropertiesFile, String hostname)
            throws IOException, URISyntaxException {
        List<String> inputLines;
        if (isDebug) {
            log("DEBUG: Checking " + applicationPropertiesFile);
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(applicationPropertiesFile))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInApplicationPropertiesFile(line, hostname));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(applicationPropertiesFile))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
    }

    /**
     * Use the provided command-line and the current configuration in config/web/settings.ini
     * to determine the overall configuration that must be applied.
     * @param scheme provided scheme parameter in the command line
     * @param port provided port parameter in the command line
     */
    private static void setCurrentConfiguration(String scheme, String port) {
        System.setProperty(WebProperties.REST_HOME.getKey(), PASchedulerProperties.SCHEDULER_HOME.getValueAsString());
        WebProperties.load();

        if (scheme != null) {
            isHttps = "https".equals(scheme);
        } else {
            isHttps = WebProperties.WEB_HTTPS.getValueAsBoolean();
        }
        if (port != null) {
            ChangeHttpConfiguration.port = Integer.parseInt(port);
        } else {
            ChangeHttpConfiguration.port = isHttps ? WebProperties.WEB_HTTPS_PORT.getValueAsInt()
                                                   : WebProperties.WEB_HTTP_PORT.getValueAsInt();
        }
    }

    private static String answerAndLog(String line) {
        if (isDebug) {
            log("DEBUG:  -> Set " + line);
        }
        return line;
    }

    /**
     * Analyse a line inside the config/web/settings.ini file
     * Return a modified line if a parameter needs to be updated
     * @param line input line
     * @return a modified or identical line
     */
    private static String analyseLineInWebPropertiesFile(String line) {
        line = line.trim();
        if (!isHttps && line.startsWith(WebProperties.WEB_HTTP_PORT.getKey())) {
            return answerAndLog(WebProperties.WEB_HTTP_PORT.getKey() + "=" + port);
        }
        if (isHttps && line.startsWith(WebProperties.WEB_HTTPS_PORT.getKey())) {
            return answerAndLog(WebProperties.WEB_HTTPS_PORT.getKey() + "=" + port);
        }
        if (publicUrlConfigured && line.contains(PortalConfiguration.NOVNC_URL.getKey() + "=")) {
            return answerAndLog(PortalConfiguration.NOVNC_URL.getKey() + "=" + configuredPublicScheme + "://" +
                                configuredPublicHostname + ":" + PortalConfiguration.NOVNC_PORT.getValueAsString());
        }
        Pattern webHttpsAllowAnyCertificatePattern = Pattern.compile(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE.getKey() +
                                                                     "\\s*=");
        Matcher webHttpsAllowAnyCertificateMatcher = webHttpsAllowAnyCertificatePattern.matcher(line);
        if (isHttps && webHttpsAllowAnyCertificateMatcher.find()) {
            return answerAndLog(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE.getKey() + "=true");
        }
        Pattern webHttpsAllowAnyHostnamePattern = Pattern.compile(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getKey() +
                                                                  "\\s*=");
        Matcher webHttpsAllowAnyHostnameMatcher = webHttpsAllowAnyHostnamePattern.matcher(line);
        if (isHttps && webHttpsAllowAnyHostnameMatcher.find()) {
            return answerAndLog(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getKey() + "=true");
        }
        Pattern webHttpsPattern = Pattern.compile(WebProperties.WEB_HTTPS.getKey() + "\\s*=");
        Matcher webHttpsMatcher = webHttpsPattern.matcher(line);
        if (webHttpsMatcher.find()) {
            return answerAndLog(WebProperties.WEB_HTTPS.getKey() + "=" + isHttps);
        }
        return line;
    }

    /**
     * Analyse a line inside the config/scheduler/settings.ini file
     * Return a modified line if a parameter needs to be updated
     * @param line input line
     * @return a modified or identical line
     */
    private static String analyseLineInSchedulerPropertiesFile(String line) {
        line = line.trim();
        if (line.contains(PASchedulerProperties.SCHEDULER_REST_PUBLIC_URL.getKey() + "=")) {
            return answerAndLog(PASchedulerProperties.SCHEDULER_REST_PUBLIC_URL.getKey() + "=" + configuredPublicUrl +
                                "/rest");
        }
        if (line.contains(PASchedulerProperties.CATALOG_REST_PUBLIC_URL.getKey() + "=")) {
            return answerAndLog(PASchedulerProperties.CATALOG_REST_PUBLIC_URL.getKey() + "=" + configuredPublicUrl +
                                "/catalog");
        }
        if (line.contains(PASchedulerProperties.CLOUD_AUTOMATION_REST_PUBLIC_URL.getKey() + "=")) {
            return answerAndLog(PASchedulerProperties.CLOUD_AUTOMATION_REST_PUBLIC_URL.getKey() + "=" +
                                configuredPublicUrl + "/cloud-automation-service");
        }
        return line;
    }

    /**
     * Analyse a line inside one application.properties file
     * Return a modified line if a parameter needs to be updated
     * @param line input line
     * @return a modified or identical line
     */
    private static String analyseLineInApplicationPropertiesFile(String line, String hostname)
            throws URISyntaxException {
        line = line.trim();
        Matcher urlMatcher = urlPattern.matcher(line);
        if (urlMatcher.find()) {
            String urlInFile = urlMatcher.group();
            // remove the starting =
            urlInFile = urlInFile.replaceFirst("=\\s*", "");
            URI oldUri = new URI(urlInFile);
            if (configuredHostname == null) {
                // store configured hostname to display the final configuration message
                configuredHostname = hostname != null ? hostname : oldUri.getHost();
            }
            URI newUri = new URI(isHttps ? "https" : "http",
                                 oldUri.getUserInfo(),
                                 hostname != null ? hostname : oldUri.getHost(),
                                 port,
                                 oldUri.getPath(),
                                 oldUri.getQuery(),
                                 oldUri.getFragment());
            return answerAndLog(line.replaceAll(HTTP_URL_REGEXP, "=" + newUri));
        }
        Pattern paWebHttpsAllowAnyCertificatePattern = Pattern.compile("pa." +
                                                                       WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE.getKey() +
                                                                       "\\s*=");
        Matcher paWebHttpsAllowAnyCertificateMatcher = paWebHttpsAllowAnyCertificatePattern.matcher(line);
        if (isHttps && paWebHttpsAllowAnyCertificateMatcher.find()) {
            return answerAndLog("pa." + WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE.getKey() + "=true");
        }
        Pattern paWebHttpsAllowAnyHostnamePattern = Pattern.compile("pa." +
                                                                    WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getKey() +
                                                                    "\\s*=");
        Matcher paWebHttpsAllowAnyHostnameMatcher = paWebHttpsAllowAnyHostnamePattern.matcher(line);
        if (isHttps && paWebHttpsAllowAnyHostnameMatcher.find()) {
            return answerAndLog("pa." + WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getKey() + "=true");
        }
        Pattern webHttpsAllowAnyCertificatePattern = Pattern.compile(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE.getKey() +
                                                                     "\\s*=");
        Matcher webHttpsAllowAnyCertificateMatcher = webHttpsAllowAnyCertificatePattern.matcher(line);
        if (isHttps && webHttpsAllowAnyCertificateMatcher.find()) {
            return answerAndLog(WebProperties.WEB_HTTPS_ALLOW_ANY_CERTIFICATE.getKey() + "=true");
        }
        Pattern webHttpsAllowAnyHostnamePattern = Pattern.compile(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getKey() +
                                                                  "\\s*=");
        Matcher webHttpsAllowAnyHostnameMatcher = webHttpsAllowAnyHostnamePattern.matcher(line);
        if (isHttps && webHttpsAllowAnyHostnameMatcher.find()) {
            return answerAndLog(WebProperties.WEB_HTTPS_ALLOW_ANY_HOSTNAME.getKey() + "=true");
        }
        return line;
    }

    static class ChangeHttpConfigurationException extends Exception {
        public String getAdditionalInfo() {
            return additionalInfo;
        }

        private String additionalInfo = null;

        public ChangeHttpConfigurationException(String message) {
            super(message);
        }

        public ChangeHttpConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ChangeHttpConfigurationException(String message, Throwable cause, String additionalInfo) {
            super(message, cause);
            this.additionalInfo = additionalInfo;
        }
    }
}
