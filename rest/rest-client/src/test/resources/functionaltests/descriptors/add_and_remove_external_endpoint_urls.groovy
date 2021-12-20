schedulerapi.connect()

schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://aaa.fr")
schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://bbb.fr")

schedulerapi.removeExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://aaa.fr")

schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://ccc.fr")
schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://ddd.fr")

schedulerapi.removeExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://aaa.fr")
schedulerapi.removeExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://ddd.fr")

schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "http://eee.fr")
