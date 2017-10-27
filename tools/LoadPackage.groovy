import org.ow2.proactive.authentication.ConnectionInfo
import org.ow2.proactive.scheduler.rest.SchedulerClient
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.apache.log4j.Logger

import java.util.zip.ZipFile


class LoadPackage {

    private final String PATH_TO_SCHEDULER_CREDENTIALS_FILE = "config/authentication/scheduler.cred"

    private final String LOAD_PACKAGE_SCRIPT_NAME = "LoadPackage.groovy"

    private final String BUCKET_OWNER = "GROUP:public-objects"

    private final String GLOBAL_SPACE_PATH
    private final String SCHEDULER_REST_URL
    private final String SCHEDULER_HOME
    private final File WORKFLOW_TEMPLATES_DIR
    private final String WORKFLOW_TEMPLATES_DIR_PATH
    private final String CATALOG_URL



    private logger = Logger.getLogger("org.ow2.proactive.scheduler")


    LoadPackage(binding) {

        // Bindings
        this.GLOBAL_SPACE_PATH = binding.variables.get("pa.scheduler.dataspace.defaultglobal.localpath")
        this.SCHEDULER_REST_URL = binding.variables.get("pa.scheduler.rest.url")
        this.SCHEDULER_HOME = binding.variables.get("pa.scheduler.home")

        // User variables
        this.WORKFLOW_TEMPLATES_DIR = new File(this.SCHEDULER_HOME, "config/workflows/templates")

        this.WORKFLOW_TEMPLATES_DIR_PATH = this.WORKFLOW_TEMPLATES_DIR.absolutePath


        // Deduced variables
        this.CATALOG_URL = this.SCHEDULER_REST_URL.substring(0, this.SCHEDULER_REST_URL.length() - 4) + "catalog"

    }


    def writeToOutput(String output) {
        logger.info("[" + this.LOAD_PACKAGE_SCRIPT_NAME + "] " + output)
    }


