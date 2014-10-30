import groovyx.gpars.GParsPool
import groovyx.gpars.GParsPoolUtil
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

@Grab("org.codehaus.gpars:gpars:1.2.1")
/**
 * This script prints the frequency of each Immunization in the CCDs
 * Created by penny.lischer on 10/23/14.
 */


def sectionFrequency = [:]
def arrayOfFiles = files

def parallelCollection = GParsPool.withPool {

    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()

            synchronized (sectionFrequency) {
                if(sectionName == "11369-6") {
                    if (sectionFrequency.containsKey(sectionName)) {
                        sectionFrequency[sectionName]++
                    } else {
                        sectionFrequency[sectionName] = 1
                    }
                }
            }
        }
    }
}

sectionFrequency.each { k,v ->
    println "Code: $k Total: $v Percent in CCDS: ${v/arrayOfFiles.size()*100}%"
}
