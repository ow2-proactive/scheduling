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
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;
import org.ow2.proactive.core.properties.PropertyDecrypter;
import org.ow2.proactive.scheduler.core.properties.PASchedulerProperties;
import org.ow2.proactive.utils.Tools;
import org.ow2.proactive.web.WebProperties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;


/**
 * Command-line tool to change the database configuration inside the ProActive server
 */
public class ChangeDBConfiguration {

    private static final String newline = System.lineSeparator();

    public static final String HELP_OPTION = "h";

    public static final String HELP_OPTION_NAME = "help";

    public static final String DB_VENDOR_OPTION = "v";

    public static final String DB_VENDOR_OPTION_NAME = "database-vendor";

    public static final String COMPONENT_OPTION = "c";

    public static final String COMPONENT_OPTION_NAME = "component";

    public static final String SCHEMA_OPTION = "s";

    public static final String SCHEMA_OPTION_NAME = "schema-name";

    public static final String URL_OPTION = "U";

    public static final String URL_OPTION_NAME = "url";

    public static final String DB_HOST_OPTION = "H";

    public static final String DB_HOST_OPTION_NAME = "hostname";

    public static final String DB_PORT_OPTION = "P";

    public static final String DB_PORT_OPTION_NAME = "port";

    public static final String USERNAME_OPTION = "u";

    public static final String USERNAME_OPTION_NAME = "username";

    public static final String PASSWORD_OPTION = "p";

    public static final String PASSWORD_OPTION_NAME = "password";

    public static final String DIALECT_OPTION = "D";

    public static final String DIALECT_OPTION_NAME = "dialect";

    public static final String MYSQL_TIMEZONE_OPTION = "z";

    public static final String MYSQL_TIMEZONE_OPTION_NAME = "timezone";

    public static final String DEBUG_OPTION = "d";

    public static final String DEBUG_OPTION_NAME = "debug";

    public static final String HSQLDB_SERVER_ADDRESS = "server.address";

    public static final String HSQLDB_SERVER_PORT = "server.port";

    public static final String HIBERNATE_CONNECTION_DRIVER_CLASS = "hibernate.connection.driver_class";

    public static final String HIBERNATE_CONNECTION_URL = "hibernate.connection.url";

    public static final String HIBERNATE_DIALECT = "hibernate.dialect";

    public static final String HIBERNATE_CONNECTION_USERNAME = "hibernate.connection.username";

    public static final String HIBERNATE_CONNECTION_PASSWORD = "hibernate.connection.password";

    public static final String SPRING_DATASOURCE_DRIVER_CLASS = "spring.datasource.driverClassName";

    public static final String SPRING_DATASOURCE_URL = "spring.datasource.url";

    public static final String SPRING_DATASOURCE_DIALECT = "spring.jpa.database-platform";

    public static final String SPRING_DATASOURCE_USERNAME = "spring.datasource.username";

    public static final String SPRING_DATASOURCE_PASSWORD = "spring.datasource.password";

    public static final String ALT_SPRING_DATASOURCE_DRIVER_CLASS = "spring.datasource.driver-class-name";

    private static final Map<String, String> configurationFilesPerTarget = new HashMap<>();

    // map of database properties file per component for hsqldb
    // (contains the user credentials that will be created when the database is first created)
    private static final Map<String, String> hsqldbConfigurationFilesPerTarget = ImmutableMap.of("rm",
                                                                                                 "config/rm/database.properties",
                                                                                                 "scheduler",
                                                                                                 "config/scheduler/database.properties",
                                                                                                 "catalog",
                                                                                                 "config/catalog/database.properties",
                                                                                                 "service-automation",
                                                                                                 "config/pca/database.properties",
                                                                                                 "notification",
                                                                                                 "config/notification-service/database.properties");

    // main hsqldb configuration file (contains the HSQLDB server port)
    private static final String MAIN_HSQLDB_CONFIGURATION = "config/hsqldb-server.properties";

    private static final String ADDONS_FOLDER = "addons";

    private static final String ALL_COMPONENTS = "all";

    // all existing databases components
    private static final Set<String> services = ImmutableSet.of("rm",
                                                                "scheduler",
                                                                "catalog",
                                                                "service-automation",
                                                                "notification");

    private static final String HSQLDB = "hsqldb";

    private static final String POSTGRESQL = "postgresql";

    private static final String MYSQL = "mysql";

