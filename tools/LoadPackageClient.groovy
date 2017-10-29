
class LoadPackageClient {

    private final String LOAD_PACKAGE_SCRIPT_NAME = "LoadPackage.groovy"
    private final String SCHEDULER_HOME
    private final GroovyObject package_loader
    private final String TOOLS_DIR


    LoadPackageClient(binding) {

        this.SCHEDULER_HOME = binding.variables.get("pa.scheduler.home")

        // User variables
        this.TOOLS_DIR = new File(this.SCHEDULER_HOME, "tools")

        // Create a new instance of the package loader
        File load_package_script = new File(this.TOOLS_DIR, this.LOAD_PACKAGE_SCRIPT_NAME)
        if (load_package_script.exists()) {
            GroovyClassLoader gcl = new GroovyClassLoader()
            Class loadPackageClass = gcl.parseClass(load_package_script)
            this.package_loader= (GroovyObject) loadPackageClass.newInstance(binding)
        } else {
            return
        }

    }

    def run(File package_dir) {
        package_loader.run(package_dir)

    }
}

try {
    new LoadPackageClient(this.binding).run(new File(args[0]))
} catch (Exception e) {
    throw new Exception ("Failed to install package into catalog." + e.getMessage())
}