import org.ow2.proactive.scheduler.util.GroovySchedulerStarter

def isWindows() {
    System.properties['os.name'].toLowerCase().contains('windows')
}

def joinPath(String... list) {
    list.join(File.separator)
}

def joinClasspath(String... list) {
    list.join(File.pathSeparator)
}

javaExe = joinPath(System.getProperty('java.home'), 'bin', isWindows() ? 'java.exe' : 'java')

schedulerHome = GroovySchedulerStarter.getSchedulerDir()

classpathDistLib = joinPath(schedulerHome, 'dist', 'lib', '*')

classpathAddons = joinPath(schedulerHome, 'addons', '*')

def schedulerStartCommand() {
    [javaExe, '-cp', joinClasspath(classpathDistLib, classpathAddons),
     'org.ow2.proactive.scheduler.util.SchedulerStarter', *args]
}

Process process = schedulerStartCommand().execute()
process.consumeProcessOutput(System.out, System.err)

addShutdownHook {
    process.destroy()
}

process.waitFor()


