import org.ow2.proactive.authentication.ConnectionInfo
import org.ow2.proactive.scheduler.rest.SchedulerClient
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.apache.log4j.Logger
import org.apache.commons.io.FilenameUtils
import org.apache.http.conn.ssl.*
import org.apache.http.impl.client.*
import javax.net.ssl.*

import java.util.zip.ZipFile


class LoadPackage {

    private final String PATH_TO_SCHEDULER_CREDENTIALS_FILE = "config/authentication/admin_user.cred"
    private final String LOAD_PACKAGE_SCRIPT_NAME = "LoadPackage.groovy"
    private final String BUCKET_OWNER = "GROUP:public-objects"
    private final String TMP_DIR_PREFIX = "package_temp_dir"

    private final String GLOBAL_SPACE_PATH
    private final String SCHEDULER_REST_URL
    private final String SCHEDULER_HOME
    private final String SCHEDULER_VERSION
    private final String CATALOG_URL
    private String sessionId


    private logger = Logger.getLogger("org.ow2.proactive.scheduler")

    private static groovy.json.JsonSlurper slurper = new groovy.json.JsonSlurper()


    LoadPackage(binding) {

        // Bindings
        this.GLOBAL_SPACE_PATH = binding.variables.get("pa.scheduler.dataspace.defaultglobal.localpath")
        this.SCHEDULER_REST_URL = binding.variables.get("pa.scheduler.rest.url")
        this.SCHEDULER_HOME = binding.variables.get("pa.scheduler.home")
        this.SCHEDULER_VERSION = org.ow2.proactive.utils.Version.PA_VERSION
        this.sessionId = binding.variables.get("pa.scheduler.session.id")

        // Deduced variables
        this.CATALOG_URL = this.SCHEDULER_REST_URL.substring(0, this.SCHEDULER_REST_URL.length() - 4) + "catalog"

        writeToOutput("SCHEDULER_HOME " + this.SCHEDULER_HOME)
        writeToOutput("SCHEDULER_REST_URL " + this.SCHEDULER_REST_URL)
        writeToOutput("CATALOG_URL " + this.CATALOG_URL)
    }


    void writeToOutput(output) {
        logger.info("[" + this.LOAD_PACKAGE_SCRIPT_NAME + "] " + output)
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


    void loginAdminUserCredToSchedulerAndGetSessionId() {

        if (this.sessionId == null) {
            def schedulerClient = SchedulerClient.createInstance()
            def schedulerConnectionSettings = new ConnectionInfo(this.SCHEDULER_REST_URL, null, null, new File(this.SCHEDULER_HOME, this.PATH_TO_SCHEDULER_CREDENTIALS_FILE), true)
            schedulerClient.init(schedulerConnectionSettings)

            this.sessionId = schedulerClient.getSession()
            writeToOutput("sessionId " + this.sessionId)
        }
    }

    def unzipPackage(package_dir) {
      // Create a temporary dir
      def package_temp_dir = Files.createTempDirectory(TMP_DIR_PREFIX).toFile()
      package_temp_dir.deleteOnExit()
      // Unzip the package into it
      unzipFile(package_dir, package_temp_dir.getPath())
      writeToOutput(" " + package_dir + " extracted!")
      // Return the unzipped package
      def package_dir_name_no_ext = FilenameUtils.removeExtension(package_dir.name)
      return new File(package_temp_dir, package_dir_name_no_ext)
    }


    void populateDataspace(dataspace_map, package_dir) {

        // Do nothing if there is nothing to copy into the dataspaces
        if (dataspace_map == null)
            return

        // Retrieve the targeted directory path
        def target_dir_path = ""
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
            writeToOutput(file_src_path + " copied to " + file_dest_path + "!")
        }
    }


    def getHttpClientBuilder() {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return HttpClients.custom().setSSLSocketFactory(
                sslsf);
    }


