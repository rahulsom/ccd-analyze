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

def allergyCounters=[numberOfCCDWithAllergies:0, nullAllergies:0, noKnownAllergies:0, knownAllergies:0, allAllergyRecords:0, allergyStartDateCount:0, allergyEndDateCount:0, allergyObservationCount:0]

GParsPool.withPool {
    files.eachParallel { File file ->

        def xml = new XmlSlurper().parse(file.newInputStream())
        def sections = xml.component.structuredBody.component.section

        sections.each { section ->
            def sectionName = section.code.@code.text()
            synchronized (allergyCounters) {
                // per ccd allergy section
                if(sectionName == "48765-2"){
//                  println 'CCDFile ' + file.getName() + ' ----------------------';
                    boolean firstTimeInCCDFile = true;
                    //allergy entries
                    def entries = section.entry;
                    if (entries.size() != 0){
                        entries.each { entry ->
                            //information about allergies
                            def observation = entry.act.entryRelationship.observation;
                            String allergyName = observation.participant.participantRole.playingEntity.code.@displayName.text()
                            if (allergyName){
                                if(!allergyName.contains("NO KNOWN")) {
                                    //println allergyName
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
                                }
                                else{
                                    allergyCounters['noKnownAllergies']++;
                                }
                            }
                            else{
                                allergyCounters['nullAllergies']++;
                            }
                            allergyCounters['allAllergyRecords']++;

                            //information about the observation
                            def allergyReactionCode = observation.entryRelationship.observation.text.reference.@value.text();
                            if(allergyReactionCode.contains('allergy_reaction')){
                                def allergyReactionObservation = observation.entryRelationship.observation.text.text();
                                if(allergyReactionObservation){
                                    //println 'Allergy Reaction: ' + allergyReactionObservation;
                                    allergyCounters['allergyObservationCount']++;
                                }
                            }
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
println 'Number of Null Allergies: ' + allergyCounters.get('nullAllergies')
println 'Number of No Known Allergies: ' + allergyCounters.get('noKnownAllergies')
println 'Number of Known Allergies: ' + allergyCounters.get('knownAllergies')
println 'Number of Allergy Start Dates: ' + allergyCounters.get('allergyStartDateCount')
println 'Number of Allergy End Dates: ' + allergyCounters.get('allergyEndDateCount')
println 'Number of Allergy Reaction Observation: ' + allergyCounters.get('allergyObservationCount')


/* 10/29/2014 -
-------------- RESULTS: --------------
Number of CCDS with Allergies: 3685
Average number of Allergies per CCD: 2.4377204885

All Allergy Records: 16390
Number of Null Allergies: 6181
Number of No Known Allergies: 1226
Number of Known Allergies: 8983
Number of Allergy Start Dates: 505
Number of Allergy End Dates: 0
Number of Allergy Reaction Observation: 944
 */
