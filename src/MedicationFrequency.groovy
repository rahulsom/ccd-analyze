/**
 * Created by shannon.shih on 10/27/14.
 *
 * This script prints the counts of each field for Medications.
 *
 * Medications:
 *      - Name
 *      - Prescriber
 *      - Start Date
 *      - End Date
 *      - Status
 */

@Grab("org.codehaus.gpars:gpars:1.2.1")
import groovyx.gpars.GParsPool
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

def mapOfCounters = [numberOfCCDWithMedications: 0, validMedRecords: 0, nullMedRecords: 0, allMedRecords: 0, medStartDates:0, medEndDates:0, medStatus:0, medPrescribers:0]
def mapOfMedName = [:]
def mapOfStatus=[:]

GParsPool.withPool {
    files.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()
            synchronized (mapOfCounters) {
                // per ccd medication section
                if (sectionName == "10160-0") {
//                  println 'CCDFile ' + file.getName() + ' ----------------------';
                    boolean firstTimeInCCD = true;
                    //medication entries
                    def entries = section.entry;
                    if (entries.size() != 0) {
                        entries.each { entry ->

                            //information about medication
                            def medicationSubstance = entry.substanceAdministration

                            //medication name
                            String referenceValue = medicationSubstance.consumable.manufacturedProduct.manufacturedMaterial.code.originalText.reference.@value.text().replace('#', '')
//                            println 'ReferenceValue: ' + referenceValue

                            if(referenceValue){
                                //loop through unstructured data to find the referenceId & matching medName
                                def textBody = section.text.table.tbody.tr
                                def usdMedRef = textBody.depthFirst().find{ it.@ID.text().equalsIgnoreCase(referenceValue) }
                                if(usdMedRef) {
                                    def medName = usdMedRef.text()
//                                println medName
                                    if (medName) {
                                        if (firstTimeInCCD) {
                                            firstTimeInCCD = false
                                            mapOfCounters['numberOfCCDWithMedications']++
                                        }
                                        //save valid medication
//                                    mapOfMedName.put(referenceValue, medName)
                                        mapOfCounters['validMedRecords']++

                                        //medication time
                                        if(medicationSubstance.effectiveTime.low.@value.text()){
                                            mapOfCounters['medStartDates']++
                                        }
                                        if(medicationSubstance.effectiveTime.high.@value.text()){
                                            mapOfCounters['medEndDates']++
                                        }

                                        //status
                                        def medStatus = medicationSubstance.entryRelationship.observation.value.@displayName.text()
                                        if(medStatus){
                                            mapOfCounters['medStatus']++
                                            //number of different medication statuses
                                            if (mapOfStatus.containsKey(medStatus)) {
                                                mapOfStatus[medStatus]++
                                            } else {
                                                mapOfStatus[medStatus] = 1
                                            }
                                        }

                                        //perscriber
                                        if(medicationSubstance.entryRelationship.supply.author.assignedAuthor.assignedPerson.name.given.text()
                                                || medicationSubstance.entryRelationship.supply.author.assignedAuthor.assignedPerson.name.family.text()) {
                                            mapOfCounters['medPrescribers']++;
                                        }

                                    } else {
                                        //null medication names
                                        mapOfCounters['nullMedRecords']++
                                    }
                                }else
                                {
                                    //un-identified reference values
                                    mapOfCounters['nullMedRecords']++
                                }
                            }

                            mapOfCounters['allMedRecords']++;
                        }
                    }
                }
            }
        }
//        }
    }
}

println ''
println '-------------- RESULTS: --------------'
println 'Number of CCDS with Medications: ' + mapOfCounters.get('numberOfCCDWithMedications')
println 'Average number of Medications per CCD: ' + mapOfCounters.get('validMedRecords')/mapOfCounters.get('numberOfCCDWithMedications')
println ''
println 'All Medications Records: ' + mapOfCounters.get('allMedRecords')
println 'Number of Null Med: ' + mapOfCounters.get('nullMedRecords')
println 'Number of Valid Med: ' + mapOfCounters.get('validMedRecords')
println ''
println 'Number of Med Prescribers: ' + mapOfCounters.get('medPrescribers')
println 'Number of Med Start Dates: ' + mapOfCounters.get('medStartDates')
println 'Number of Med End Dates: ' + mapOfCounters.get('medEndDates')
println 'Number of Med Status: ' + mapOfCounters.get('medStatus')

//prints out all medication names
//mapOfMedName.each { k, v ->
//    println "$k - $v"
//}

//prints out medication status
mapOfStatus.each { k, v ->
    println "   $k - $v"
}


/*  10/29/2014
-------------- RESULTS: --------------
Number of CCDS with Medications: 7915
Average number of Medications per CCD: 40.4117498421

All Medications Records: 319888
Number of Null Med: 29
Number of Valid Med: 319859

Number of Med Prescribers: 99783
Number of Med Start Dates: 299294
Number of Med End Dates: 257720
Number of Med Status: 319859
   Active - 80044
   Inactive - 19552
   Drug Course Completed - 50899
   Discontinued - 165012
   No Longer Active - 4352
*/