    def createAndExecuteQueryWithFileAttachment(query_push_obj_query, boundary, File object_file) {
        def post = new org.apache.http.client.methods.HttpPost(query_push_obj_query)
        post.addHeader("Accept", "application/json")
        post.addHeader("Content-Type", org.apache.http.entity.ContentType.MULTIPART_FORM_DATA.getMimeType() + ";boundary=" + boundary)
        post.addHeader("sessionId", this.sessionId)

        def builder = org.apache.http.entity.mime.MultipartEntityBuilder.create()
        builder.setBoundary(boundary);
        builder.setMode(org.apache.http.entity.mime.HttpMultipartMode.BROWSER_COMPATIBLE)
        builder.addPart("file", new org.apache.http.entity.mime.content.FileBody(object_file))
        post.setEntity(builder.build())

        return getHttpClientBuilder().build().execute(post)
    }

    def pushObject(object, package_dir, bucket_resources_list, bucket_name) {
        def metadata_map = object.get("metadata")

        // OBJECT SECTION /////////////////////////////

        // Does the object already exist in the catalog ? -------------
        def object_name = object.get("name")
        def object_found = bucket_resources_list.find { resource -> resource.name == object_name }
        writeToOutput(" " + object_name + " found? " + (object_found != null))

        // Push the object to the bucket if it not exists
        def object_relative_path = object.get("file")
        def object_file = new File(package_dir.absolutePath, object_relative_path)
        if (!object_found) {
            // Retrieve object metadata
            def kind = metadata_map.get("kind")
            def commitMessageEncoded = java.net.URLEncoder.encode(metadata_map.get("commitMessage") + " (" + SCHEDULER_VERSION + ")", "UTF-8")
            def contentType = metadata_map.get("contentType")

            // For POST queries
            this.class.getClass().getResource(new File(this.SCHEDULER_HOME, "dist/lib/httpclient-4.5.2.jar").absolutePath);
            def boundary = "---------------" + UUID.randomUUID().toString();

            // POST QUERY
            def query_push_obj_query = this.CATALOG_URL + "/buckets/" + bucket_name + "/resources?name=" + object_name + "&kind=" + kind + "&commitMessage=" + commitMessageEncoded + "&objectContentType=" + contentType
            createAndExecuteQueryWithFileAttachment(query_push_obj_query, boundary, object_file)
            writeToOutput(" " + object_file.getName() + " created!")
        } else {
            // Retrieve object metadata
            def commitMessageEncoded = java.net.URLEncoder.encode(metadata_map.get("commitMessage") + " (" + SCHEDULER_VERSION + ")", "UTF-8")

            // For POST queries
            this.class.getClass().getResource(new File(this.SCHEDULER_HOME, "dist/lib/httpclient-4.5.2.jar").absolutePath);
            def boundary = "---------------" + UUID.randomUUID().toString();

            // POST QUERY
            def query_push_obj_query = this.CATALOG_URL + "/buckets/" + bucket_name + "/resources/"+ object_name + "/revisions?commitMessage=" + commitMessageEncoded
            createAndExecuteQueryWithFileAttachment(query_push_obj_query, boundary, object_file)
            writeToOutput(" " + object_file.getName() + " updated!")
        }
        return object_file
    }



