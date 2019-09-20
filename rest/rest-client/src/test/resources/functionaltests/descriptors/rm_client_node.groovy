rmapi.connect()

println "Session id " + rmapi.getSession()

println "List of node events:"

full = rmapi.getRMStateFull()

full.getNodesEvents().each { event ->
    println(event.getNodeUrl())
    println(event.getNodeState())
}