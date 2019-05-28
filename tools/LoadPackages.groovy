import org.apache.commons.configuration2.Configuration
import org.apache.commons.configuration2.PropertiesConfiguration
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder
import org.apache.commons.configuration2.builder.fluent.Parameters
import org.apache.commons.configuration2.builder.fluent.PropertiesBuilderParameters
import org.apache.commons.configuration2.ex.ConfigurationException
import org.apache.log4j.Logger
import org.codehaus.groovy.runtime.StackTraceUtils

import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import java.util.Arrays
import org.apache.commons.lang3.ArrayUtils



class LoadPackages {

    private static final String PROACTIVE_EXAMPLES_LOADED = "proactive.examples.loaded"
    private static final String PROACTIVE_EXAMPLES_LAST_LOADED_PACKAGE = "proactive.examples.last.loaded.package"
    private static final FileBasedConfigurationBuilder<PropertiesConfiguration> BUILDER = new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class)
    private static Configuration configuration
    private static String lastLoadedPackage

    private final String SCRIPT_NAME = "LoadPackages.groovy"

    private final String LOAD_PACKAGE_SCRIPT_NAME = "LoadPackage.groovy"


    private final String SCHEDULER_HOME
    private final String EXAMPLES_ZIP_PATH
    private final String EXAMPLES_DIR_PATH
    private final File SAMPLES_PROPERTIES_FILE
    private final String TOOLS_DIR
    private final GroovyObject package_loader

    private logger = Logger.getLogger("org.ow2.proactive.scheduler")

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                configuration.setProperty(PROACTIVE_EXAMPLES_LAST_LOADED_PACKAGE, lastLoadedPackage)
                BUILDER.save()

            }
        })
    }


    LoadPackages(binding) {

        // Bindings
        this.SCHEDULER_HOME = binding.variables.get("pa.scheduler.home")

        // User variables
        this.EXAMPLES_ZIP_PATH = new File(this.SCHEDULER_HOME, "samples/proactive-examples.zip").absolutePath
        this.TOOLS_DIR = new File(this.SCHEDULER_HOME, "tools")
        this.SAMPLES_PROPERTIES_FILE = new File(this.SCHEDULER_HOME, "samples/samples.properties")

        // Load configuration
        this.configuration = loadConfig()

        // Deduced variables
        this.EXAMPLES_DIR_PATH = this.SCHEDULER_HOME + "/samples/workflows/proactive-examples"

        // Create a new instance of the package loader
        File load_package_script = new File(this.TOOLS_DIR, this.LOAD_PACKAGE_SCRIPT_NAME)
        if (load_package_script.exists()) {
            GroovyClassLoader gcl = new GroovyClassLoader()
            Class loadPackageClass = gcl.parseClass(load_package_script)
            this.package_loader = (GroovyObject) loadPackageClass.newInstance(binding)
        } else {
            return
        }
    }


    void unzipFile(src, dest) {
        def zipFile = new ZipFile(src)
        zipFile.entries().each { it ->
            def path = Paths.get(dest + "/" + it.name)
            if (it.directory) {
                Files.createDirectories(path)
            } else {
                def parentDir = path.getParent()
                if (!Files.exists(parentDir)) {
                    Files.createDirectories(parentDir)
                }
                Files.copy(zipFile.getInputStream(it), path)
            }
        }
    }

    public void writeToOutput(output) {
        logger.info("[" + this.SCRIPT_NAME + "] " + output)
    }

    public void writeError(output, exception) {
        logger.error("[" + this.SCRIPT_NAME + "] " + output, exception)
    }

    /**
     * loads Proactive examples configuration.
     *
     * @return Proactive example configuration
     * @throws ConfigurationException If a problem occurs when loading proactive examples configuration
     * @since version 8.4.0
     */
    public Configuration loadConfig() throws ConfigurationException {

        Configuration config

        PropertiesBuilderParameters propertyParameters = new Parameters().properties()
        propertyParameters.setFile(SAMPLES_PROPERTIES_FILE)
        propertyParameters.setThrowExceptionOnMissing(true)

        BUILDER.configure(propertyParameters)

        config = BUILDER.getConfiguration()

        writeToOutput("Proactive examples configuration loaded")

        return config
    }

    public void updateConfig(){
        configuration.setProperty(PROACTIVE_EXAMPLES_LOADED, Boolean.TRUE.toString())
        configuration.setProperty(PROACTIVE_EXAMPLES_LAST_LOADED_PACKAGE, "")
        BUILDER.save()
    }

    def run() {

        if (!configuration.getBoolean(PROACTIVE_EXAMPLES_LOADED)) {
            writeToOutput(" Automatic deployment of proactive packages ...")
            writeToOutput(" Variables : ")
            writeToOutput(" EXAMPLES_ZIP_PATH " + this.EXAMPLES_ZIP_PATH)
            writeToOutput(" EXAMPLES_DIR_PATH " + this.EXAMPLES_DIR_PATH)

            writeToOutput(" Actions : ")

            def examples_dir = new File(this.EXAMPLES_DIR_PATH)
            // If the unzipped dir already exists, do nothing
            if (examples_dir.exists()) {
                writeToOutput(this.EXAMPLES_DIR_PATH + " already exists, delete it to redeploy packages.")
                writeToOutput("Terminated.")
            } else {
                // Unzip the examples
                def examples_zip = new File(this.EXAMPLES_ZIP_PATH)
                if (!examples_zip.exists()) {
                    writeToOutput(this.EXAMPLES_ZIP_PATH + " not found!")
                    return
                }
                unzipFile(examples_zip, this.EXAMPLES_DIR_PATH)
                writeToOutput(this.EXAMPLES_ZIP_PATH + " extracted!")
            }

            println "Loading workflow and utility packages..."

            // Connect to the scheduler
            package_loader.loginAdminUserCredToSchedulerAndGetSessionId()

            // Create buckets following the ordered bucket list
            new File(examples_dir, "ordered_bucket_list").text.split(",").each { bucket ->
                package_loader.createBucketIfNotExist(bucket)
            }

            //sort packages to simplify the recovery loading behavior
            def directoryFilter = new FileFilter() {
                boolean accept(File file) {
                    return file.isDirectory()
                }}
            File[] packages = examples_dir.listFiles(directoryFilter)
            Arrays.sort(packages)

            //load only remaining packages in case proactive.examples.last.loaded.package property is not empty
            lastLoadedPackage = configuration.getString(PROACTIVE_EXAMPLES_LAST_LOADED_PACKAGE)
            if(!lastLoadedPackage.equals("")){
                packages = Arrays.copyOfRange(packages, ArrayUtils.indexOf(packages, new File(lastLoadedPackage)), packages.length-1)
            }


            // Load all packages
            for(File package_dir : packages) {
                package_loader.run(package_dir, false)
                lastLoadedPackage = package_dir.getPath()
            }
            //update sample.properties
            updateConfig()

            writeToOutput(" ... proactive packages deployed!")
            println "Packages successfully loaded"
        } else {
            writeToOutput(this.EXAMPLES_DIR_PATH + " already exists, delete it to redeploy packages.")
            println "Workflow and utility packages already loaded"
        }
    }
}

instance = null
try {
    instance = new LoadPackages(this.binding)
    instance.run()
} catch (Exception e) {
    StackTraceUtils.deepSanitize(e)
    instance.writeError("Failed to load examples into the catalog", e)
    throw new Exception("Failed to load examples into the catalog " + e.getMessage())
}
