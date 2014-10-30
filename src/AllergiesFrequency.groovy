/**
 * Created by shannon.shih on 10/27/14.
 *
 * This script prints the counts of each field for Allergies.
 *
 * Allergies:
 *      - Name
 *      - Start Date
 *      - End Date
 *      - Observation / Reaction
 *
 */

@Grab("org.codehaus.gpars:gpars:1.2.1")
import groovyx.gpars.GParsPool
import groovy.transform.BaseScript

@BaseScript CcdAnalysis script

def allergyCounters=[numberOfCCDWithAllergies:0, nullRefAllergies:0, noKnownAllergies:0, knownAllergies:0, allAllergyRecords:0, allergyStartDateCount:0, allergyEndDateCount:0, allergyObservationCount:0]

GParsPool.withPool {
    files.eachParallel { File file ->
        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()
            synchronized (allergyCounters) {
                // per ccd allergy section
                if (sectionName == "48765-2") {
//                  println 'CCDFile ' + file.getName() + ' ----------------------';
                    boolean firstTimeInCCDFile = true;
                    //allergy entries
                    def entries = section.entry;
                    if (entries.size() != 0) {
                        entries.each { entry ->
                            //information about allergies
                            def observation = entry.act.entryRelationship.observation;
                            String referenceValue = observation.participant.participantRole.playingEntity.code.originalText.reference.@value.text().replace('#', '')
//                                println 'REF: ' + referenceValue

                            if(referenceValue){
                                //search for matching allergy title
                                def textBody = section.text.table.tbody
                                def usdAllergyRef = textBody.depthFirst().find { it.@ID.text().equalsIgnoreCase(referenceValue) }
                                if (usdAllergyRef) {
                                    def allergyName = usdAllergyRef.text()
                                    if (allergyName) {
                                        if (!allergyName.contains("NO KNOWN")) {
//                                                println allergyName
                                            allergyCounters['knownAllergies']++;

                                            if (firstTimeInCCDFile) {
                                                allergyCounters['numberOfCCDWithAllergies']++;
                                                firstTimeInCCDFile = false;
                                            }
                                            if (observation.effectiveTime.low.@value.text()) {
                                                allergyCounters['allergyStartDateCount']++;
                                            }
                                            if (observation.effectiveTime.high.@value.text()) {
                                                allergyCounters['allergyEndDateCount']++;
                                            }

                                            //information about the observation
                                            def allergyReactionCode = observation.entryRelationship.observation.text.reference.@value.text();
                                            if (allergyReactionCode.contains('allergy_reaction')) {
                                                def allergyReactionObservation = observation.entryRelationship.observation.text.text();
                                                if (allergyReactionObservation) {
                                                    //println 'Allergy Reaction: ' + allergyReactionObservation;
                                                    allergyCounters['allergyObservationCount']++;
                                                }
                                            }

                                        } else {
                                            allergyCounters['noKnownAllergies']++;
                                        }
                                    } else {
                                        //empty allergy names
                                        allergyCounters['nullRefAllergies']++;
                                    }
                                } else {
                                    //reference value could not finding matching title
                                    allergyCounters['nullRefAllergies']++;
                                }
                            }else{
                                //reference value is null
                                allergyCounters['nullRefAllergies']++;
                            }
                            allergyCounters['allAllergyRecords']++;
                        }
                    }
                }
            }
        }
    }
}

println ''
println '-------------- RESULTS: --------------'
println 'Number of CCDS with Allergies: ' + allergyCounters.get('numberOfCCDWithAllergies')
println 'Average number of Allergies per CCD: ' + allergyCounters.get('knownAllergies')/allergyCounters.get('numberOfCCDWithAllergies');
println ''
println 'All Allergy Records: ' + allergyCounters.get('allAllergyRecords')
println 'Number of Null Ref Allergies: ' + allergyCounters.get('nullRefAllergies')
println 'Number of No Known Allergies: ' + allergyCounters.get('noKnownAllergies')
println 'Number of Known Allergies: ' + allergyCounters.get('knownAllergies')
println 'Number of Allergy Start Dates: ' + allergyCounters.get('allergyStartDateCount')
println 'Number of Allergy End Dates: ' + allergyCounters.get('allergyEndDateCount')
println 'Number of Allergy Reaction Observation: ' + allergyCounters.get('allergyObservationCount')


/* 10/30/2014 -
-------------- RESULTS: --------------
Number of CCDS with Allergies: 3738
Average number of Allergies per CCD: 2.4459604066

All Allergy Records: 16390
Number of Null Ref Allergies: 6021
Number of No Known Allergies: 1226
Number of Known Allergies: 9143
Number of Allergy Start Dates: 520
Number of Allergy End Dates: 0
Number of Allergy Reaction Observation: 942
 */
