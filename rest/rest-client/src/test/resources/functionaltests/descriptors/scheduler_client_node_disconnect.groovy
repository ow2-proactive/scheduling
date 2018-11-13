import org.codehaus.groovy.runtime.StackTraceUtils

try {
    schedulerapi.connect()
    assert schedulerapi.isConnected()
    sessionid = schedulerapi.getSession()
    schedulerapi.disconnect()
    assert !schedulerapi.isConnected()
    schedulerapi.connect()
    assert schedulerapi.isConnected()
    newsessionid = schedulerapi.getSession()
    assert sessionid != newsessionid
    result = true
} catch (Exception e) {
    StackTraceUtils.sanitize(e)
    println "Error at line : " +e.stackTrace.head().lineNumber
    e.printStackTrace()
    throw e
}


