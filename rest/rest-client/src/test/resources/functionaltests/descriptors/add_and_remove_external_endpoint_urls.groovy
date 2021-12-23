schedulerapi.connect()

schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "aaa", "http://aaa.fr", "icon/aaa")
schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "bbb", "http://bbb.fr", "icon/bbb")

schedulerapi.removeExternalEndpointUrl(variables.get("PA_JOB_ID"), "aaa")

schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "ccc", "http://ccc.fr", "icon/ccc")
schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "ddd", "http://ddd.fr", "icon/ddd")

schedulerapi.removeExternalEndpointUrl(variables.get("PA_JOB_ID"), "aaa")
schedulerapi.removeExternalEndpointUrl(variables.get("PA_JOB_ID"), "ddd")

schedulerapi.addExternalEndpointUrl(variables.get("PA_JOB_ID"), "eee", "http://eee.fr", null)
