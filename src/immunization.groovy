import groovyx.gpars.GParsPool
import groovyx.gpars.GParsPoolUtil
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

@Grab("org.codehaus.gpars:gpars:1.2.1")
/**
 * This script prints the frequency of each Immunization in the CCDs
 * Created by penny.lischer on 10/23/14.
 */

def immunizationCounters =[ccdWithImmunizations:0, immunizationRecords:0, name:0, date:0, nullImmunizations:0]
def arrayOfFiles = files

def parallelCollection = GParsPool.withPool {

    arrayOfFiles.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()

            synchronized (immunizationCounters) {

                if (sectionName == "11369-6") {
                    boolean firstTimeIn = true;

                    def entries = section.entry;
                    if (entries.size() != 0) {
                        entries.each { entry ->
                            def substanceAdministration = entry.substanceAdministration;

                            immunizationCounters['immunizationRecords']++

                            String referenceValue = substanceAdministration.text.reference.@value.text().replace('#', '')
//                            println 'REF: ' + referenceValue

                            if(referenceValue) {
                                //search for matching lab test title
                                def textBody = section.text.table.tbody
                                def usdImuRef = textBody.depthFirst().find {
                                    it.@ID.text().equalsIgnoreCase(referenceValue)
                                }

                                if (usdImuRef) {
//                                    println usdImuRef
                                    def immunizationName = usdImuRef.text()

                                    if (immunizationName) {
                                        if (firstTimeIn) {
                                            immunizationCounters['ccdWithImmunizations']++;
                                            firstTimeIn = false;
                                        }

                                        //Immunization Name
                                        immunizationCounters['name']++

                                        def date = substanceAdministration.effectiveTime.@value.text();
                                        //Date
                                        if (date) {
                                            immunizationCounters['date']++
                                        }
                                    }
                                } else {
                                    immunizationCounters['nullImmunizations']++;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

println 'Number of CCDS with Immunizations: ' + immunizationCounters.get('ccdWithImmunizations');
println 'Number of Null Immunizations: ' + immunizationCounters.get('nullImmunizations')
println 'Percent of Immunizations per total CCDs: ' + (immunizationCounters.get('ccdWithImmunizations')/arrayOfFiles.size()) *100 + '%'
println 'Immunizations Record Total: ' + immunizationCounters.get('immunizationRecords');
println "Immunization Name Total:" + immunizationCounters.get('name')
println "Percent of Immunization name in each CCD: " + (immunizationCounters.get('name')/immunizationCounters.get('immunizationRecords'))*100 + '%'
println "Date Total:" + immunizationCounters.get('date')
println "Percent of date in each CCD: " + (immunizationCounters.get('date')/immunizationCounters.get('immunizationRecords'))*100 + '%'
