import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile


class Main {



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


	def run( binding ) {

		def scriptFileName = "load-examples.groovy"
		println "[" + scriptFileName + "] Automatic deployment of proactive examples ..."

		// Bindings
		def global_space_path = binding.variables.get("pa.scheduler.dataspace.defaultglobal.localpath")
		def scheduler_rest_url = binding.variables.get("pa.scheduler.rest.url")
		def scheduler_home = binding.variables.get("pa.scheduler.home")

		// User variables
		def examples_zip_path = new File(scheduler_home, "samples/proactive-examples.zip").absolutePath
		def workflow_templates_dir = new File(scheduler_home, "config/workflows/templates")
		def workflow_templates_dir_path = workflow_templates_dir.absolutePath
		def bucket_owner = "GROUP:public-objects"

		// Deduced variables
		def catalog_url = scheduler_rest_url.substring(0,scheduler_rest_url.length()-4) + "catalog"
		def example_dir_path = examples_zip_path.substring(0,examples_zip_path.lastIndexOf("."))

		println "[" + scriptFileName + "] Variables : "
		println "[" + scriptFileName + "] examples_zip_path " + examples_zip_path
		println "[" + scriptFileName + "] example_dir_path " + example_dir_path
		println "[" + scriptFileName + "] global_space_path " + global_space_path
		println "[" + scriptFileName + "] catalog_url " + catalog_url
		println "[" + scriptFileName + "] workflow_templates_dir_path " + workflow_templates_dir_path
		println "[" + scriptFileName + "] bucket_owner " + bucket_owner

		println "[" + scriptFileName + "] Actions : "

		// If the unzipped dir already exists, stop the script execution
		def example_dir = new File(example_dir_path)
		if (example_dir.exists())
		{
			println "[" + scriptFileName + "] " + example_dir_path + " already exists, delete it to redeploy examples."
			println "[" + scriptFileName + "] Terminated."
			return
		}

		// Unzip the examples
		def examples_zip = new File(examples_zip_path)
		if (!examples_zip.exists())
		{
			println "[" + scriptFileName + "] " + examples_zip_path + " not found!"
			return

		}
		unzipFile(examples_zip, example_dir_path)
		println "[" + scriptFileName + "] " + examples_zip_path + " extracted!"

		def target_dir_path = ""
		def bucket = ""

		// Start by finding the next template dir index
		def template_dir_name = "1"
		// If the wkw template dir does not exist, let's create it
		if (!workflow_templates_dir.exists())
			workflow_templates_dir.mkdirs()
		// If it exists, let's iterate over existing templates
		else
		{
			def templates_dirs_list = []
			new File(workflow_templates_dir_path).eachDir { dir ->
				templates_dirs_list << dir.getName().toInteger()
			}
			if (!templates_dirs_list.isEmpty())
				template_dir_name = (templates_dirs_list.sort().last() + 1) + ""
		}
		println "[" + scriptFileName + "] Next template dir name " + template_dir_name

		// For POST queries
		Main.class.getClass().getResource(new File(scheduler_home, "dist/lib/httpclient-4.5.2.jar").absolutePath);
		def boundary = "---------------" + UUID.randomUUID().toString();

		// For each example directory
		example_dir.eachDir() { dir ->

			def metadata_file = new File(dir.absolutePath, "METADATA.json")
			if (metadata_file.exists())
			{
				println "[" + scriptFileName + "] Parsing " + metadata_file.absolutePath

				// From json to map
				def slurper = new groovy.json.JsonSlurper()
				def metadata_file_map = (Map) slurper.parseText(metadata_file.text)

				def catalog_map = metadata_file_map.get("catalog")


				// DATASPACE SECTION /////////////////////////////


				def dataspace_map = null
				if ((dataspace_map = catalog_map.get("dataspace")) != null)
				{
					// Retrieve the targeted directory path
					def target = dataspace_map.get("target")
					if(target == "global")
						target_dir_path = global_space_path

					// Copy all files into the targeted directory
					dataspace_map.get("files").each { file_relative_path ->
						def file_src = new File(dir.absolutePath, file_relative_path)
						def file_src_path = file_src.absolutePath
						def file_name = file_src.getName()
						def file_dest = new File(target_dir_path, file_name)
						def file_dest_path = file_dest.absolutePath
						Files.copy(Paths.get(file_src_path), Paths.get(file_dest_path), StandardCopyOption.REPLACE_EXISTING)
					}
				}


				// BUCKET SECTION /////////////////////////////


				bucket = catalog_map.get("bucket")

				// Does the bucket already exist? -------------
				// GET QUERY
				def list_buckets_rest_query = catalog_url + "/buckets?owner=" + bucket_owner
				def response =  new URL(list_buckets_rest_query).text
				def buckets_list = slurper.parseText(response.toString())
				def bucket_found = buckets_list.find {object -> object.name == bucket}

				println "[" + scriptFileName + "] bucket " + bucket + " found? " + (bucket_found != null)

				// Create a bucket if needed -------------
				def bucket_id = null
				if (bucket_found)
					bucket_id = bucket_found.id
				else {
					// POST QUERY
					def create_bucket_query = catalog_url + "/buckets?name=" + bucket + "&owner=" + bucket_owner
					def post = new org.apache.http.client.methods.HttpPost(create_bucket_query)
					post.addHeader("Accept", "application/json");
					post.addHeader("Content-Type", "application/json");

					response = org.apache.http.impl.client.HttpClientBuilder.create().build().execute(post)
					def bis = new BufferedInputStream(response.getEntity().getContent())
					def result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
					bucket_id = slurper.parseText(result.toString()).get("id")
					bis.close();
					println "[" + scriptFileName + "] " + bucket + " created!"
				}


				// OBJECTS SECTION /////////////////////////////


				// GET QUERY
				def list_bucket_resources_rest_query = catalog_url + "/buckets/" + bucket_id + "/resources"
				response =  new URL(list_bucket_resources_rest_query).text
				def bucket_resources_list = slurper.parseText(response.toString())

				catalog_map.get("objects").each { object ->

					def metadata_map = object.get("metadata")


					// OBJECT SECTION /////////////////////////////


					// Does the object already exist in the catalog ? -------------
					def object_name = object.get("name")
					def object_found = bucket_resources_list.find {resource -> resource.name == object_name}
					println "[" + scriptFileName + "] " + object_name + " found? " + (object_found != null)

					// Push the object to the bucket if it not exists
					def object_relative_path = object.get("file")
					def object_file = new File(dir.absolutePath, object_relative_path)
					def object_absolute_path = object_file.absolutePath
					if (!object_found)
					{
						// Retrieve object metadata
						def kind = metadata_map.get("kind")
						def commitMessageEncoded = java.net.URLEncoder.encode(metadata_map.get("commitMessage"), "UTF-8")
						def contentType = metadata_map.get("contentType")

						// POST QUERY
						def query_push_obj_query = catalog_url + "/buckets/" + bucket_id + "/resources?name=" + object_name + "&kind=" + kind + "&commitMessage=" + commitMessageEncoded + "&contentType=" + contentType
						def post = new org.apache.http.client.methods.HttpPost(query_push_obj_query)
						post.addHeader("Accept", "application/json");
						post.addHeader("Content-Type", org.apache.http.entity.ContentType.MULTIPART_FORM_DATA.getMimeType()+";boundary="+boundary);

						def builder = org.apache.http.entity.mime.MultipartEntityBuilder.create()
						builder.setBoundary(boundary);
						builder.setMode(org.apache.http.entity.mime.HttpMultipartMode.BROWSER_COMPATIBLE)
						builder.addPart("file", new org.apache.http.entity.mime.content.FileBody(object_file ))
						post.setEntity(builder.build())

						def result = org.apache.http.impl.client.HttpClientBuilder.create().build().execute(post)
						println "[" + scriptFileName + "] " + object_file.getName() + " pushed!"
					}

					// Expose the workflow/object as a studio template
					def studio_template = object.get("studio_template")
					if (studio_template != null)
					{
						// Retrieve the studio template name
						def studio_template_name = studio_template.get("name")

						// Is the workflow already exposed as a studio template ? -------------
						def studio_template_found = isAStudioTemplate(studio_template_name, workflow_templates_dir_path)
						println "[" + scriptFileName + "] " + studio_template_name + " found in studio templates ? " + studio_template_found

						if (! studio_template_found)
						{
							// Create a new template dir in the targeted directory and copy the workflow into it
							def template_dir = new File(workflow_templates_dir_path, template_dir_name)
							template_dir.mkdir()
							println "[" + scriptFileName + "] " + template_dir.absolutePath + " created!"

							// Copy the workflow into it
							def file_dest = new File(template_dir, "job.xml")
							def file_dest_path = file_dest.absolutePath
							Files.copy(Paths.get(object_absolute_path), Paths.get(file_dest_path))
							println "[" + scriptFileName + "] " + file_dest_path + " created!"

							// Create a name file into it
							def name_file = new File(template_dir,"name")
							name_file.text = studio_template_name
							println "[" + scriptFileName + "] " + name_file.absolutePath + " created!"

							// Create the metadata file into it
							def studio_metadata_file = new File(template_dir,"metadata")
							studio_metadata_file.text = studio_template.get("offsets_json_string")
							println "[" + scriptFileName + "] " + studio_metadata_file.absolutePath + " created!"

							template_dir_name = (template_dir_name.toInteger() + 1) + ""
						}

					}

				}

			}
		}

		println "[" + scriptFileName + "] ... proactive examples deployed!"
		println "[" + scriptFileName + "] Terminated."
	}
}

new Main().run(this.binding)
	
