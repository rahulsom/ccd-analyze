import groovyx.gpars.GParsPool
import groovyx.gpars.GParsPoolUtil
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script
@Grab("org.codehaus.gpars:gpars:1.2.1")
/**
 * This script prints the frequency of each section in the CCD
 * Created by penny.lischer on 10/23/14.
 */

def encounterCounters=[ccdWithEncounters:0, timeLow:0, timeHigh:0, performer:0, source:0]

//def arrayOfFiles = new File("/Users/penny.lischer/documents/certify-ccds").listFiles()
def arrayOfFiles = files

def parallelCollection = GParsPool.withPool {

    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()

            synchronized (encounterCounters) {

                if (sectionName == "46240-8") {
                    boolean firstTimeIn = false;

                    def entries = section.entry;
                    if (entries.size() != 0) {
                        entries.each { entry ->
                            def encounter = entry.encounter;
                            def performer = encounter.performer;
                            def timeLow = encounter.effectiveTime.low.@value.text();
                            def timeHigh = encounter.effectiveTime.high.@value.text();
                            def performerName = performer.assignedEntity.assignedPerson.name.family.text();
                            def street = performer.assignedEntity.addr.streetAddressLine.text();
                            def city = performer.assignedEntity.addr.city.text();
                            def state = performer.assignedEntity.addr.state.text();
                            def postalCode = performer.assignedEntity.addr.postalCode.text();
                            def country = performer.assignedEntity.addr.country.text();

                            if (firstTimeIn == false) {
                                encounterCounters['ccdWithEncounters']++;
                                firstTimeIn = true;
                            }

                            //Performer
                            if (performerName) {
                                encounterCounters['performer']++
                            }

                            //Source
                            if (street || city || state || postalCode || country) {
                                encounterCounters['source']++
                            }

                            //Date
                            if (timeLow) {
                                encounterCounters['timeLow']++
                            }

                            if (timeHigh) {
                                encounterCounters['timeHigh']++
                            }
                        }
                    }
                }
            }
        }
    }
}

println 'Number of CCDS with Encounters: ' + encounterCounters.get('ccdWithEncounters')+ '%';
println 'Percent of Encounters that contain one test in the total CCDS: ' + (encounterCounters.get('ccdWithEncounters')/arrayOfFiles.size()) *100 + '%';
println "Type:???"
println "Performer Total:" + encounterCounters.get('performer')
println "Source Total:" + encounterCounters.get('source')
println "Time Low Total:" + encounterCounters.get('timeLow')
println "Time High Total:" + encounterCounters.get('timeHigh')

