import org.ow2.proactive.authentication.ConnectionInfo
import org.ow2.proactive.scheduler.rest.SchedulerClient
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.apache.log4j.Logger
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.FileUtils
import org.apache.http.conn.ssl.*
import org.apache.http.impl.client.*
import javax.net.ssl.*
import java.util.zip.ZipFile



class LoadPackage {

    private final String PATH_TO_SCHEDULER_CREDENTIALS_FILE = "config/authentication/admin_user.cred"
    private final String LOAD_PACKAGE_SCRIPT_NAME = "LoadPackage.groovy"
    private String BUCKET_OWNER = "GROUP:public-objects"
    private final String TMP_DIR_PREFIX = "packageTempDir"

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

    def unzipPackage(packageDir) {
      // Create a temporary dir
      def packageTempDir = Files.createTempDirectory(TMP_DIR_PREFIX).toFile()
      packageTempDir.deleteOnExit()
      // Unzip the package into it
      unzipFile(packageDir, packageTempDir.getPath())
      writeToOutput(" " + packageDir + " extracted!")
      // Return the unzipped package
      def packageDirNameNoExt = FilenameUtils.removeExtension(packageDir.name)
      return new File(packageTempDir, packageDirNameNoExt)
    }


    void populateDataspace(packageDataspaceMap, packageDir) {

        // Do nothing if there is nothing to copy into the dataspaces
        if (packageDataspaceMap == null)
            return

        // Retrieve the targeted directory path
        def targetDirPath = ""
        def target = packageDataspaceMap.get("target")
        if (target == "global")
            targetDirPath = this.GLOBAL_SPACE_PATH

        // Copy all files into the targeted directory
        packageDataspaceMap.get("files").each { fileRelativePath ->
            def fileSrc = new File(packageDir.absolutePath, fileRelativePath)
            def fileSrcPath = fileSrc.absolutePath
            def fileDest = fileRelativePath.replace("resources/dataspace/", "")
            def fileDestPath = Paths.get(targetDirPath, fileDest).toString()
            // Create nonexistent parent directories
            FileUtils.forceMkdirParent(new File(fileDestPath))
            Files.copy(Paths.get(fileSrcPath), Paths.get(fileDestPath), StandardCopyOption.REPLACE_EXISTING)
            writeToOutput(fileSrcPath + " copied to " + fileDestPath + "!")
        }
    }


    def getHttpClientBuilder() {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return HttpClients.custom().setSSLSocketFactory(sslsf);
    }


    def createAndExecuteQueryWithFileAttachment(pushObjectQuery, boundary, File objectFile) {
        def post = new org.apache.http.client.methods.HttpPost(pushObjectQuery)
        post.addHeader("Accept", "application/json")
        post.addHeader("Content-Type", org.apache.http.entity.ContentType.MULTIPART_FORM_DATA.getMimeType() + ";boundary=" + boundary)
        post.addHeader("sessionId", this.sessionId)

        def builder = org.apache.http.entity.mime.MultipartEntityBuilder.create()
        builder.setBoundary(boundary);
        builder.setMode(org.apache.http.entity.mime.HttpMultipartMode.BROWSER_COMPATIBLE)
        builder.addPart("file", new org.apache.http.entity.mime.content.FileBody(objectFile))
        post.setEntity(builder.build())

        return getHttpClientBuilder().build().execute(post)
    }