    private static final String ORACLE = "oracle";

    // supported database vendors
    private static final Set<String> vendors = ImmutableSet.of(HSQLDB, POSTGRESQL, MYSQL, ORACLE);

    // sets of ProActive services that all use the scheduler database
    private static final Set<String> servicesWithSchedulerDB = ImmutableSet.of("scheduler",
                                                                               "job-planner",
                                                                               "proactive-cloud-watch",
                                                                               "scheduling-api");

    // JDBC jar prefix name per database vendor
    private static final Map<String, String> vendorsJDBCDrivers = ImmutableMap.of(POSTGRESQL,
                                                                                  "postgresql-",
                                                                                  MYSQL,
                                                                                  "mysql-connector-",
                                                                                  ORACLE,
                                                                                  "ojdbc8");

    // hibernate dialect per database vendor
    private static final Map<String, String> vendorsDefaultDialects = ImmutableMap.of(HSQLDB,
                                                                                      "org.hibernate.dialect.HSQLDialect",
                                                                                      POSTGRESQL,
                                                                                      "org.hibernate.dialect.PostgreSQL94Dialect",
                                                                                      MYSQL,
                                                                                      "org.hibernate.dialect.MySQL5InnoDBDialect",
                                                                                      ORACLE,
                                                                                      "org.hibernate.dialect.Oracle12cDialect");

    // JDBC driver class name per database vendor
    private static final Map<String, String> vendorsDrivers = ImmutableMap.of(HSQLDB,
                                                                              "org.hsqldb.jdbc.JDBCDriver",
                                                                              POSTGRESQL,
                                                                              "org.postgresql.Driver",
                                                                              MYSQL,
                                                                              "com.mysql.jdbc.Driver",
                                                                              ORACLE,
                                                                              "oracle.jdbc.driver.OracleDriver");

    // Default port per database vendor
    private static final Map<String, String> defaultPorts = ImmutableMap.of(HSQLDB,
                                                                            "9001",
                                                                            POSTGRESQL,
                                                                            "5432",
                                                                            MYSQL,
                                                                            "3307",
                                                                            ORACLE,
                                                                            "1521");

    // Default url format per database vendor
    private static final Map<String, String> vendorsUrls = ImmutableMap.of(HSQLDB,
                                                                           "jdbc:hsqldb:hsql://%s:%s/%s",
                                                                           POSTGRESQL,
                                                                           "jdbc:postgresql://%s:%s/%s",
                                                                           MYSQL,
                                                                           "jdbc:mysql://%s:%s/%s?verifyServerCertificate=false&allowPublicKeyRetrieval=true&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=%s",
                                                                           ORACLE,
                                                                           "jdbc:oracle:thin:@%s:%s:%s");

    // Database schema name per ProActive service
    private static final Map<String, String> defaultSchemaNames = ImmutableMap.of("rm",
                                                                                  "rm",
                                                                                  "scheduler",
                                                                                  "scheduler",
                                                                                  "catalog",
                                                                                  "catalog",
                                                                                  "service-automation",
                                                                                  "pca",
                                                                                  "notification",
                                                                                  "notification");

    private static final String VALID_HOSTNAME_REGEXP = "^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\\-]*[a-zA-Z0-9])\\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])$";

    private static final String VALID_IP_REGEXP = "^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    private static final Pattern hostnamePattern = Pattern.compile(VALID_HOSTNAME_REGEXP);

    private static final Pattern ipPattern = Pattern.compile(VALID_IP_REGEXP);

    private static boolean isDebug = false;

    private static String component = null;

    private static String vendor = null;

    private static String schemaName = null;

    private static String username = null;

    private static String password = null;

    private static String url = null;

    private static String hostname = null;

    private static String port = null;

    private static String dialect = null;

    private static String timezone = null;

