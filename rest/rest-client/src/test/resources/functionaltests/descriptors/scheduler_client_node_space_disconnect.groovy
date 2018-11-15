import org.codehaus.groovy.runtime.StackTraceUtils

try {
    userspaceapi.connect()
    assert userspaceapi.isConnected()
    assert globalspaceapi.isConnected()
    assert schedulerapi.isConnected()

    sessionid = schedulerapi.getSession()

    userspaceapi.disconnect()

    assert !userspaceapi.isConnected()
    assert !globalspaceapi.isConnected()
    assert !schedulerapi.isConnected()

    schedulerapi.connect()
    assert userspaceapi.isConnected()
    assert globalspaceapi.isConnected()
    assert schedulerapi.isConnected()

    newsessionid = schedulerapi.getSession()
    assert sessionid != newsessionid
    return true
} catch (Exception e) {
    StackTraceUtils.sanitize(e)
    println "Error at line : " +e.stackTrace.head().lineNumber
    e.printStackTrace()
    throw e
}