    def pushObject(object, packageDir, catalogBucketResourcesList, bucketName) {
        def packageMetadataMap = object.get("metadata")

        // Does the object already exist in the catalog ? -------------
        def objectName = java.net.URLEncoder.encode(object.get("name"), "UTF-8")
        def objectFound = catalogBucketResourcesList.find { resource -> resource.name == objectName }
        writeToOutput(" " + objectName + " found? " + (objectFound != null))

        // Create the object file
        def objectRelativePath = object.get("file")
        def objectFile = new File(packageDir.absolutePath, objectRelativePath)

        // Commit message
        def commitMessageStarting = packageMetadataMap.get("commitMessage")
        def commitMessageEnding  = " (" + SCHEDULER_VERSION + ")"

        // If the object does not exist in the catalog bucket
        if (!objectFound) {

            // Retrieve object metadata
            def kind = packageMetadataMap.get("kind")
            def commitMessageEncoded = java.net.URLEncoder.encode(commitMessageStarting + commitMessageEnding, "UTF-8")
            def contentType = packageMetadataMap.get("contentType")
            def projectName = packageMetadataMap.get("projectName")
            def tags = packageMetadataMap.get("tags")

            // For POST queries
            this.class.getClass().getResource(new File(this.SCHEDULER_HOME, "dist/lib/httpclient-4.5.14.jar").absolutePath);
            def boundary = "---------------" + UUID.randomUUID().toString();

            // Create part of the POST query
            def pushObjectQuery = this.CATALOG_URL + "/buckets/" + bucketName + "/resources?name=" + objectName + "&kind=" + kind + "&commitMessage=" + commitMessageEncoded + "&objectContentType=" + contentType
            if(projectName != null) {
                pushObjectQuery = pushObjectQuery + "&projectName=" +  java.net.URLEncoder.encode(projectName, "UTF-8")
            }
            if(tags != null) {
                pushObjectQuery = pushObjectQuery + "&tags=" +  java.net.URLEncoder.encode(tags, "UTF-8")
            }

            // Create the POST query and execute it
            createAndExecuteQueryWithFileAttachment(pushObjectQuery, boundary, objectFile)
            writeToOutput(" " + objectFile.getName() + " created!")

            // If the object exists in the catalog and its last commit is not a user commit
        } else if (objectFound.commit_message.startsWith(commitMessageStarting)){

            // Retrieve object metadata
            def commitMessageEncoded = java.net.URLEncoder.encode(commitMessageStarting + commitMessageEnding, "UTF-8")
            def projectName = packageMetadataMap.get("projectName")

            // Create part of the POST query
            this.class.getClass().getResource(new File(this.SCHEDULER_HOME, "dist/lib/httpclient-4.5.14.jar").absolutePath);
            def boundary = "---------------" + UUID.randomUUID().toString();
            def pushObjectQuery = this.CATALOG_URL + "/buckets/" + bucketName + "/resources/"+ objectName + "/revisions?commitMessage=" + commitMessageEncoded
            if(projectName != null) {
                pushObjectQuery = pushObjectQuery + "&projectName=" +  java.net.URLEncoder.encode(projectName, "UTF-8")
            }

            // Create the POST query and execute it
            createAndExecuteQueryWithFileAttachment(pushObjectQuery, boundary, objectFile)
            writeToOutput(" " + objectFile.getName() + " updated!")
        }
    }



    def createBucketIfNotExist(bucket){

        // Does the bucket already exist? -------------
        // GET QUERY
        def listBucketsQuery = this.CATALOG_URL + "/buckets"
        def get = new org.apache.http.client.methods.HttpGet(listBucketsQuery)
        get.addHeader("sessionid", this.sessionId)
        def response = getHttpClientBuilder().build().execute(get)
        def bis = new BufferedInputStream(response.getEntity().getContent())
        def result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
        bis.close()
        def responseStatusLine = response.getStatusLine()
        if (responseStatusLine.getStatusCode() >= 400) {
            writeToOutput("Result of " + listBucketsQuery + ":")
            writeToOutput(result)
            throw new IllegalStateException("Invalid response status from " + listBucketsQuery + ": " + responseStatusLine)
        }
        def buckets = slurper.parseText(result)
        def bucketFound = buckets.find { object -> object.name == bucket }

        writeToOutput(" bucket " + bucket + " found? " + (bucketFound != null))

        // Create a bucket if needed -------------
        if (bucketFound) {
            if(!this.BUCKET_OWNER.equals("GROUP:public-objects")){
                writeToOutput("Bucket to update: "+bucket)
                def updateBucketQuery = this.CATALOG_URL + "/buckets/" + bucket + "?owner=" + this.BUCKET_OWNER
                def put = new org.apache.http.client.methods.HttpPut(updateBucketQuery)
                put.addHeader("sessionId", this.sessionId)
                put.addHeader("Accept", "application/json")
                put.addHeader("Content-Type", "application/json")
                def putResponse = getHttpClientBuilder().build().execute(put)
                def putResponseStatusLine = putResponse.getStatusLine()
                if (putResponseStatusLine.getStatusCode() >= 400) {
                    writeToOutput("Result of " + updateBucketQuery + ":")
                    writeToOutput(result)
                    throw new IllegalStateException("Invalid response status from " + updateBucketQuery + ": " + putResponseStatusLine)
                }
                def putRes = new BufferedInputStream(putResponse.getEntity().getContent())
                result = org.apache.commons.io.IOUtils.toString(putRes, "UTF-8")
                def bucketName = slurper.parseText(result.toString()).get("name")
                putRes.close();
                writeToOutput("The user group of " + bucketName + " is updated!")
            }
            return bucketFound.name
        } else {
            // POST QUERY
            def createBucketQuery = this.CATALOG_URL + "/buckets?name=" + bucket + "&owner=" + this.BUCKET_OWNER
            def post = new org.apache.http.client.methods.HttpPost(createBucketQuery)
            post.addHeader("Accept", "application/json")
            post.addHeader("Content-Type", "application/json")
            post.addHeader("sessionId", this.sessionId)

            response = getHttpClientBuilder().build().execute(post)
            responseStatusLine = response.getStatusLine()
            if (responseStatusLine.getStatusCode() >= 400) {
                writeToOutput("Result of " + createBucketQuery + ":")
                writeToOutput(result)
                throw new IllegalStateException("Invalid response status from " + createBucketQuery + ": " + responseStatusLine)
            }
            bis = new BufferedInputStream(response.getEntity().getContent())
            result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
            def bucketName = slurper.parseText(result.toString()).get("name")
            bis.close();
            writeToOutput(" " + bucket + " created!")
            return bucketName
        }
    }