    /**
     * Entry point
     *
     * @param args arguments, try '-h' for help
     * @see WebProperties
     */
    public static void main(String[] args) {

        configurationFilesPerTarget.put("rm", "config/rm/database.properties");
        configurationFilesPerTarget.put("scheduler", "config/scheduler/database.properties");
        configurationFilesPerTarget.put("catalog", "dist/war/catalog/WEB-INF/classes/application.properties");
        configurationFilesPerTarget.put("service-automation",
                                        "dist/war/cloud-automation-service/WEB-INF/classes/application.properties");
        configurationFilesPerTarget.put("job-planner", "dist/war/job-planner/WEB-INF/classes/application.properties");
        configurationFilesPerTarget.put("notification",
                                        "dist/war/notification-service/WEB-INF/classes/application.properties");
        configurationFilesPerTarget.put("proactive-cloud-watch",
                                        "dist/war/proactive-cloud-watch/WEB-INF/classes/application.properties");
        configurationFilesPerTarget.put("scheduling-api",
                                        "dist/war/scheduling-api/WEB-INF/classes/application.properties");

        try {
            if (changeDBConfiguration(args)) {
                log(vendor + " database configuration successfully applied to " + component + ".");
            }
        } catch (ChangeDBConfigurationException e) {
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

    public static boolean changeDBConfiguration(String... args)
            throws ChangeDBConfigurationException, IOException, URISyntaxException {
        Options options = new Options();
        CommandLine cmd = getCommandLine(args, options);

        if (cmd.hasOption(HELP_OPTION_NAME) || cmd.getOptions().length == 0) {
            displayHelp(options);
            return false;
        }

        if (cmd.hasOption(COMPONENT_OPTION_NAME)) {
            component = cmd.getOptionValue(COMPONENT_OPTION_NAME).trim();
            Set<String> allServices = new LinkedHashSet<>();
            allServices.add(ALL_COMPONENTS);
            allServices.addAll(services);
            if (!allServices.contains(component)) {
                exitWithErrorMessage("--" + COMPONENT_OPTION_NAME + " must be one of " + allServices, null, null);
            }
        } else {
            exitWithErrorMessage("--" + COMPONENT_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(DB_VENDOR_OPTION_NAME)) {
            vendor = cmd.getOptionValue(DB_VENDOR_OPTION_NAME).trim();
            if (!vendors.contains(vendor)) {
                exitWithErrorMessage("--" + DB_VENDOR_OPTION_NAME + " must be one of " + vendors, null, null);
            }
        } else {
            exitWithErrorMessage("--" + DB_VENDOR_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(SCHEMA_OPTION_NAME)) {
            if (component.equals(ALL_COMPONENTS)) {
                exitWithErrorMessage("--" + SCHEMA_OPTION_NAME + " cannot be used when --" + COMPONENT_OPTION_NAME +
                                     "=" + ALL_COMPONENTS, null, null);
            }
            if (vendor.equals(HSQLDB)) {
                exitWithErrorMessage("--" + SCHEMA_OPTION_NAME + " cannot be used when --" + DB_VENDOR_OPTION_NAME +
                                     "=" + HSQLDB, null, null);
            }
            schemaName = cmd.getOptionValue(SCHEMA_OPTION_NAME).trim();
        }

        if (cmd.hasOption(URL_OPTION_NAME)) {
            if (component.equals(ALL_COMPONENTS)) {
                exitWithErrorMessage("--" + URL_OPTION_NAME + " cannot be used when --" + COMPONENT_OPTION_NAME + "=" +
                                     ALL_COMPONENTS, null, null);
            }
            url = cmd.getOptionValue(URL_OPTION_NAME).trim();
        }

        if (cmd.hasOption(USERNAME_OPTION_NAME)) {
            username = cmd.getOptionValue(USERNAME_OPTION_NAME).trim();
        } else {
            exitWithErrorMessage("--" + USERNAME_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(PASSWORD_OPTION_NAME)) {
            password = cmd.getOptionValue(PASSWORD_OPTION_NAME).trim();
            if (!password.startsWith("ENC(")) {
                password = "ENC(" + PropertyDecrypter.getDefaultEncryptor().encrypt(password) + ")";
            }
        } else {
            exitWithErrorMessage("--" + PASSWORD_OPTION_NAME + " is required", null, null);
        }

        if (cmd.hasOption(DB_HOST_OPTION_NAME)) {
            if (url != null) {
                exitWithErrorMessage("When --" + URL_OPTION_NAME + " is provided, the -- " + DB_HOST_OPTION_NAME +
                                     " option cannot be used", null, null);
            }
            hostname = cmd.getOptionValue(DB_HOST_OPTION_NAME).trim();
            Matcher hostnameMatcher = hostnamePattern.matcher(hostname);
            Matcher ipMatcher = ipPattern.matcher(hostname);
            if (!hostnameMatcher.find() && !ipMatcher.find()) {
                exitWithErrorMessage(DB_HOST_OPTION_NAME + " must be either a valid hostname or ip address.",
                                     null,
                                     null);
            }
        } else if (url == null) {
            hostname = "localhost";
        }

        if (cmd.hasOption(DB_PORT_OPTION_NAME)) {
            if (url != null) {
                exitWithErrorMessage("When --" + URL_OPTION_NAME + " is provided, the --" + DB_PORT_OPTION +
                                     " option cannot be used", null, null);
            }
            port = cmd.getOptionValue(DB_PORT_OPTION_NAME).trim();
            try {
                int portValue = Integer.parseInt(port);
                if (portValue < 0 || portValue > 65353) {
                    throw new IllegalArgumentException();
                }
                if (portValue < 1024 && vendor.equals(HSQLDB)) {
                    System.err.println("WARN: port range from 0 to 1023 are accessible only to privileged users. Using it within ProActive require additional operating system configuration.");
                }
            } catch (Exception e) {
                exitWithErrorMessage(DB_PORT_OPTION_NAME + " must be a valid port value.", null, null);
            }
        } else if (url == null) {
            port = defaultPorts.get(vendor);
        }

        if (cmd.hasOption(DIALECT_OPTION_NAME)) {
            dialect = cmd.getOptionValue(DIALECT_OPTION_NAME).trim();
        }

        if (cmd.hasOption(MYSQL_TIMEZONE_OPTION_NAME)) {
            if (url != null) {
                exitWithErrorMessage("When --" + URL_OPTION_NAME + " is provided, the --" + MYSQL_TIMEZONE_OPTION_NAME +
                                     " option cannot be used", null, null);
            }
            timezone = cmd.getOptionValue(MYSQL_TIMEZONE_OPTION_NAME).trim();
        } else if (url == null) {
            timezone = "UTC";
        }

        if (cmd.hasOption(DEBUG_OPTION_NAME)) {
            isDebug = true;
        }

        File addonsFolder = getSchedulerFile(ADDONS_FOLDER);

        if (!HSQLDB.equals(vendor)) {
            checkJDBCDriverIsPresentInAddonsFolder(addonsFolder);
        } else {
            // configuration specific to HSQLDB
            log("WARN: HSQLDB does not support password changing once the database has been created.");
            log("WARN: if you provided a different password from the one that was used before, you need to delete the previous database in data/db/" +
                (component.equals(ALL_COMPONENTS) ? defaultSchemaNames.values() : defaultSchemaNames.get(component)));
            changeMainHsqlDbProperties(getSchedulerFile(MAIN_HSQLDB_CONFIGURATION));
        }

        List<String> componentsToConfigure = new ArrayList<>();
        if (component.equals(ALL_COMPONENTS)) {
            log("NOTE: when using --" + COMPONENT_OPTION_NAME + "=" + ALL_COMPONENTS +
                ", the same username/password configuration will be applied to all components");
            componentsToConfigure.addAll(services);
        } else {
            componentsToConfigure.add(component);
        }

        for (String currentComponent : componentsToConfigure) {
            if (url == null || component.equals(ALL_COMPONENTS)) {
                if (vendor.equals(MYSQL)) {
                    url = String.format(vendorsUrls.get(vendor),
                                        hostname,
                                        port,
                                        schemaName == null ? defaultSchemaNames.get(currentComponent) : schemaName,
                                        timezone);
                } else {
                    url = String.format(vendorsUrls.get(vendor),
                                        hostname,
                                        port,
                                        schemaName == null ? defaultSchemaNames.get(currentComponent) : schemaName);
                }
            }
            switch (currentComponent) {
                case "rm":
                    changeDBPropertiesConfiguration(getSchedulerFile(configurationFilesPerTarget.get(currentComponent)));
                    break;
                case "catalog":
                case "service-automation":
                case "notification":
                    if (HSQLDB.equals(vendor)) {
                        changeDBPropertiesConfiguration(getSchedulerFile(hsqldbConfigurationFilesPerTarget.get(currentComponent)));
                    }
                    changeDBPropertiesConfiguration(getSchedulerFile(configurationFilesPerTarget.get(currentComponent)));
                    break;
                case "scheduler":
                    for (String component : servicesWithSchedulerDB) {
                        changeDBPropertiesConfiguration(getSchedulerFile(configurationFilesPerTarget.get(component)));
                    }
                    break;
            }
        }
        return true;
    }

    /**
     * Check that a corresponding JDBC driver is present in the addons folder for the selected vendor
     *
     * Prints a comprehensive message and exits if not
     */
    private static void checkJDBCDriverIsPresentInAddonsFolder(File addonsFolder)
            throws ChangeDBConfigurationException {
        boolean vendorJDBCDriverFound = false;
        for (File file : addonsFolder.listFiles()) {
            if (file.getName().startsWith(vendorsJDBCDrivers.get(vendor)) && file.isFile() && file.canRead()) {
                vendorJDBCDriverFound = true;
            }
        }
        if (!vendorJDBCDriverFound) {
            String driverDownloadMessage = "";
            switch (vendor) {
                case MYSQL:
                    driverDownloadMessage = "You can download the MySQL \"Platform independent\" driver from https://dev.mysql.com/downloads/connector/j/. From the archive, extract the JAR file into the \"addons\" folder.";
                    break;
                case POSTGRESQL:
                    driverDownloadMessage = "You can download the PostgreSQL \"Java 8\" driver from https://jdbc.postgresql.org/download/. Download the JAR file into the \"addons\" folder.";
                    break;
                case ORACLE:
                    driverDownloadMessage = "You can download the Oracle \"Java 8\" driver, corresponding to your Oracle database version, from https://www.oracle.com/fr/database/technologies/appdev/jdbc-downloads.html. Download the JAR file into the \"addons\" folder.";
                    break;
            }
            exitWithErrorMessage("Cannot find " + vendor + " JDBC driver, \"" + vendorsJDBCDrivers.get(vendor) +
                                 "*.jar\", in " + addonsFolder + "." + newline + driverDownloadMessage + newline,
                                 null,
                                 null);
        }
    }

    /**
     * Build the command line options and parse
     */
    private static CommandLine getCommandLine(String[] args, Options options) throws ChangeDBConfigurationException {
        Option opt = new Option(HELP_OPTION, HELP_OPTION_NAME, false, "Display this help");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(DEBUG_OPTION, DEBUG_OPTION_NAME, false, "Debug mode (prints modified files and properties)");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option(COMPONENT_OPTION,
                         COMPONENT_OPTION_NAME,
                         false,
                         "Target component to configure. Component name can be \"rm\", \"scheduler\", \"catalog\", \"service-automation\", \"notification\" or \"all\" (to configure all components with a common configuration).");
        opt.setRequired(false);
        opt.setArgName(COMPONENT_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(SCHEMA_OPTION,
                         SCHEMA_OPTION_NAME,
                         false,
                         "Override the default database schema name for the selected component. Defaults names are { Resource Manager : \"rm\", Scheduler : \"scheduler\", Catalog : \"catalog\", Service Automation : \"pca\", Notification Service : \"notification\" }. This option cannot be used when --" +
                                COMPONENT_OPTION_NAME + "=" + ALL_COMPONENTS + " or when --" + DB_VENDOR_OPTION_NAME +
                                "=hsqldb");
        opt.setRequired(false);
        opt.setArgName(SCHEMA_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(DB_VENDOR_OPTION,
                         DB_VENDOR_OPTION_NAME,
                         false,
                         "Target database vendor to configure (\"hsqldb\", \"postgresql\", \"mysql\" or \"oracle\")");
        opt.setRequired(false);
        opt.setArgName(DB_VENDOR_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(URL_OPTION,
                         URL_OPTION_NAME,
                         false,
                         "Sets the JDBC url that will be used to access the target component database. If not used, the url will be inferred from --" +
                                DB_HOST_OPTION_NAME + ", --" + DB_PORT_OPTION_NAME + " and optionally --" +
                                SCHEMA_OPTION_NAME + " options. This option cannot be used when --" +
                                COMPONENT_OPTION_NAME + "=" + ALL_COMPONENTS +
                                ". Note: this parameter is the only way to use the TNS URL Format with Oracle database.");
        opt.setRequired(false);
        opt.setArgName(URL_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(USERNAME_OPTION, USERNAME_OPTION_NAME, false, "Database user name");
        opt.setRequired(false);
        opt.setArgName(USERNAME_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(PASSWORD_OPTION,
                         PASSWORD_OPTION_NAME,
                         false,
                         "Database user password. Password can be provided already encrypted using the 'encypt' command or in clear text. " +
                                "The password will always be encrypted when stored in configuration files.");
        opt.setRequired(false);
        opt.setArgName(PASSWORD_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(DB_HOST_OPTION,
                         DB_HOST_OPTION_NAME,
                         true,
                         "Database hostname used (e.g. localhost, myserver) or IP address (e.g. 127.0.0.1, 192.168.12.1). This option cannot be used in conjunction with --" +
                               URL_OPTION_NAME + ".");
        opt.setRequired(false);
        opt.setArgName(DB_HOST_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(DB_PORT_OPTION,
                         DB_PORT_OPTION_NAME,
                         true,
                         "Database port used (e.g. 9001, 5432). If this option is not provided, the database vendor default port will be used. This option cannot be used in conjunction with --" +
                               URL_OPTION_NAME + ".");
        opt.setRequired(false);
        opt.setArgName(DB_PORT_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(DIALECT_OPTION,
                         DIALECT_OPTION_NAME,
                         true,
                         "Override default database vendor dialect. Use this option when the default dialect does not match your database version. Defaults are " +
                               vendorsDefaultDialects);
        opt.setRequired(false);
        opt.setArgName(DIALECT_OPTION_NAME.toUpperCase());
        opt.setArgs(1);
        options.addOption(opt);

        opt = new Option(MYSQL_TIMEZONE_OPTION,
                         MYSQL_TIMEZONE_OPTION_NAME,
                         true,
                         "Specific to MySQL, sets the timezone used by the MySQL server. The default is \"UTC\". This option cannot be used in conjunction with --" +
                               URL_OPTION_NAME + ".");
        opt.setRequired(false);
        opt.setArgName(MYSQL_TIMEZONE_OPTION_NAME.toUpperCase());
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
            throws ChangeDBConfigurationException {
        throw new ChangeDBConfigurationException(errorMessage, e, infoMessage);
    }

    private static void displayHelp(Options options) {
        String header = newline + "Change ProActive server database configuration." + newline;
        header += "ProActive can either use the integrated HSQLDB database or an external database (PostgreSQL MySQL or Oracle)." +
                  newline;
        header += "ProActive requires the following components to be mapped to different database schemas:" + newline;
        header += "Resource Manager, Scheduler, Catalog, Service Automation and Notification Service." + newline +
                  newline;

        String footer = newline + "Examples: " + newline;
        footer += "# Configure HSQLDB for all components with single user and password, default hostname and port. Debug output to see all modifications." +
                  newline;
        footer += "configure-db -c all -v hsqldb -u user -p pwd -d" + newline;
        footer += "# Configure PostgreSQL for the Resource Manager component with a specific hostname and port" +
                  newline;
        footer += "configure-db -c rm -v postgresql -H myserver -P 5434 -u user -p pwd" + newline;
        footer += "# Configure PostgreSQL for the Resource Manager component with a specific hostname, port and schema name" +
                  newline;
        footer += "configure-db -c rm -v postgresql -H myserver -P 5434 -s RESOURCE_MANAGER -u user -p pwd" + newline;
        footer += "# Configure Oracle for the Resource Manager component with a TNS URL format" + newline;
        footer += "configure-db -c rm -v oracle -U \"jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=myserver)(PORT=1521))(CONNECT_DATA=(SERVICE_NAME=rm))\" -u user -p pwd" +
                  newline;
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(135);
        hf.printHelp("configure-db" + Tools.shellExtension(), header, options, footer, true);
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

    // check that the provided configuration file can be modified
    private static void checkConfigurationFile(File configurationFile) throws ChangeDBConfigurationException {
        if (configurationFile != null && configurationFile.exists() && configurationFile.isFile()) {
            if (!configurationFile.canWrite()) {
                throw new ChangeDBConfigurationException("File " + configurationFile + " cannot be modified");
            }
        }
    }

    // apply the current configuration to the main HSQLDB configuration file
    private static void changeMainHsqlDbProperties(File propertiesFile)
            throws IOException, ChangeDBConfigurationException {
        if (isDebug) {
            log("DEBUG: Checking " + propertiesFile);
        }
        checkConfigurationFile(propertiesFile);
        List<String> inputLines;
        try (BufferedReader reader = new BufferedReader(new FileReader(propertiesFile))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInMainHsqldbFile(line));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
    }

    // apply the current configuration to one application.properties or database.properties file
    private static void changeDBPropertiesConfiguration(File propertiesFile)
            throws IOException, ChangeDBConfigurationException {
        if (isDebug) {
            log("DEBUG: Checking " + propertiesFile);
        }
        checkConfigurationFile(propertiesFile);
        List<String> inputLines;
        try (BufferedReader reader = new BufferedReader(new FileReader(propertiesFile))) {
            inputLines = IOUtils.readLines(reader);
        }
        List<String> outputLines = new ArrayList<>(inputLines.size());
        for (String line : inputLines) {
            outputLines.add(analyseLineInPropertiesFile(line));
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile))) {
            IOUtils.writeLines(outputLines, null, writer);
        }
    }

    private static void log(String line) {
        System.out.println(line);
    }

    private static String answerAndLog(String line) {
        if (isDebug) {
            log("DEBUG:  -> Set " + line);
        }
        return line;
    }

    /**
     * Analyse a line inside config/hsqldb-server.properties
     * Return a modified line if a parameter needs to be updated
     * @param line input line
     * @return a modified or identical line
     */
    private static String analyseLineInMainHsqldbFile(String line) {
        line = line.trim();
        if (hostname != null && line.startsWith(HSQLDB_SERVER_ADDRESS)) {
            return answerAndLog(HSQLDB_SERVER_ADDRESS + "=" + hostname);
        } else if (port != null && line.startsWith(HSQLDB_SERVER_PORT)) {
            return answerAndLog(HSQLDB_SERVER_PORT + "=" + port);
        }
        return line;
    }

    /**
     * Analyse a line inside a database.properties or application.properties file
     * Return a modified line if a parameter needs to be updated
     * @param line input line
     * @return a modified or identical line
     */
    private static String analyseLineInPropertiesFile(String line) {
        line = line.trim();

        if (line.startsWith(HIBERNATE_CONNECTION_DRIVER_CLASS)) {
            return answerAndLog(HIBERNATE_CONNECTION_DRIVER_CLASS + "=" + vendorsDrivers.get(vendor));
        } else if (line.startsWith(HIBERNATE_CONNECTION_URL)) {
            return answerAndLog(HIBERNATE_CONNECTION_URL + "=" + url);
        } else if (line.startsWith(HIBERNATE_DIALECT)) {
            return answerAndLog(HIBERNATE_DIALECT + "=" +
                                (dialect == null ? vendorsDefaultDialects.get(vendor) : dialect));
        } else if (line.startsWith(HIBERNATE_CONNECTION_USERNAME)) {
            return answerAndLog(HIBERNATE_CONNECTION_USERNAME + "=" + username);
        } else if (line.startsWith(HIBERNATE_CONNECTION_PASSWORD)) {
            return answerAndLog(HIBERNATE_CONNECTION_PASSWORD + "=" + password);
        } else if (line.startsWith(SPRING_DATASOURCE_DRIVER_CLASS)) {
            return answerAndLog(SPRING_DATASOURCE_DRIVER_CLASS + "=" + vendorsDrivers.get(vendor));
        } else if (line.startsWith(SPRING_DATASOURCE_URL)) {
            return answerAndLog(SPRING_DATASOURCE_URL + "=" + url);
        } else if (line.startsWith(SPRING_DATASOURCE_DIALECT)) {
            return answerAndLog(SPRING_DATASOURCE_DIALECT + "=" +
                                (dialect == null ? vendorsDefaultDialects.get(vendor) : dialect));
        } else if (line.startsWith(SPRING_DATASOURCE_USERNAME)) {
            return answerAndLog(SPRING_DATASOURCE_USERNAME + "=" + username);
        } else if (line.startsWith(SPRING_DATASOURCE_PASSWORD)) {
            return answerAndLog(SPRING_DATASOURCE_PASSWORD + "=" + password);
        } else if (line.startsWith(ALT_SPRING_DATASOURCE_DRIVER_CLASS)) {
            return answerAndLog(ALT_SPRING_DATASOURCE_DRIVER_CLASS + "=" + vendorsDrivers.get(vendor));
        }
        return line;
    }

    static class ChangeDBConfigurationException extends Exception {
        public String getAdditionalInfo() {
            return additionalInfo;
        }

        private String additionalInfo = null;

        public ChangeDBConfigurationException(String message) {
            super(message);
        }

        public ChangeDBConfigurationException(String message, Throwable cause) {
            super(message, cause);
        }

        public ChangeDBConfigurationException(String message, Throwable cause, String additionalInfo) {
            super(message, cause);
            this.additionalInfo = additionalInfo;
        }
    }
}
