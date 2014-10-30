import groovyx.gpars.GParsPool
import groovyx.gpars.GParsPoolUtil
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

@Grab("org.codehaus.gpars:gpars:1.2.1")
/**
 * This script prints the frequency of each Vital in the CCDs
 * Created by penny.lischer on 10/23/14.
 */


def sectionFrequency = [:]
def sectionNames = [:]

def arrayOfFiles = files

def parallelCollection = GParsPool.withPool {

    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section
        def vitals = sections.find {it.code.@code.text() == '8716-3'}
        def entries = vitals.entry
        def vitalSigns = entries.organizer.component.observation.code.collect{
            [it.@code.text(), it.@displayName.text()]
        }.unique()

        vitalSigns.each { vital ->
            def sectionName = vital[0]

            synchronized (sectionFrequency) {
                if (sectionFrequency.containsKey(sectionName)) {
                    sectionFrequency[sectionName]++
                } else {
                    sectionFrequency[sectionName] = 1
                    sectionNames[sectionName] = vital[1]
                }
            }
        }
    }
}

sectionFrequency.each { k,v ->
    println "Code: $k Total: $v Name: ${sectionNames[k]} Percent in CCDS: ${v/arrayOfFiles.size()*100}%"
}
