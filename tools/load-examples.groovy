import org.ow2.proactive.authentication.ConnectionInfo
import org.ow2.proactive.scheduler.rest.SchedulerClient

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile
import org.apache.log4j.Logger

class LoadExamples {

	private final String PATH_TO_SCHEDULER_CREDENTIALS_FILE = "config/authentication/scheduler.cred"

	private final String SCRIPT_NAME = "load-examples.groovy"

	private final String BUCKET_OWNER = "GROUP:public-objects"

	private final String GLOBAL_SPACE_PATH
	private final String SCHEDULER_REST_URL
	private final String SCHEDULER_HOME
	private final String EXAMPLES_ZIP_PATH
	private final File WORKFLOW_TEMPLATES_DIR
	private final String WORKFLOW_TEMPLATES_DIR_PATH
	private final String CATALOG_URL
	private final String EXAMPLES_DIR_PATH

	private logger = Logger.getLogger("org.ow2.proactive.scheduler")


	LoadExamples(binding) {

		// Bindings
		this.GLOBAL_SPACE_PATH = binding.variables.get("pa.scheduler.dataspace.defaultglobal.localpath")
		this.SCHEDULER_REST_URL = binding.variables.get("pa.scheduler.rest.url")
		this.SCHEDULER_HOME = binding.variables.get("pa.scheduler.home")

		// User variables
		this.EXAMPLES_ZIP_PATH = new File(this.SCHEDULER_HOME, "samples/proactive-examples.zip").absolutePath
		this.WORKFLOW_TEMPLATES_DIR = new File(this.SCHEDULER_HOME, "config/workflows/templates")
		this.WORKFLOW_TEMPLATES_DIR_PATH = this.WORKFLOW_TEMPLATES_DIR.absolutePath

		// Deduced variables
		this.CATALOG_URL = this.SCHEDULER_REST_URL.substring(0,this.SCHEDULER_REST_URL.length()-4) + "catalog"
		this.EXAMPLES_DIR_PATH = this.EXAMPLES_ZIP_PATH.substring(0,this.EXAMPLES_ZIP_PATH.lastIndexOf("."))
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

	def loginAdminUserCredToSchedulerAndGetSessionId() {
		writeToOutput( "Scheduler home: " + this.SCHEDULER_HOME)
		writeToOutput( "Scheduler rest: " + this.SCHEDULER_REST_URL)

		def schedulerClient = SchedulerClient.createInstance()
		def schedulerConnectionSettings = new ConnectionInfo(this.SCHEDULER_REST_URL, null, null, new File(this.SCHEDULER_HOME,this.PATH_TO_SCHEDULER_CREDENTIALS_FILE), false)
		schedulerClient.init(schedulerConnectionSettings)

		String sessionID = schedulerClient.getSession()
		writeToOutput("SessionId: "+ sessionID)
		return sessionID
	}

	def isAStudioTemplate (workflow_name, templates_dir_path) {
		try {
			new File(templates_dir_path).eachDir() { dir ->
				if (new File(dir, "name").text == workflow_name)
					throw new Exception()
			}
			return false
		} catch (Exception e) {
			return true
		}
	}


	def run() {

		String sessionId = loginAdminUserCredToSchedulerAndGetSessionId()

		// If the unzipped dir already exists, stop the script execution
		def examples_dir = new File(this.EXAMPLES_DIR_PATH)
		if (examples_dir.exists())
		{
			writeToOutput(this.EXAMPLES_DIR_PATH + " already exists, delete it to redeploy examples.")
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


		// Start by finding the next template dir index
		def template_dir_name = "1"
		// If the wkw template dir does not exist, let's create it
		if (!this.WORKFLOW_TEMPLATES_DIR.exists())
			this.WORKFLOW_TEMPLATES_DIR.mkdirs()
		// If it exists, let's iterate over existing templates
		else
		{
			def templates_dirs_list = []
			new File(this.WORKFLOW_TEMPLATES_DIR_PATH).eachDir { dir ->
				templates_dirs_list << dir.getName().toInteger()
			}
			if (!templates_dirs_list.isEmpty())
				template_dir_name = (templates_dirs_list.sort().last() + 1) + ""
		}
		writeToOutput("Next template dir name " + template_dir_name)

		// For POST queries
		this.class.getClass().getResource(new File(this.SCHEDULER_HOME, "dist/lib/httpclient-4.5.2.jar").absolutePath);
		def boundary = "---------------" + UUID.randomUUID().toString();

		// To parse json files
		def slurper = new groovy.json.JsonSlurper()

		// Retrieve the ordered bucket list
		def ordered_bucket_list_file = new File(examples_dir, "ordered_bucket_list")
		ordered_bucket_list_file.text.split(",").each { bucket_name ->

			// Define a specific filter on package directories to only consider those targeting the current bucket
			def package_related_to_bucket_filter = new FileFilter() {
				boolean accept(File package_dir) {
					def metadata_file = new File(package_dir, "METADATA.json")
					if (metadata_file.exists()) {
						// From json to map
						def metadata_file_map = (Map) slurper.parseText(metadata_file.text)
						return bucket_name == metadata_file_map.get("catalog").get("bucket")
					}
					return false
				}
			}


			// CREATE OR NOT CREATE THE BUCKET ? /////////////////////////////


			// Does the bucket already exist? -------------
			// GET QUERY
			def list_buckets_rest_query = this.CATALOG_URL + "/buckets?owner=" + this.BUCKET_OWNER
			def response =  new URL(list_buckets_rest_query).getText(requestProperties: [sessionId: sessionId])
			def buckets_list = slurper.parseText(response.toString())
			def bucket_found = buckets_list.find {object -> object.name == bucket_name}

			writeToOutput("bucket " + bucket_name + " found? " + (bucket_found != null))

			// Create a bucket if needed -------------
			def bucket_id = null
			if (bucket_found)
				bucket_id = bucket_found.id
			else {
				// POST QUERY
				def create_bucket_query = this.CATALOG_URL + "/buckets?name=" + bucket_name + "&owner=" + this.BUCKET_OWNER
				def post = new org.apache.http.client.methods.HttpPost(create_bucket_query)
				post.addHeader("Accept", "application/json")
				post.addHeader("Content-Type", "application/json")
				post.addHeader("sessionId", sessionId)

				response = org.apache.http.impl.client.HttpClientBuilder.create().build().execute(post)
				def bis = new BufferedInputStream(response.getEntity().getContent())
				def result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
				bucket_id = slurper.parseText(result.toString()).get("id")
				bis.close();
				writeToOutput(bucket_name + " created!")
			}


			// FOR EACH PACKAGE ... /////////////////////////////


			// For each package targeting the current bucket
			examples_dir.listFiles(package_related_to_bucket_filter).each { package_dir ->

				writeToOutput("Loading package " + package_dir.absolutePath)

				// Parse the metadata json file
				def metadata_file = new File(package_dir, "METADATA.json")

				// From json to map
				def metadata_file_map = (Map) slurper.parseText(metadata_file.text)



				// DATASPACE SECTION /////////////////////////////


				def target_dir_path = ""
				def dataspace_map = null
				if ((dataspace_map = metadata_file_map.get("dataspace")) != null)
				{
					// Retrieve the targeted directory path
					def target = dataspace_map.get("target")
					if(target == "global")
						target_dir_path = this.GLOBAL_SPACE_PATH

					// Copy all files into the targeted directory
					dataspace_map.get("files").each { file_relative_path ->
						def file_src = new File(package_dir, file_relative_path)
						def file_src_path = file_src.absolutePath
						def file_name = file_src.getName()
						def file_dest = new File(target_dir_path, file_name)
						def file_dest_path = file_dest.absolutePath
						Files.copy(Paths.get(file_src_path), Paths.get(file_dest_path), StandardCopyOption.REPLACE_EXISTING)
						writeToOutput(file_src_path + " copied to " + file_dest_path)
					}
				}


				// FOR EACH OBJECT OF THE CATALOG ... /////////////////////////////


				def catalog_map = metadata_file_map.get("catalog")

				// GET QUERY
				def list_bucket_resources_rest_query = this.CATALOG_URL + "/buckets/" + bucket_id + "/resources"
				response =  new URL(list_bucket_resources_rest_query).getText(requestProperties: [sessionId: sessionId])
				def bucket_resources_list = slurper.parseText(response.toString())

				catalog_map.get("objects").each { object ->

					def metadata_map = object.get("metadata")


					// OBJECT SECTION /////////////////////////////


					// Does the object already exist in the catalog ? -------------
					def object_name = object.get("name")
					def object_found = bucket_resources_list.find {resource -> resource.name == object_name}
					writeToOutput(object_name + " found? " + (object_found != null))

					// Push the object to the bucket if it not exists
					def object_relative_path = object.get("file")
					def object_file = new File(package_dir, object_relative_path)
					def object_absolute_path = object_file.absolutePath
					if (!object_found)
					{
						// Retrieve object metadata
						def kind = metadata_map.get("kind")
						def commitMessageEncoded = java.net.URLEncoder.encode(metadata_map.get("commitMessage"), "UTF-8")
						def contentType = metadata_map.get("contentType")

						// POST QUERY
						def query_push_obj_query = this.CATALOG_URL + "/buckets/" + bucket_id + "/resources?name=" + object_name + "&kind=" + kind + "&commitMessage=" + commitMessageEncoded + "&contentType=" + contentType
						def post = new org.apache.http.client.methods.HttpPost(query_push_obj_query)
						post.addHeader("Accept", "application/json")
						post.addHeader("Content-Type", org.apache.http.entity.ContentType.MULTIPART_FORM_DATA.getMimeType()+";boundary="+boundary)
						post.addHeader("sessionId", sessionId)

						def builder = org.apache.http.entity.mime.MultipartEntityBuilder.create()
						builder.setBoundary(boundary);
						builder.setMode(org.apache.http.entity.mime.HttpMultipartMode.BROWSER_COMPATIBLE)
						builder.addPart("file", new org.apache.http.entity.mime.content.FileBody(object_file ))
						post.setEntity(builder.build())

						def result = org.apache.http.impl.client.HttpClientBuilder.create().build().execute(post)
						writeToOutput(object_file.getName() + " pushed!")
					}

					// Expose the workflow/object as a studio template
					def studio_template = object.get("studio_template")
					if (studio_template != null)
					{
						// Retrieve the studio template name
						def studio_template_name = studio_template.get("name")

						// Is the workflow already exposed as a studio template ? -------------
						def studio_template_found = isAStudioTemplate(studio_template_name, this.WORKFLOW_TEMPLATES_DIR_PATH)
						writeToOutput(studio_template_name + " found in studio templates ? " + studio_template_found)

						if (! studio_template_found)
						{
							// Create a new template dir in the targeted directory and copy the workflow into it
							def template_dir = new File(this.WORKFLOW_TEMPLATES_DIR_PATH, template_dir_name)
							template_dir.mkdir()
							writeToOutput(template_dir.absolutePath + " created!")

							// Copy the workflow into it
							def file_dest = new File(template_dir, "job.xml")
							def file_dest_path = file_dest.absolutePath
							Files.copy(Paths.get(object_absolute_path), Paths.get(file_dest_path))
							writeToOutput(file_dest_path + " created!")

							// Create a name file into it
							def name_file = new File(template_dir,"name")
							name_file.text = studio_template_name
							writeToOutput(name_file.absolutePath + " created!")

							// Create the metadata file into it
							def studio_metadata_file = new File(template_dir,"metadata")
							studio_metadata_file.text = studio_template.get("offsets_json_string")
							writeToOutput(studio_metadata_file.absolutePath + " created!")

							template_dir_name = (template_dir_name.toInteger() + 1) + ""
						}

					}

				}

			}

		}

		writeToOutput("Terminated.")
	}
}

try {
	new LoadExamples(this.binding).run()
} catch (Exception e) {
	println "Failed to load examples into the catalog."+ e.getMessage()
}

