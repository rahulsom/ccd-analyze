/**
    Created by brian.jong on 10/27/14.

    29762-2 - 2411 - Social History
    • Social
        o Use
        o Frequency

    Result
    Use count - 7661
    Frequency count - 398
*/

@Grab("org.codehaus.gpars:gpars:1.2.1")
import groovyx.gpars.GParsPool

int useCount = 0
int frequencyCount = 0

def allTables = [socialUse:[:], socialFrequency:[:]]
def arrayOfFiles = new File("/Users/brian.jong/desktop/certify-ccds").listFiles()

/*
* The closure passed here gets the benefits of added methods for GPars concurrency models based on executor pools.
*/
GParsPool.withPool {
    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()

            //History: Social
            if (sectionName == "29762-2") {
                def entries = section.entry;
                entries.each { entry ->
                    def observation = entry.observation
                    synchronized (allTables) {
                        //look for social name
                        String socialText = observation.text.text()

                        if (socialText) {
                            def socialItem = socialText.toLowerCase().trim();
                            if (allTables['socialUse'].containsKey(socialItem)) {
                                allTables['socialUse'][socialItem]++
                            } else {
                                allTables['socialUse'][socialItem] = 1
                            }
                        }

                        String frequency = observation.value.text()

                        if (frequency) {
                            def socialItem = frequency.toLowerCase().trim();
                            if (allTables['socialFrequency'].containsKey(socialItem)) {
                                allTables['socialFrequency'][socialItem]++
                            } else {
                                allTables['socialFrequency'][socialItem] = 1
                            }
                        }
                    }
                }
            }
        }
    }
}

allTables['socialUse'].each { k, v ->
    useCount += v
    println "$k - $v"
}
println()
println "Use count - ${useCount}"
println()

allTables['socialFrequency'].each { k, v ->
    frequencyCount += v
    println "$k - $v"
}
println()
println "Frequency count - ${frequencyCount}"
