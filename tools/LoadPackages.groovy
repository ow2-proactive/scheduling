
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipFile
import org.apache.log4j.Logger

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
		this.EXAMPLES_DIR_PATH = this.EXAMPLES_ZIP_PATH.substring(0,this.EXAMPLES_ZIP_PATH.lastIndexOf("."))

		// Create a new instance of the package loader
		File load_package_script = new File(this.TOOLS_DIR, this.LOAD_PACKAGE_SCRIPT_NAME)
		if (load_package_script.exists()) {
			GroovyClassLoader gcl = new GroovyClassLoader()
			Class loadPackageClass = gcl.parseClass(load_package_script)
			this.package_loader= (GroovyObject) loadPackageClass.newInstance(binding)
		} else {
			return
		}
	}


	def unzipFile(src, dest) {
		def zipFile = new ZipFile(src)
		zipFile.entries().each { it ->
			def path = Paths.get(dest + "/" + it.name)
			if(it.directory){
				Files.createDirectories(path)
			}
			else {
				def parentDir = path.getParent()
				if (!Files.exists(parentDir)) {
					Files.createDirectories(parentDir)
				}
				Files.copy(zipFile.getInputStream(it), path)
			}
		}
	}

	def writeToOutput(String output) {
		logger.info("[" + this.SCRIPT_NAME + "] " + output)
	}


	def run() {
		writeToOutput(" Automatic deployment of proactive packages ...")

		writeToOutput(" Variables : ")
		writeToOutput(" EXAMPLES_ZIP_PATH " + this.EXAMPLES_ZIP_PATH)
		writeToOutput(" EXAMPLES_DIR_PATH " + this.EXAMPLES_DIR_PATH)

		writeToOutput(" Actions : ")

		// If the unzipped dir already exists, stop the script execution
		def examples_dir = new File(this.EXAMPLES_DIR_PATH)
		if (examples_dir.exists())
		{
			writeToOutput(this.EXAMPLES_DIR_PATH + " already exists, delete it to redeploy packages.")
			writeToOutput("Terminated.")
			return
		}

		// Unzip the examples
		def examples_zip = new File(this.EXAMPLES_ZIP_PATH)
		if (!examples_zip.exists())
		{
			writeToOutput(this.EXAMPLES_ZIP_PATH + " not found!")
			return

		}
		unzipFile(examples_zip, this.EXAMPLES_DIR_PATH)
		writeToOutput(this.EXAMPLES_ZIP_PATH + " extracted!")


		//retrieve all (package, bucket) pairs
		def package_bucket_map = [:]
		examples_dir.eachFile() { package_dir ->
			def metadata_file = new File(package_dir.absolutePath, "METADATA.json")
			if (metadata_file.exists()) {
				def slurper = new groovy.json.JsonSlurper()
				def metadata_file_map = (Map) slurper.parseText(metadata_file.text)
				def bucket_name = metadata_file_map.get("catalog").get("bucket")
				package_bucket_map.put(package_dir, bucket_name)
			}
		}

		// Retrieve the ordered bucket list
		def ordered_bucket_list = []
		def ordered_bucket_list_file = new File(examples_dir, "ordered_bucket_list")
		ordered_bucket_list_file.text.split(",").each { bucket_name ->
			ordered_bucket_list.add(bucket_name)
			// Define a specific filter on package directories to only consider those targeting the current bucket
			def file_filter = new FileFilter() {
				boolean accept(File package_dir) {
					//def package_dir = new File(pathname)
					def metadata_file = new File(package_dir.absolutePath, "METADATA.json")
					if (metadata_file.exists()) {
						// From json to map
						def slurper = new groovy.json.JsonSlurper()
						def metadata_file_map = (Map) slurper.parseText(metadata_file.text)
						return bucket_name = metadata_file_map.get("catalog").get("bucket")
					}
					return false
				}
			}

			// For each package targeting the current bucket call the package loader class
			examples_dir.listFiles(file_filter).each { package_dir ->
				package_bucket_map.remove(package_dir)
				package_loader.run(package_dir)
			}

		}
		//check in the package directory map if there are buckets not defined in the ordered_bucket_list file
		if (package_bucket_map.size() != 0) {
			package_bucket_map.each { key, value ->
				if (!ordered_bucket_list.contains(value)) {
					//append the new bucket name at the end of the ordered_bucket_list file
					ordered_bucket_list_file.append("," + value)
				}
			}
		}
		writeToOutput(" ... proactive packages deployed!")
		writeToOutput(" Terminated.")
	}
}

try {
	new LoadPackages(this.binding).run()
} catch (Exception e) {
	println "Failed to load examples into the catalog."+ e.getMessage()
}