    def createBucketIfNotExist(bucket){

        // Does the bucket already exist? -------------
        // GET QUERY
        def list_buckets_rest_query = this.CATALOG_URL + "/buckets"
        def get = new org.apache.http.client.methods.HttpGet(list_buckets_rest_query)
        get.addHeader("sessionid", this.sessionId)
        def response = getHttpClientBuilder().build().execute(get)
        def bis = new BufferedInputStream(response.getEntity().getContent())
        def result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
        bis.close()
        def buckets = slurper.parseText(result)
        def bucket_found = buckets.find { object -> object.name == bucket }

        writeToOutput(" bucket " + bucket + " found? " + (bucket_found != null))

        // Create a bucket if needed -------------
        if (bucket_found) {
            return bucket_found.name
        } else {
            // POST QUERY
            def create_bucket_query = this.CATALOG_URL + "/buckets?name=" + bucket + "&owner=" + this.BUCKET_OWNER
            def post = new org.apache.http.client.methods.HttpPost(create_bucket_query)
            post.addHeader("Accept", "application/json")
            post.addHeader("Content-Type", "application/json")
            post.addHeader("sessionId", this.sessionId)

            response = getHttpClientBuilder().build().execute(post)
            bis = new BufferedInputStream(response.getEntity().getContent())
            result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
            def bucket_name = slurper.parseText(result.toString()).get("name")
            bis.close();
            writeToOutput(" " + bucket + " created!")
            return bucket_name
        }
    }


    void populateBucket(catalog_map, package_dir) {

        // Do nothing if there is no workflow to push
        if (catalog_map == null)
            return

        def buckets_list
        //Transform a String to a List of String
        if (catalog_map.get("bucket") instanceof String) {
            buckets_list = [catalog_map.get("bucket")]

        } else {
            buckets_list = catalog_map.get("bucket")
        }
        buckets_list.each { bucket ->

            // BUCKET SECTION /////////////////////////////

            def bucket_name = createBucketIfNotExist(bucket)

            // OBJECTS SECTION /////////////////////////////

            // GET QUERY
            def list_bucket_resources_rest_query = this.CATALOG_URL + "/buckets/" + bucket_name + "/resources"
            def get = new org.apache.http.client.methods.HttpGet(list_bucket_resources_rest_query)
            get.addHeader("sessionid", this.sessionId)
            def response = getHttpClientBuilder().build().execute(get)
            def bis = new BufferedInputStream(response.getEntity().getContent())
            def result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
            bis.close()
            def bucket_resources_list = slurper.parseText(result)

            catalog_map.get("objects").each { object ->
                //push object in the catalog
                def object_file = pushObject(object, package_dir,bucket_resources_list, bucket_name)
            }
        }
    }


    void run(package_dir, load_dependencies) {

        // Connect to the scheduler
        loginAdminUserCredToSchedulerAndGetSessionId()

        // If the package dir is a zip file, create a temporary directory that contains the unzipped package dir
        writeToOutput(" Loading " + package_dir)
        def package_dir_ext = FilenameUtils.getExtension(package_dir.name)
        def unzipped_package_dir
        if (package_dir_ext == "zip"){
            unzipped_package_dir = unzipPackage(package_dir)
            package_dir_ext = "." + package_dir_ext
        }
        else if (package_dir_ext.isEmpty())
            unzipped_package_dir = package_dir
        else {
            writeToOutput(" package dir extension not supported")
            throw new Exception(" package dir extension not supported")
        }

        // Parse the metadata json file
        def metadata_file = new File(unzipped_package_dir, "METADATA.json")
        writeToOutput(" Parsing " + metadata_file.absolutePath)
        def metadata_file_map = (Map) slurper.parseText(metadata_file.text)

        // LOAD PACKAGE DEPENDENCIES RECURSIVELY ////////////////////////////

        if (load_dependencies) {
            def dependencies_list = metadata_file_map.get("dependencies")

            if (dependencies_list != null)
            {
                def parent_package_dir = package_dir.getAbsoluteFile().getParentFile()
                dependencies_list.each { package_name ->

                    this.run( new File(parent_package_dir, package_name + package_dir_ext), load_dependencies)
                }
            }
        }

        // POPULATE DATASPACE ////////////////////////////

        def dataspace_map = metadata_file_map.get("dataspace")
        populateDataspace(dataspace_map, unzipped_package_dir)

        // POPULATE BUCKETS /////////////////////////////

        def catalog_map = metadata_file_map.get("catalog")
        populateBucket(catalog_map, unzipped_package_dir)

    }
}