    def isAStudioTemplate(workflow_name, templates_dir_path) {
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
    def unzipFile(src, dest) {
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


    def loginAdminUserCredToSchedulerAndGetSessionId() {
        writeToOutput("Scheduler home: " + this.SCHEDULER_HOME)
        writeToOutput("Scheduler rest: " + this.SCHEDULER_REST_URL)
        writeToOutput("CATALOG_URL " +this.CATALOG_URL)
        def schedulerClient = SchedulerClient.createInstance()
        def schedulerConnectionSettings = new ConnectionInfo(this.SCHEDULER_REST_URL, null, null, new File(this.SCHEDULER_HOME, this.PATH_TO_SCHEDULER_CREDENTIALS_FILE), false)
        schedulerClient.init(schedulerConnectionSettings)

        String sessionID = schedulerClient.getSession()
        writeToOutput("SessionId: " + sessionID)
        return sessionID
    }


    def run(File package_dir) {
        def target_dir_path = ""
        def bucket = ""
        String sessionId = loginAdminUserCredToSchedulerAndGetSessionId()
        //If the package dir is a zip file, create a temporary directory that contains the unzipped package dir
        if (package_dir.getPath().endsWith(".zip")) {
            if (!package_dir.exists()) {
                writeToOutput("] " + packager_dir + " not found!")
                return
            } else {
                def package_temp_dir = Files.createTempDirectory("package_temp_dir").toFile()
                unzipFile(package_dir, package_temp_dir.getPath())
                writeToOutput(" " + package_dir + " extracted!")
                package_dir = new File(package_temp_dir.getPath()+"/"+package_dir.getName().substring(0, package_dir.getName().length() - 4))
                package_temp_dir.deleteOnExit()

            }
        }
        def metadata_file = new File(package_dir.absolutePath, "METADATA.json")
        writeToOutput(" Parsing " + metadata_file.absolutePath)
        // From json to map
        def slurper = new groovy.json.JsonSlurper()
        def metadata_file_map = (Map) slurper.parseText(metadata_file.text)
        def catalog_map = metadata_file_map.get("catalog")

        // DATASPACE SECTION /////////////////////////////

        def dataspace_map = null
        if ((dataspace_map = catalog_map.get("dataspace")) != null) {
            // Retrieve the targeted directory path
            def target = dataspace_map.get("target")
            if (target == "global")
                target_dir_path = this.GLOBAL_SPACE_PATH

            // Copy all files into the targeted directory
            dataspace_map.get("files").each { file_relative_path ->
                def file_src = new File(package_dir.absolutePath, file_relative_path)
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
        def list_buckets_rest_query = this.CATALOG_URL + "/buckets?owner=" + this.BUCKET_OWNER
        def response = new URL(list_buckets_rest_query).getText(requestProperties: [sessionId: sessionId])
        def buckets_list = slurper.parseText(response.toString())
        def bucket_found = buckets_list.find { object -> object.name == bucket }

        writeToOutput(" bucket " + bucket + " found? " + (bucket_found != null))

        // Create a bucket if needed -------------
        def bucket_id = null
        if (bucket_found) {
            bucket_id = bucket_found.id
        }
        else {
            // POST QUERY
            def create_bucket_query = this.CATALOG_URL + "/buckets?name=" + bucket + "&owner=" + this.BUCKET_OWNER
            def post = new org.apache.http.client.methods.HttpPost(create_bucket_query)
            post.addHeader("Accept", "application/json")
            post.addHeader("Content-Type", "application/json")
            post.addHeader("sessionId", sessionId)

            response = org.apache.http.impl.client.HttpClientBuilder.create().build().execute(post)
            def bis = new BufferedInputStream(response.getEntity().getContent())
            def result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
            bucket_id = slurper.parseText(result.toString()).get("id")
            bis.close();
            writeToOutput(" " + bucket + " created!")
        }

        // OBJECTS SECTION /////////////////////////////

        // GET QUERY
        def list_bucket_resources_rest_query = this.CATALOG_URL + "/buckets/" + bucket_id + "/resources"
        response = new URL(list_bucket_resources_rest_query).getText(requestProperties: [sessionId: sessionId])
        def bucket_resources_list = slurper.parseText(response.toString())

        catalog_map.get("objects").each { object ->

            def metadata_map = object.get("metadata")

            // OBJECT SECTION /////////////////////////////

            // Does the object already exist in the catalog ? -------------
            def object_name = object.get("name")
            def object_found = bucket_resources_list.find { resource -> resource.name == object_name }
            writeToOutput(" " + object_name + " found? " + (object_found != null))

            // Push the object to the bucket if it not exists
            def object_relative_path = object.get("file")
            def object_file = new File(package_dir.absolutePath, object_relative_path)
            def object_absolute_path = object_file.absolutePath
            if (!object_found) {
                // Retrieve object metadata
                def kind = metadata_map.get("kind")
                def commitMessageEncoded = java.net.URLEncoder.encode(metadata_map.get("commitMessage"), "UTF-8")
                def contentType = metadata_map.get("contentType")

                // For POST queries
                this.class.getClass().getResource(new File(this.SCHEDULER_HOME, "dist/lib/httpclient-4.5.2.jar").absolutePath);
                def boundary = "---------------" + UUID.randomUUID().toString();

                // POST QUERY
                def query_push_obj_query = this.CATALOG_URL + "/buckets/" + bucket_id + "/resources?name=" + object_name + "&kind=" + kind + "&commitMessage=" + commitMessageEncoded + "&contentType=" + contentType
                def post = new org.apache.http.client.methods.HttpPost(query_push_obj_query)
                post.addHeader("Accept", "application/json")
                post.addHeader("Content-Type", org.apache.http.entity.ContentType.MULTIPART_FORM_DATA.getMimeType() + ";boundary=" + boundary)
                post.addHeader("sessionId", sessionId)

                def builder = org.apache.http.entity.mime.MultipartEntityBuilder.create()
                builder.setBoundary(boundary);
                builder.setMode(org.apache.http.entity.mime.HttpMultipartMode.BROWSER_COMPATIBLE)
                builder.addPart("file", new org.apache.http.entity.mime.content.FileBody(object_file))
                post.setEntity(builder.build())

                def result = org.apache.http.impl.client.HttpClientBuilder.create().build().execute(post)
                writeToOutput(" " + object_file.getName() + " pushed!")
            }

            // Expose the workflow/object as a studio template

            // Start by finding the next template dir index
            def template_dir_name = "1"
            // If the wkw template dir does not exist, let's create it
            if (!this.WORKFLOW_TEMPLATES_DIR.exists()) {
                this.WORKFLOW_TEMPLATES_DIR.mkdirs()
            }
            // If it exists, let's iterate over existing templates
            else {
                def templates_dirs_list = []
                new File(this.WORKFLOW_TEMPLATES_DIR_PATH).eachDir { dir ->
                    templates_dirs_list << dir.getName().toInteger()
                }
                if (!templates_dirs_list.isEmpty())
                    template_dir_name = (templates_dirs_list.sort().last() + 1) + ""
            }
            writeToOutput(" Next template dir name " + template_dir_name)

            def studio_template = object.get("studio_template")
            if (studio_template != null) {
                // Retrieve the studio template name
                def studio_template_name = studio_template.get("name")

                // Is the workflow already exposed as a studio template ? -------------
                def studio_template_found = isAStudioTemplate(studio_template_name, this.WORKFLOW_TEMPLATES_DIR_PATH)
                writeToOutput(" " + studio_template_name + " found in studio templates ? " + studio_template_found)

                if (!studio_template_found) {
                    // Create a new template dir in the targeted directory and copy the workflow into it
                    def template_dir = new File(this.WORKFLOW_TEMPLATES_DIR_PATH, template_dir_name)
                    template_dir.mkdir()
                    writeToOutput("] " + template_dir.absolutePath + " created!")

                    // Copy the workflow into it
                    def file_dest = new File(template_dir, "job.xml")
                    def file_dest_path = file_dest.absolutePath
                    Files.copy(Paths.get(object_absolute_path), Paths.get(file_dest_path))
                    writeToOutput(" " + file_dest_path + " created!")

                    // Create a name file into it
                    def name_file = new File(template_dir, "name")
                    name_file.text = studio_template_name
                    writeToOutput(" " + name_file.absolutePath + " created!")

                    // Create the metadata file into it
                    def studio_metadata_file = new File(template_dir, "metadata")
                    studio_metadata_file.text = studio_template.get("offsets_json_string")
                    writeToOutput(" " + studio_metadata_file.absolutePath + " created!")

                    template_dir_name = (template_dir_name.toInteger() + 1) + ""
                }

            }

        }
    }
}

