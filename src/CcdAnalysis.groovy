

/**
 * Created by rahul on 10/28/14.
 */
abstract class CcdAnalysis extends Script{
    long startTime
    String[] cliArgs
    boolean filesCalled = false

    @Override
    def run() {
        before()
        internalRun()
        after()
    }

    abstract internalRun()

    def before() {
        startTime = System.nanoTime()
        cliArgs = this.binding.variables['args']
        println "Starting script at ${new Date()}"
    }
    def after()  {
        long endTime = System.nanoTime()
        println "Script ended at ${new Date()} and took ${(endTime - startTime)/10E6} ms"
        if (!filesCalled) {
            throw new RuntimeException("Script did not use files")
        }
    }

    def getDirectory() {
        if (cliArgs.length == 0) {
            new File(".").canonicalPath
        } else {
            new File(cliArgs[0]).canonicalPath
        }
    }

    List<File> accumulate(List<File> sum, File f) {
        if (f.isDirectory() && f.name != '.git') {
            f.listFiles().inject sum, {List<File> s1, f1 -> accumulate(s1, f1)}
        } else if (f.name.endsWith(".xml")){
            sum + f
        } else {
            sum
        }
    }

    def getFiles() {
        filesCalled = true
        new File(directory).listFiles().inject([]) { List<File> sum, f ->
            accumulate(sum, f)
        }
    }

}
