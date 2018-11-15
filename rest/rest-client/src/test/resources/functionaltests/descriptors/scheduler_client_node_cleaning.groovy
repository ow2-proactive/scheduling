schedulerapi.connect()
println "SCHEDULERAPI_URI_LIST_NOT_NULL="+(schedulerapi.getGlobalSpaceURIs()!=null)
userspaceapi.connect()
println "USERSPACE_FILE_LIST_NOT_NULL="+(userspaceapi.listFiles(".", "*")!=null)
globalspaceapi.connect()

println "GLOBALSPACE_FILE_LIST_NOT_NULL="+(globalspaceapi.listFiles(".", "*")!=null)
println "TEST_CREDS="+(credentials.get("TEST_CREDS"))



