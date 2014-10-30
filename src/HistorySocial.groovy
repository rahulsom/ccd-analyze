/**
    Created by brian.jong on 10/27/14.

    29762-2 - 2411 - Social History
    • Social
        o Use
        o Frequency

    Result
    Use count - 7661
    Frequency count - 398
    total entries - 9052
    total social history CCDs - 2411
    total valid social history CCDs - 2025
*/

@Grab("org.codehaus.gpars:gpars:1.2.1")
import groovyx.gpars.GParsPool
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

int useCount = 0
int frequencyCount = 0

def allTables = [socialUse:[:], socialFrequency:[:], totalEntries:0, totalCcds:0, totalValidCcds:0]
def arrayOfFiles = files

/*
* The closure passed here gets the benefits of added methods for GPars concurrency models based on executor pools.
*/
GParsPool.withPool {
    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section
        def ccdWithValidEntries = false

        sections.each { section ->
            def sectionName = section.code.@code.text()

            //History: Social
            if (sectionName == "29762-2") {
                def entries = section.entry;
                synchronized (allTables) {
                    allTables['totalCcds']++
                    allTables['totalEntries'] += entries.size()

                    entries.each { entry ->
                        def observation = entry.observation

                        //look for social name
                        String socialText = observation.text.text().trim()

                        if (socialText) {
                            def socialItem = socialText.toLowerCase().trim();
                            if (allTables['socialUse'].containsKey(socialItem)) {
                                allTables['socialUse'][socialItem]++
                            } else {
                                allTables['socialUse'][socialItem] = 1
                            }
                        }

                        String frequency = observation.value.text().trim()

                        if (frequency) {
                            def socialItem = frequency.toLowerCase().trim();
                            if (allTables['socialFrequency'].containsKey(socialItem)) {
                                allTables['socialFrequency'][socialItem]++
                            } else {
                                allTables['socialFrequency'][socialItem] = 1
                            }
                        }

                        ccdWithValidEntries |= (socialText || frequency)
                    }

                }
            }
        }
        if(ccdWithValidEntries)
            allTables['totalValidCcds']++
    }
}

allTables['socialUse'].each { k, v ->
    useCount += v
    println "$k - $v"
}
println()
allTables['socialFrequency'].each { k, v ->
    frequencyCount += v
    println "$k - $v"
}
println()
println "Use count - ${useCount}"
println "Frequency count - ${frequencyCount}"
println "total entries - ${allTables['totalEntries']}"
println "total social history CCDs - ${allTables['totalCcds']}"
println "total valid social history CCDs - ${allTables['totalValidCcds']}"
println()
