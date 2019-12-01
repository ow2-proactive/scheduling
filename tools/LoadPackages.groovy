import org.apache.log4j.Logger
import org.codehaus.groovy.runtime.StackTraceUtils

import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import org.apache.commons.io.FileUtils;


class LoadPackages {

    private final String SCRIPT_NAME = "LoadPackages.groovy"

    private final String LOAD_PACKAGE_SCRIPT_NAME = "LoadPackage.groovy"


    private final String SCHEDULER_HOME
    private final String EXAMPLES_ZIP_PATH
    private final String EXAMPLES_DIR_PATH
    private final String TOOLS_DIR
    private final GroovyObject package_loader

    private logger = Logger.getLogger("org.ow2.proactive.scheduler")


    LoadPackages(binding) {

        // Bindings
        this.SCHEDULER_HOME = binding.variables.get("pa.scheduler.home")

        // User variables
        this.EXAMPLES_ZIP_PATH = new File(this.SCHEDULER_HOME, "samples/proactive-examples.zip").absolutePath
        this.TOOLS_DIR = new File(this.SCHEDULER_HOME, "tools")

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

    def run() {

        writeToOutput(" Automatic deployment of proactive packages ...")
        writeToOutput(" Variables : ")
        writeToOutput(" EXAMPLES_ZIP_PATH " + this.EXAMPLES_ZIP_PATH)
        writeToOutput(" EXAMPLES_DIR_PATH " + this.EXAMPLES_DIR_PATH)

        writeToOutput(" Actions : ")

        def examples_dir = new File(this.EXAMPLES_DIR_PATH)
        def packages_loaded_file = new File(this.SCHEDULER_HOME, "samples/packages.loaded")

        // If packages.loaded file already exists, do nothing
        if (packages_loaded_file.exists() && !packages_loaded_file.isDirectory()) {
            writeToOutput(this.EXAMPLES_DIR_PATH + " already exists, delete it to redeploy packages.")
            writeToOutput("Terminated.")
            println "Workflow and utility packages already loaded"
            return
        } else {
            FileUtils.deleteQuietly(examples_dir)
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
        new File(examples_dir, "ordered_bucket_list").text.split("[\\s,]+").each { bucket ->
            package_loader.createBucketIfNotExist(bucket)
        }

        // Load all packages
        examples_dir.eachDir { package_dir ->
            package_loader.run(package_dir, false)
        }

        packages_loaded_file.createNewFile()

        writeToOutput(" ... proactive packages deployed!")
        println "Packages successfully loaded"
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