    void populateBucket(packageCatalogMap, packageDir) {

        // Do nothing if there is no workflow to push
        if (packageCatalogMap == null)
            return

        def packageBucketsList
        //Transform a String to a List of String
        if (packageCatalogMap.get("bucket") instanceof String) {
            packageBucketsList = [packageCatalogMap.get("bucket")]

        } else {
            packageBucketsList = packageCatalogMap.get("bucket")
        }

        // BUCKETS SECTION /////////////////////////////

        packageBucketsList.each { bucket ->

            // Check user group
            if (packageCatalogMap.containsKey("userGroup") && !packageCatalogMap.get("userGroup").isEmpty()) {
                this.BUCKET_OWNER = "GROUP:" + packageCatalogMap.get("userGroup")
            }

            def bucketName = createBucketIfNotExist(bucket)

            // GET the bucket resources from the catalog
            // Create part of the GET query
            def listBucketResourcesQuery = this.CATALOG_URL + "/buckets/" + bucketName + "/resources"
            def get = new org.apache.http.client.methods.HttpGet(listBucketResourcesQuery)
            get.addHeader("sessionid", this.sessionId)

            // Build the GET query and execute it
            def response = getHttpClientBuilder().build().execute(get)
            def responseStatusLine = response.getStatusLine()
            if (responseStatusLine.getStatusCode() >= 400) {
                writeToOutput("Result of " + listBucketResourcesQuery + ":")
                writeToOutput(result)
                throw new IllegalStateException("Invalid response status from " + listBucketResourcesQuery + ": " + responseStatusLine)
            }

            // Parse the result of the GET query
            def bis = new BufferedInputStream(response.getEntity().getContent())
            def result = org.apache.commons.io.IOUtils.toString(bis, "UTF-8")
            bis.close()
            def catalogBucketResourcesList = slurper.parseText(result)

            // OBJECTS SECTION /////////////////////////////

            packageCatalogMap.get("objects").each { object ->
                //push object in the catalog
                pushObject(object, packageDir, catalogBucketResourcesList, bucketName)
            }
        }
    }


    void run(packageDir, loadDependencies) {

        // Connect to the scheduler
        loginAdminUserCredToSchedulerAndGetSessionId()

        // If the package dir is a zip file, create a temporary directory that contains the unzipped package dir
        writeToOutput(" Loading " + packageDir)
        def packageDirExt = FilenameUtils.getExtension(packageDir.name)
        def unzippedPackageDir
        if (packageDirExt == "zip"){
            unzippedPackageDir = unzipPackage(packageDir)
            packageDirExt = "." + packageDirExt
        }
        else if (packageDirExt.isEmpty())
            unzippedPackageDir = packageDir
        else {
            writeToOutput(" package dir extension not supported")
            throw new Exception(" package dir extension not supported")
        }

        // Parse the metadata json file
        def packageMetadataFile = new File(unzippedPackageDir, "METADATA.json")
        writeToOutput(" Parsing " + packageMetadataFile.absolutePath)
        def packageMetadataMap = (Map) slurper.parseText(packageMetadataFile.text)

        // LOAD PACKAGE DEPENDENCIES RECURSIVELY ////////////////////////////

        if (loadDependencies) {
            def dependenciesList = packageMetadataMap.get("dependencies")

            if (dependenciesList != null)
            {
                def parentPackageDir = packageDir.getAbsoluteFile().getParentFile()
                dependenciesList.each { packageName ->

                    this.run( new File(parentPackageDir, packageName + packageDirExt), loadDependencies)
                }
            }
        }

        // POPULATE DATASPACE ////////////////////////////

        def packageDataspaceMap = packageMetadataMap.get("dataspace")
        populateDataspace(packageDataspaceMap, unzippedPackageDir)

        // POPULATE BUCKETS /////////////////////////////

        def packageCatalogMap = packageMetadataMap.get("catalog")
        populateBucket(packageCatalogMap, unzippedPackageDir)

    }
}